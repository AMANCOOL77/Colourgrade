package com.example

import android.graphics.Bitmap
import com.example.engine.ColorEngine
import com.example.engine.CubeLutParser
import com.example.model.ColorPreset
import com.example.model.ManualAdjustments
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayInputStream

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ColorLabEngineTest {

    @Test
    fun testCubeLutParser_valid3dLut() {
        val cubeContent = """
            # Standard test cube LUT
            TITLE "Test Hollywood Grade"
            LUT_3D_SIZE 2
            DOMAIN_MIN 0.0 0.0 0.0
            DOMAIN_MAX 1.0 1.0 1.0
            0.0 0.0 0.0
            1.0 0.0 0.0
            0.0 1.0 0.0
            1.0 1.0 0.0
            0.0 0.0 1.0
            1.0 0.0 1.0
            0.0 1.0 1.0
            1.0 1.0 1.0
        """.trimIndent()

        val stream = ByteArrayInputStream(cubeContent.toByteArray())
        val result = CubeLutParser.parse(stream, "Default Name")

        assertTrue(result.isSuccess)
        val lut = result.getOrNull()
        assertNotNull(lut)
        assertEquals("Test Hollywood Grade", lut!!.name)
        assertEquals(2, lut.size)
        assertTrue(lut.is3D)

        // Test trilinear sampling
        val outRgb = FloatArray(3)
        lut.sample(0.5f, 0.5f, 0.5f, outRgb)
        assertEquals(0.5f, outRgb[0], 0.01f)
        assertEquals(0.5f, outRgb[1], 0.01f)
        assertEquals(0.5f, outRgb[2], 0.01f)
    }

    @Test
    fun testCubeLutParser_invalidData() {
        val invalidContent = """
            TITLE "Broken LUT"
            LUT_3D_SIZE 4
            0.0 0.0
        """.trimIndent()

        val stream = ByteArrayInputStream(invalidContent.toByteArray())
        val result = CubeLutParser.parse(stream)
        assertTrue(result.isFailure)
    }

    @Test
    fun testColorEngine_preservesOriginalBitmap() = runBlocking {
        // Create 10x10 test bitmap with red pixel
        val orig = Bitmap.createBitmap(10, 10, Bitmap.Config.ARGB_8888)
        orig.setPixel(0, 0, android.graphics.Color.RED)
        val origPixel = orig.getPixel(0, 0)

        val tealOrangePreset = ColorPreset.ALL.first { it.id == "preset_teal_orange" }
        val processed = ColorEngine.processImage(
            sourceBitmap = orig,
            preset = tealOrangePreset,
            lut = null,
            intensity = 100f,
            adjustments = ManualAdjustments(exposure = 20f, contrast = 15f)
        )

        // Crucial requirement: original must NEVER be modified or destroyed
        assertEquals(origPixel, orig.getPixel(0, 0))
        assertNotNull(processed)
        assertEquals(orig.width, processed.width)
        assertEquals(orig.height, processed.height)
    }

    @Test
    fun testColorEngine_zeroIntensityReturnsOriginalValues() = runBlocking {
        val orig = Bitmap.createBitmap(4, 4, Bitmap.Config.ARGB_8888)
        val testColor = android.graphics.Color.rgb(120, 140, 180)
        orig.setPixel(0, 0, testColor)

        val cyberpunkPreset = ColorPreset.ALL.first { it.id == "preset_cyberpunk" }
        val processed = ColorEngine.processImage(
            sourceBitmap = orig,
            preset = cyberpunkPreset,
            lut = null,
            intensity = 0f, // 0% intensity
            adjustments = ManualAdjustments.DEFAULT
        )

        val processedColor = processed.getPixel(0, 0)
        assertEquals(testColor, processedColor)
    }
}
