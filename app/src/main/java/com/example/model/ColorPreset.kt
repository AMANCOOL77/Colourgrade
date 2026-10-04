package com.example.model

/**
 * Built-in cinematic color grade definitions.
 * Each preset specifies tone curve transformations, color balance,
 * shadow/highlight tints, temperature/tint shifts, and channel weights.
 */
data class ColorPreset(
    val id: String,
    val name: String,
    val description: String,
    val exposureBias: Float = 0f,
    val contrast: Float = 1.0f,
    val saturation: Float = 1.0f,
    val temperature: Float = 0f,  // -100 to +100 (Cool to Warm)
    val tint: Float = 0f,         // -100 to +100 (Green to Magenta)
    val highlights: Float = 0f,   // -100 to +100
    val shadows: Float = 0f,      // -100 to +100
    val shadowTintR: Float = 0f,  // -1f to 1f
    val shadowTintG: Float = 0f,
    val shadowTintB: Float = 0f,
    val highlightTintR: Float = 0f,
    val highlightTintG: Float = 0f,
    val highlightTintB: Float = 0f,
    val curveLift: Float = 0f,    // Black point lift (film toe/matte fade)
    val curveCrush: Float = 0f,   // White point compression
    val vibrance: Float = 0f
) {
    companion object {
        val ALL: List<ColorPreset> = listOf(
            ColorPreset(
                id = "preset_original",
                name = "Original",
                description = "Standard unedited photograph"
            ),
            ColorPreset(
                id = "preset_clean",
                name = "Natural / Clean",
                description = "Pristine dynamic range with gentle contrast and clear skin tones",
                exposureBias = 0.05f,
                contrast = 1.08f,
                saturation = 1.05f,
                highlights = -10f,
                shadows = 15f,
                vibrance = 10f
            ),
            ColorPreset(
                id = "preset_teal_orange",
                name = "Hollywood Travel",
                description = "Iconic blockbuster teal shadows and warm amber skin tones",
                contrast = 1.22f,
                saturation = 1.15f,
                temperature = 12f,
                shadowTintR = -0.15f,
                shadowTintG = 0.05f,
                shadowTintB = 0.22f,
                highlightTintR = 0.22f,
                highlightTintG = 0.08f,
                highlightTintB = -0.16f,
                highlights = -12f,
                shadows = 8f
            ),
            ColorPreset(
                id = "preset_portrait",
                name = "Cinematic Portrait",
                description = "Flattering portrait roll-off, soft highlights, and gentle film contrast",
                contrast = 1.06f,
                saturation = 0.96f,
                temperature = 8f,
                tint = 4f,
                highlights = -18f,
                shadows = 12f,
                highlightTintR = 0.10f,
                highlightTintG = 0.04f,
                highlightTintB = -0.04f,
                shadowTintR = 0.02f,
                shadowTintG = 0.02f,
                shadowTintB = 0.06f,
                curveLift = 0.03f
            ),
            ColorPreset(
                id = "preset_dark_blockbuster",
                name = "Dark Blockbuster",
                description = "Moody, deep shadows with desaturated midtones and intense atmosphere",
                contrast = 1.35f,
                saturation = 0.85f,
                temperature = -12f,
                tint = -5f,
                shadows = -25f,
                highlights = -8f,
                shadowTintR = -0.10f,
                shadowTintG = 0.02f,
                shadowTintB = 0.18f,
                highlightTintR = 0.05f,
                highlightTintG = 0.02f,
                highlightTintB = 0.08f,
                curveLift = 0.02f
            ),
            ColorPreset(
                id = "preset_golden_hour",
                name = "Golden Hour Movie",
                description = "Lush warm sunlight wash, rich bronzed glow, and filmic warmth",
                contrast = 1.14f,
                saturation = 1.12f,
                temperature = 36f,
                tint = 6f,
                highlights = -15f,
                shadows = 10f,
                highlightTintR = 0.25f,
                highlightTintG = 0.12f,
                highlightTintB = -0.18f,
                shadowTintR = 0.12f,
                shadowTintG = 0.04f,
                shadowTintB = -0.06f
            ),
            ColorPreset(
                id = "preset_sunset_epic",
                name = "Sunset Epic",
                description = "Vivid dusk twilight with intense coral highlights and purple-tinted shadows",
                contrast = 1.25f,
                saturation = 1.26f,
                temperature = 22f,
                tint = 18f,
                highlights = -20f,
                shadows = -5f,
                highlightTintR = 0.28f,
                highlightTintG = 0.06f,
                highlightTintB = -0.05f,
                shadowTintR = 0.08f,
                shadowTintG = -0.06f,
                shadowTintB = 0.20f
            ),
            ColorPreset(
                id = "preset_blue_hour",
                name = "Blue Hour Cinema",
                description = "Melancholy sapphire twilight, cool ambient tones, and soft silver highlights",
                contrast = 1.16f,
                saturation = 0.92f,
                temperature = -38f,
                tint = -8f,
                highlights = -10f,
                shadows = 12f,
                shadowTintR = -0.18f,
                shadowTintG = -0.04f,
                shadowTintB = 0.28f,
                highlightTintR = -0.08f,
                highlightTintG = 0.02f,
                highlightTintB = 0.15f
            ),
            ColorPreset(
                id = "preset_night_street",
                name = "Night Street",
                description = "Deep blacks, vibrant neon streetlights, and punchy urban contrast",
                contrast = 1.38f,
                saturation = 1.18f,
                temperature = -10f,
                shadows = -30f,
                highlights = 8f,
                shadowTintR = -0.05f,
                shadowTintG = 0.02f,
                shadowTintB = 0.12f,
                highlightTintR = 0.16f,
                highlightTintG = 0.12f,
                highlightTintB = -0.04f
            ),
            ColorPreset(
                id = "preset_cyberpunk",
                name = "Cyberpunk Night",
                description = "Electric neon magenta highlights with moody deep cobalt shadows",
                contrast = 1.32f,
                saturation = 1.35f,
                temperature = -15f,
                tint = 32f,
                shadows = -18f,
                highlights = 10f,
                highlightTintR = 0.32f,
                highlightTintG = -0.15f,
                highlightTintB = 0.25f,
                shadowTintR = -0.22f,
                shadowTintG = -0.05f,
                shadowTintB = 0.32f
            ),
            ColorPreset(
                id = "preset_clean_2050",
                name = "Clean 2050",
                description = "High-key futuristic aesthetic with crisp clinical whites and subtle cyan tint",
                exposureBias = 0.12f,
                contrast = 1.12f,
                saturation = 0.88f,
                temperature = -18f,
                tint = -10f,
                highlights = -5f,
                shadows = 25f,
                highlightTintR = -0.05f,
                highlightTintG = 0.08f,
                highlightTintB = 0.14f,
                shadowTintR = -0.08f,
                shadowTintG = 0.06f,
                shadowTintB = 0.15f,
                curveLift = 0.04f
            ),
            ColorPreset(
                id = "preset_future_noir",
                name = "Future Noir",
                description = "Monochrome-leaning stylized film noir with lifted matte blacks and cold edge",
                contrast = 1.45f,
                saturation = 0.30f,
                temperature = -14f,
                tint = -4f,
                highlights = -15f,
                shadows = -15f,
                curveLift = 0.08f,
                shadowTintR = -0.04f,
                shadowTintG = 0.02f,
                shadowTintB = 0.10f
            )
        )
    }
}
