package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FilterVintage
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.ColorPreset
import com.example.model.CubeLut
import com.example.ui.EditorTab
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun PresetThumbnailStrip(
    selectedTab: EditorTab,
    onTabSelected: (EditorTab) -> Unit,
    activePreset: ColorPreset,
    activeLut: CubeLut?,
    importedLuts: List<CubeLut>,
    presetThumbnails: Map<String, Bitmap>,
    onSelectPreset: (ColorPreset) -> Unit,
    onSelectLut: (CubeLut) -> Unit,
    onImportLutClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface)
            .padding(vertical = 6.dp)
    ) {
        // Section Header Tabs: Built-in vs My LUTs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                TabHeaderItem(
                    title = "Built-in (${ColorPreset.ALL.size})",
                    isSelected = selectedTab == EditorTab.BUILT_IN,
                    onClick = { onTabSelected(EditorTab.BUILT_IN) },
                    testTag = "tab_built_in"
                )
                TabHeaderItem(
                    title = "My LUTs (${importedLuts.size})",
                    isSelected = selectedTab == EditorTab.MY_LUTS,
                    onClick = { onTabSelected(EditorTab.MY_LUTS) },
                    testTag = "tab_my_luts"
                )
            }

            if (selectedTab == EditorTab.MY_LUTS) {
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(DarkStudioSurfaceVariant)
                        .clickable { onImportLutClick() }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                        .testTag("import_lut_button_strip"),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Import LUT",
                        tint = AccentTeal,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Import .cube",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = AccentTeal
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Horizontal Strip
        when (selectedTab) {
            EditorTab.BUILT_IN -> {
                LazyRow(
                    contentPadding = PaddingValues(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("presets_horizontal_strip")
                ) {
                    items(ColorPreset.ALL, key = { it.id }) { preset ->
                        val isSelected = activePreset.id == preset.id && activeLut == null
                        val thumbnail = presetThumbnails[preset.id]

                        PresetCard(
                            name = preset.name,
                            isSelected = isSelected,
                            thumbnail = thumbnail,
                            fallbackGradient = getPresetFallbackGradient(preset.id),
                            onClick = { onSelectPreset(preset) },
                            testTag = "preset_${preset.id}"
                        )
                    }
                }
            }

            EditorTab.MY_LUTS -> {
                if (importedLuts.isEmpty()) {
                    // Empty State Card
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkStudioSurfaceVariant)
                            .clickable { onImportLutClick() }
                            .padding(14.dp)
                            .testTag("empty_luts_card"),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(DarkStudioBorder),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.FolderOpen,
                                contentDescription = null,
                                tint = AccentTeal,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Import Custom 3D LUT",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = StudioTextPrimary
                            )
                            Text(
                                text = "Supports standard .cube files (17x, 33x, 65x)",
                                fontSize = 11.sp,
                                color = StudioTextMuted
                            )
                        }
                    }
                } else {
                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("my_luts_horizontal_strip")
                    ) {
                        items(importedLuts, key = { it.id }) { lut ->
                            val isSelected = activeLut?.id == lut.id
                            LutCard(
                                name = lut.name,
                                lutSize = "${lut.size}³",
                                isSelected = isSelected,
                                onClick = { onSelectLut(lut) },
                                testTag = "lut_${lut.id}"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TabHeaderItem(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        modifier = Modifier
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
            ),
            color = if (isSelected) StudioTextPrimary else StudioTextMuted
        )
        Spacer(modifier = Modifier.height(4.dp))
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(2.dp)
                .background(if (isSelected) AccentTeal else Color.Transparent)
        )
    }
}

@Composable
private fun PresetCard(
    name: String,
    isSelected: Boolean,
    thumbnail: Bitmap?,
    fallbackGradient: Brush,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(72.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AccentTeal else DarkStudioBorder,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            if (thumbnail != null) {
                val imgBitmap = remember(thumbnail) { thumbnail.asImageBitmap() }
                Image(
                    bitmap = imgBitmap,
                    contentDescription = name,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.size(64.dp)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(fallbackGradient)
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(20.dp)
                        .clip(CircleShape)
                        .background(AccentTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = DarkStudioSurface,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentTeal else StudioTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun LutCard(
    name: String,
    lutSize: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    testTag: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(76.dp)
            .clickable { onClick() }
            .testTag(testTag)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkStudioSurfaceVariant)
                .border(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) AccentTeal else DarkStudioBorder,
                    shape = RoundedCornerShape(8.dp)
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(
                    imageVector = Icons.Default.FilterVintage,
                    contentDescription = null,
                    tint = if (isSelected) AccentTeal else StudioTextMuted,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = lutSize,
                    fontSize = 9.sp,
                    color = StudioTextMuted,
                    fontWeight = FontWeight.Bold
                )
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                        .size(14.dp)
                        .clip(CircleShape)
                        .background(AccentTeal),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Active",
                        tint = DarkStudioSurface,
                        modifier = Modifier.size(10.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = name,
            fontSize = 10.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = if (isSelected) AccentTeal else StudioTextSecondary,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = TextAlign.Center
        )
    }
}

private fun getPresetFallbackGradient(id: String): Brush {
    return when (id) {
        "preset_clean" -> Brush.linearGradient(listOf(Color(0xFFE2E8F0), Color(0xFF94A3B8)))
        "preset_teal_orange" -> Brush.linearGradient(listOf(Color(0xFF0284C7), Color(0xFFF97316)))
        "preset_portrait" -> Brush.linearGradient(listOf(Color(0xFFFDBA74), Color(0xFFFB7185)))
        "preset_dark_blockbuster" -> Brush.linearGradient(listOf(Color(0xFF0F172A), Color(0xFF1E293B)))
        "preset_golden_hour" -> Brush.linearGradient(listOf(Color(0xFFF59E0B), Color(0xFFEA580C)))
        "preset_sunset_epic" -> Brush.linearGradient(listOf(Color(0xFFEC4899), Color(0xFF8B5CF6)))
        "preset_blue_hour" -> Brush.linearGradient(listOf(Color(0xFF1E3A8A), Color(0xFF3B82F6)))
        "preset_night_street" -> Brush.linearGradient(listOf(Color(0xFF111827), Color(0xFFFBBF24)))
        "preset_cyberpunk" -> Brush.linearGradient(listOf(Color(0xFFD946EF), Color(0xFF06B6D4)))
        "preset_clean_2050" -> Brush.linearGradient(listOf(Color(0xFFF1F5F9), Color(0xFF38BDF8)))
        "preset_future_noir" -> Brush.linearGradient(listOf(Color(0xFF334155), Color(0xFF0F172A)))
        else -> Brush.linearGradient(listOf(Color(0xFF475569), Color(0xFF1E293B)))
    }
}
