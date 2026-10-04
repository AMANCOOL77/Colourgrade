package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.model.ColorPreset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Aman Color Lab", appName)
    }

    @Test
    fun `verify all 12 presets are present`() {
        assertEquals(12, ColorPreset.ALL.size)
        val names = ColorPreset.ALL.map { it.name }
        assertTrue(names.contains("Original"))
        assertTrue(names.contains("Natural / Clean"))
        assertTrue(names.contains("Hollywood Travel"))
        assertTrue(names.contains("Cinematic Portrait"))
        assertTrue(names.contains("Dark Blockbuster"))
        assertTrue(names.contains("Golden Hour Movie"))
        assertTrue(names.contains("Sunset Epic"))
        assertTrue(names.contains("Blue Hour Cinema"))
        assertTrue(names.contains("Night Street"))
        assertTrue(names.contains("Cyberpunk Night"))
        assertTrue(names.contains("Clean 2050"))
        assertTrue(names.contains("Future Noir"))
    }
}
