package com.example.engine

import android.graphics.Bitmap
import com.example.model.ColorPreset
import com.example.model.CubeLut
import com.example.model.ManualAdjustments
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.withContext
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow

object ColorEngine {

    /**
     * Applies color grading from preset/LUT and manual adjustments.
     * NEVER mutates the input sourceBitmap. Returns a newly rendered Bitmap.
     */
    suspend fun processImage(
        sourceBitmap: Bitmap,
        preset: ColorPreset?,
        lut: CubeLut?,
        intensity: Float,
        adjustments: ManualAdjustments
    ): Bitmap = withContext(Dispatchers.Default) {
        val width = sourceBitmap.width
        val height = sourceBitmap.height
        val totalPixels = width * height

        // Output bitmap config
        val outputBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val srcPixels = IntArray(totalPixels)
        sourceBitmap.getPixels(srcPixels, 0, width, 0, 0, width, height)
        val dstPixels = IntArray(totalPixels)

        val intensityRatio = (intensity / 100f).coerceIn(0f, 1f)
        val hasPreset = preset != null && preset.id != "preset_original" && intensityRatio > 0.001f
        val hasLut = lut != null && intensityRatio > 0.001f
        val hasAdjustments = !adjustments.isDefault

        // If no edits are applied, copy source pixels directly
        if (!hasPreset && !hasLut && !hasAdjustments) {
            outputBitmap.setPixels(srcPixels, 0, width, 0, 0, width, height)
            return@withContext outputBitmap
        }

        // Precompute manual adjustment constants
        val exposureFactor = 2f.pow(adjustments.exposure / 50f)
        val contrastFactor = (adjustments.contrast + 100f) / 100f
        val tempShift = (adjustments.temperature / 100f) * 0.18f
        val tintShift = (adjustments.tint / 100f) * 0.18f
        val satFactor = (adjustments.saturation + 100f) / 100f
        val vibranceFactor = (adjustments.vibrance / 100f) * 0.5f
        val shadowsFactor = (adjustments.shadows / 100f) * 0.28f
        val highlightsFactor = (adjustments.highlights / 100f) * 0.28f
        val fadeFactor = (adjustments.fade / 100f) * 0.20f

        // Partition work across available CPU cores
        val cores = Runtime.getRuntime().availableProcessors().coerceIn(2, 16)
        val chunkSize = (totalPixels + cores - 1) / cores

        coroutineScope {
            val jobs = (0 until cores).map { coreIdx ->
                val start = coreIdx * chunkSize
                val end = min(start + chunkSize, totalPixels)
                async {
                    if (start >= end) return@async
                    val tempLutOut = FloatArray(3)

                    for (i in start until end) {
                        val pixel = srcPixels[i]
                        val a = (pixel ushr 24) and 0xFF
                        val origR = ((pixel ushr 16) and 0xFF) / 255f
                        val origG = ((pixel ushr 8) and 0xFF) / 255f
                        val origB = (pixel and 0xFF) / 255f

                        var r = origR
                        var g = origG
                        var b = origB

                        // 1. Apply LUT or Preset
                        if (hasLut && lut != null) {
                            lut.sample(origR, origG, origB, tempLutOut)
                            r = origR + (tempLutOut[0] - origR) * intensityRatio
                            g = origG + (tempLutOut[1] - origG) * intensityRatio
                            b = origB + (tempLutOut[2] - origB) * intensityRatio
                        } else if (hasPreset && preset != null) {
                            // Preset exposure bias
                            if (preset.exposureBias != 0f) {
                                val exp = 2f.pow(preset.exposureBias * intensityRatio)
                                r *= exp
                                g *= exp
                                b *= exp
                            }

                            // Preset temperature & tint
                            val pTemp = (preset.temperature / 100f) * 0.22f * intensityRatio
                            val pTint = (preset.tint / 100f) * 0.22f * intensityRatio
                            r += pTemp + pTint * 0.5f
                            g -= pTint
                            b -= pTemp

                            // Preset split toning (highlights & shadows)
                            val lum = (0.2126f * r + 0.7152f * g + 0.0722f * b).coerceIn(0f, 1f)
                            val shWeight = (1f - lum) * (1f - lum)
                            val hlWeight = lum * lum
                            r += (preset.shadowTintR * shWeight + preset.highlightTintR * hlWeight) * intensityRatio
                            g += (preset.shadowTintG * shWeight + preset.highlightTintG * hlWeight) * intensityRatio
                            b += (preset.shadowTintB * shWeight + preset.highlightTintB * hlWeight) * intensityRatio

                            // Preset highlights & shadows tone curves
                            val pSh = (preset.shadows / 100f) * 0.25f * intensityRatio
                            val pHl = (preset.highlights / 100f) * 0.25f * intensityRatio
                            val toneLift = (pSh * shWeight) + (pHl * hlWeight)
                            r += toneLift
                            g += toneLift
                            b += toneLift

                            // Preset contrast
                            if (preset.contrast != 1.0f) {
                                val pContrast = 1.0f + (preset.contrast - 1.0f) * intensityRatio
                                r = 0.5f + (r - 0.5f) * pContrast
                                g = 0.5f + (g - 0.5f) * pContrast
                                b = 0.5f + (b - 0.5f) * pContrast
                            }

                            // Preset film toe fade
                            if (preset.curveLift > 0f) {
                                val lift = preset.curveLift * intensityRatio
                                r = r * (1f - lift) + lift
                                g = g * (1f - lift) + lift
                                b = b * (1f - lift) + lift
                            }

                            // Preset saturation
                            if (preset.saturation != 1.0f || preset.vibrance != 0f) {
                                val pSat = 1.0f + (preset.saturation - 1.0f) * intensityRatio
                                val newLum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                                r = newLum + (r - newLum) * pSat
                                g = newLum + (g - newLum) * pSat
                                b = newLum + (b - newLum) * pSat
                            }

                            // Blend preset with original based on intensity
                            r = origR + (r - origR) * intensityRatio
                            g = origG + (g - origG) * intensityRatio
                            b = origB + (b - origB) * intensityRatio
                        }

                        // 2. Apply Manual Adjustments on top
                        if (hasAdjustments) {
                            // Exposure
                            if (exposureFactor != 1.0f) {
                                r *= exposureFactor
                                g *= exposureFactor
                                b *= exposureFactor
                            }

                            // Contrast
                            if (contrastFactor != 1.0f) {
                                r = 0.5f + (r - 0.5f) * contrastFactor
                                g = 0.5f + (g - 0.5f) * contrastFactor
                                b = 0.5f + (b - 0.5f) * contrastFactor
                            }

                            // Temperature & Tint
                            if (tempShift != 0f || tintShift != 0f) {
                                r += tempShift + tintShift * 0.5f
                                g -= tintShift
                                b -= tempShift
                            }

                            // Highlights & Shadows
                            if (shadowsFactor != 0f || highlightsFactor != 0f) {
                                val lum = (0.2126f * r + 0.7152f * g + 0.0722f * b).coerceIn(0f, 1f)
                                val shW = (1f - lum) * (1f - lum)
                                val hlW = lum * lum
                                val adjustLift = (shadowsFactor * shW) + (highlightsFactor * hlW)
                                r += adjustLift
                                g += adjustLift
                                b += adjustLift
                            }

                            // Saturation & Vibrance
                            if (satFactor != 1.0f || vibranceFactor != 0f) {
                                val curLum = 0.2126f * r + 0.7152f * g + 0.0722f * b
                                val maxC = max(r, max(g, b))
                                val minC = min(r, min(g, b))
                                val chrominance = (maxC - minC).coerceIn(0f, 1f)
                                val effectiveSat = satFactor + (1f - chrominance) * vibranceFactor
                                r = curLum + (r - curLum) * effectiveSat
                                g = curLum + (g - curLum) * effectiveSat
                                b = curLum + (b - curLum) * effectiveSat
                            }

                            // Fade (lift blacks)
                            if (fadeFactor > 0f) {
                                r = r * (1f - fadeFactor) + fadeFactor
                                g = g * (1f - fadeFactor) + fadeFactor
                                b = b * (1f - fadeFactor) + fadeFactor
                            }
                        }

                        // Clamp to [0, 255]
                        val finalR = (r.coerceIn(0f, 1f) * 255f).toInt()
                        val finalG = (g.coerceIn(0f, 1f) * 255f).toInt()
                        val finalB = (b.coerceIn(0f, 1f) * 255f).toInt()

                        dstPixels[i] = (a shl 24) or (finalR shl 16) or (finalG shl 8) or finalB
                    }
                }
            }
            jobs.awaitAll()
        }

        outputBitmap.setPixels(dstPixels, 0, width, 0, 0, width, height)
        outputBitmap
    }
}
