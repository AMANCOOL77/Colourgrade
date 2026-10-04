package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ManualAdjustments
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import kotlin.math.roundToInt

@Composable
fun AdjustmentsPanel(
    isVisible: Boolean,
    adjustments: ManualAdjustments,
    onAdjustmentsChange: (ManualAdjustments) -> Unit,
    onResetAdjustments: () -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    AnimatedVisibility(
        visible = isVisible,
        enter = expandVertically() + fadeIn(),
        exit = shrinkVertically() + fadeOut(),
        modifier = modifier
    ) {
        Surface(
            color = DarkStudioSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "MANUAL ADJUSTMENTS",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.2.sp,
                            color = StudioTextPrimary
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        if (!adjustments.isDefault) {
                            Row(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(DarkStudioSurfaceVariant)
                                    .clickable { onResetAdjustments() }
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                                    .testTag("reset_adjustments_button"),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.RestartAlt,
                                    contentDescription = "Reset Adjustments",
                                    tint = StudioTextSecondary,
                                    modifier = Modifier.size(12.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = "Reset",
                                    fontSize = 11.sp,
                                    color = StudioTextSecondary,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                        }

                        IconButton(
                            onClick = onClose,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Adjustments",
                                tint = StudioTextMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(4.dp))

                // Scrollable Adjustment Sliders
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp)
                        .verticalScroll(rememberScrollState())
                ) {
                    AdjustmentItem(
                        label = "Exposure",
                        value = adjustments.exposure,
                        displayValue = String.format("%.1f EV", adjustments.exposure / 50f),
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(exposure = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(exposure = 0f)) },
                        testTag = "slider_exposure"
                    )

                    AdjustmentItem(
                        label = "Contrast",
                        value = adjustments.contrast,
                        displayValue = "${adjustments.contrast.roundToInt()}",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(contrast = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(contrast = 0f)) },
                        testTag = "slider_contrast"
                    )

                    AdjustmentItem(
                        label = "Highlights",
                        value = adjustments.highlights,
                        displayValue = "${adjustments.highlights.roundToInt()}",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(highlights = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(highlights = 0f)) },
                        testTag = "slider_highlights"
                    )

                    AdjustmentItem(
                        label = "Shadows",
                        value = adjustments.shadows,
                        displayValue = "${adjustments.shadows.roundToInt()}",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(shadows = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(shadows = 0f)) },
                        testTag = "slider_shadows"
                    )

                    AdjustmentItem(
                        label = "Temperature",
                        value = adjustments.temperature,
                        displayValue = if (adjustments.temperature > 0) "+${adjustments.temperature.roundToInt()} Warm"
                        else if (adjustments.temperature < 0) "${adjustments.temperature.roundToInt()} Cool" else "0",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(temperature = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(temperature = 0f)) },
                        activeColor = if (adjustments.temperature >= 0) AccentOrange else AccentTeal,
                        testTag = "slider_temperature"
                    )

                    AdjustmentItem(
                        label = "Tint",
                        value = adjustments.tint,
                        displayValue = if (adjustments.tint > 0) "+${adjustments.tint.roundToInt()} Mag"
                        else if (adjustments.tint < 0) "${adjustments.tint.roundToInt()} Grn" else "0",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(tint = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(tint = 0f)) },
                        activeColor = Color(0xFFE879F9),
                        testTag = "slider_tint"
                    )

                    AdjustmentItem(
                        label = "Saturation",
                        value = adjustments.saturation,
                        displayValue = "${adjustments.saturation.roundToInt()}",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(saturation = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(saturation = 0f)) },
                        testTag = "slider_saturation"
                    )

                    AdjustmentItem(
                        label = "Vibrance",
                        value = adjustments.vibrance,
                        displayValue = "${adjustments.vibrance.roundToInt()}",
                        valueRange = -100f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(vibrance = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(vibrance = 0f)) },
                        testTag = "slider_vibrance"
                    )

                    AdjustmentItem(
                        label = "Fade (Film Blacks)",
                        value = adjustments.fade,
                        displayValue = "${adjustments.fade.roundToInt()}%",
                        valueRange = 0f..100f,
                        onValueChange = { onAdjustmentsChange(adjustments.copy(fade = it)) },
                        onReset = { onAdjustmentsChange(adjustments.copy(fade = 0f)) },
                        testTag = "slider_fade"
                    )
                }
            }
        }
    }
}

@Composable
private fun AdjustmentItem(
    label: String,
    value: Float,
    displayValue: String,
    valueRange: ClosedFloatingPointRange<Float>,
    onValueChange: (Float) -> Unit,
    onReset: () -> Unit,
    activeColor: Color = AccentTeal,
    testTag: String
) {
    val isModified = value != 0f

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Label & value
        Row(
            modifier = Modifier
                .width(110.dp)
                .clickable { onReset() },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                fontSize = 11.sp,
                fontWeight = if (isModified) FontWeight.Bold else FontWeight.Normal,
                color = if (isModified) StudioTextPrimary else StudioTextSecondary
            )
            Text(
                text = displayValue,
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isModified) activeColor else StudioTextMuted
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        // Slider
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = if (isModified) activeColor else StudioTextMuted,
                activeTrackColor = if (isModified) activeColor else DarkStudioBorder,
                inactiveTrackColor = DarkStudioBorder
            ),
            modifier = Modifier
                .weight(1f)
                .testTag(testTag)
        )
    }
}
