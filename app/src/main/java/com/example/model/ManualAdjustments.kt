package com.example.model

data class ManualAdjustments(
    val exposure: Float = 0f,     // -100 to +100
    val contrast: Float = 0f,     // -100 to +100
    val saturation: Float = 0f,   // -100 to +100
    val highlights: Float = 0f,   // -100 to +100
    val shadows: Float = 0f,      // -100 to +100
    val temperature: Float = 0f,  // -100 to +100
    val tint: Float = 0f,         // -100 to +100
    val vibrance: Float = 0f,     // -100 to +100
    val fade: Float = 0f,         // 0 to 100
    val sharpen: Float = 0f       // 0 to 100
) {
    val isDefault: Boolean
        get() = exposure == 0f &&
                contrast == 0f &&
                saturation == 0f &&
                highlights == 0f &&
                shadows == 0f &&
                temperature == 0f &&
                tint == 0f &&
                vibrance == 0f &&
                fade == 0f &&
                sharpen == 0f

    companion object {
        val DEFAULT = ManualAdjustments()
    }
}
