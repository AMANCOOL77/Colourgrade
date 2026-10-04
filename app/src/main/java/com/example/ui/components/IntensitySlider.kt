package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import kotlin.math.roundToInt

@Composable
fun IntensitySlider(
    intensity: Float,
    onIntensityChange: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface)
            .padding(horizontal = 16.dp, vertical = 2.dp)
            .testTag("intensity_slider_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "INTENSITY",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp,
                    color = StudioTextPrimary
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "${intensity.roundToInt()}%",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentTeal,
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(DarkStudioSurfaceVariant)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                )

                if (intensity != 100f) {
                    Text(
                        text = "100%",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = StudioTextMuted,
                        modifier = Modifier
                            .padding(start = 8.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(DarkStudioBorder)
                            .clickable { onIntensityChange(100f) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Slider(
            value = intensity,
            onValueChange = onIntensityChange,
            valueRange = 0f..100f,
            colors = SliderDefaults.colors(
                thumbColor = AccentTeal,
                activeTrackColor = AccentTeal,
                inactiveTrackColor = DarkStudioBorder
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag("intensity_slider")
        )
    }
}
