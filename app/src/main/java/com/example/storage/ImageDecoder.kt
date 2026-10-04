package com.example.storage

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.provider.OpenableColumns
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max

object ImageDecoder {

    data class DecodedImageResult(
        val previewBitmap: Bitmap,
        val originalWidth: Int,
        val originalHeight: Int,
        val fileName: String,
        val mimeType: String?
    )

    /**
     * Decodes an image from content URI with proper EXIF orientation handling
     * and downsampling for responsive preview.
     */
    suspend fun decodeForPreview(
        context: Context,
        uri: Uri,
        maxPreviewDimension: Int = 1600
    ): Result<DecodedImageResult> = withContext(Dispatchers.IO) {
        try {
            val fileName = queryFileName(context, uri) ?: "photo.jpg"
            val mimeType = context.contentResolver.getType(uri)

            // Check if user selected known unsupported RAW formats
            val lowerName = fileName.lowercase()
            val isKnownUnsupportedRaw = lowerName.endsWith(".arw") ||
                    lowerName.endsWith(".cr2") ||
                    lowerName.endsWith(".cr3") ||
                    lowerName.endsWith(".nef") ||
                    lowerName.endsWith(".orf") ||
                    lowerName.endsWith(".rw2")

            if (isKnownUnsupportedRaw) {
                return@withContext Result.failure(
                    UnsupportedOperationException("RAW format not supported by the current decoder.")
                )
            }

            // 1. First pass: Query dimensions
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Unable to open image stream."))

            val rawWidth = boundsOptions.outWidth
            val rawHeight = boundsOptions.outHeight

            if (rawWidth <= 0 || rawHeight <= 0) {
                // If it's a DNG or other RAW that BitmapFactory could not decode
                if (lowerName.endsWith(".dng") || lowerName.endsWith(".raw")) {
                    return@withContext Result.failure(
                        UnsupportedOperationException("RAW format not supported by the current decoder.")
                    )
                }
                return@withContext Result.failure(IllegalArgumentException("Unsupported or corrupted image file."))
            }

            // 2. Compute sample size for preview
            var sampleSize = 1
            val maxEdge = max(rawWidth, rawHeight)
            while ((maxEdge / (sampleSize * 2)) >= maxPreviewDimension) {
                sampleSize *= 2
            }

            // 3. Decode scaled bitmap
            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val decodedBitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            }

            if (decodedBitmap == null) {
                if (lowerName.endsWith(".dng") || lowerName.endsWith(".raw")) {
                    return@withContext Result.failure(
                        UnsupportedOperationException("RAW format not supported by the current decoder.")
                    )
                }
                return@withContext Result.failure(IllegalArgumentException("Failed to decode image data."))
            }

            // 4. Read EXIF orientation
            val orientation = readExifOrientation(context, uri)
            val orientedBitmap = applyExifOrientation(decodedBitmap, orientation)

            val finalWidth = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270
            ) rawHeight else rawWidth

            val finalHeight = if (orientation == ExifInterface.ORIENTATION_ROTATE_90 ||
                orientation == ExifInterface.ORIENTATION_ROTATE_270
            ) rawWidth else rawHeight

            Result.success(
                DecodedImageResult(
                    previewBitmap = orientedBitmap,
                    originalWidth = finalWidth,
                    originalHeight = finalHeight,
                    fileName = fileName,
                    mimeType = mimeType
                )
            )
        } catch (e: UnsupportedOperationException) {
            Result.failure(e)
        } catch (e: OutOfMemoryError) {
            Result.failure(IllegalStateException("Photograph requires too much memory to preview."))
        } catch (e: Exception) {
            Result.failure(IllegalArgumentException("Could not load image: ${e.localizedMessage ?: "unknown error"}"))
        }
    }

    /**
     * Decodes the full resolution image for export.
     * Uses memory safety checks to avoid OOM on very large photos.
     */
    suspend fun decodeFullResolution(
        context: Context,
        uri: Uri
    ): Result<Bitmap> = withContext(Dispatchers.IO) {
        try {
            // First check dimensions
            val boundsOptions = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, boundsOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Cannot open image for export."))

            val maxAllowedPixels = 36_000_000 // 36 Megapixels limit for safe export allocation
            var sampleSize = 1
            var pixels = (boundsOptions.outWidth.toLong() * boundsOptions.outHeight)
            while (pixels > maxAllowedPixels) {
                sampleSize *= 2
                pixels /= 4
            }

            val decodeOptions = BitmapFactory.Options().apply {
                inSampleSize = sampleSize
                inPreferredConfig = Bitmap.Config.ARGB_8888
            }

            val bitmap = context.contentResolver.openInputStream(uri)?.use { stream ->
                BitmapFactory.decodeStream(stream, null, decodeOptions)
            } ?: return@withContext Result.failure(IllegalStateException("Failed to decode full resolution photo."))

            val orientation = readExifOrientation(context, uri)
            val oriented = applyExifOrientation(bitmap, orientation)
            Result.success(oriented)
        } catch (e: OutOfMemoryError) {
            Result.failure(IllegalStateException("Image is too large to export at full resolution."))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun readExifOrientation(context: Context, uri: Uri): Int {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val exif = ExifInterface(stream)
                exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)
            } ?: ExifInterface.ORIENTATION_NORMAL
        } catch (_: Exception) {
            ExifInterface.ORIENTATION_NORMAL
        }
    }

    private fun applyExifOrientation(bitmap: Bitmap, orientation: Int): Bitmap {
        val matrix = Matrix()
        when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
            ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
            ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
            ExifInterface.ORIENTATION_TRANSPOSE -> {
                matrix.postRotate(90f)
                matrix.postScale(-1f, 1f)
            }
            ExifInterface.ORIENTATION_TRANSVERSE -> {
                matrix.postRotate(270f)
                matrix.postScale(-1f, 1f)
            }
            else -> return bitmap
        }

        return try {
            val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
            if (rotated != bitmap) {
                bitmap.recycle()
            }
            rotated
        } catch (_: Exception) {
            bitmap
        }
    }

    fun queryFileName(context: Context, uri: Uri): String? {
        if (uri.scheme == "content") {
            try {
                context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                    if (cursor.moveToFirst()) {
                        val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                        if (nameIndex >= 0) {
                            return cursor.getString(nameIndex)
                        }
                    }
                }
            } catch (_: Exception) { }
        }
        return uri.lastPathSegment
    }
}
