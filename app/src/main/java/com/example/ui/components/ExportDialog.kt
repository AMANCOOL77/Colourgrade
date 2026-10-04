package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.HighQuality
import androidx.compose.material.icons.filled.PhotoSizeSelectActual
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.storage.MediaStoreExporter
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun ExportDialog(
    isOpen: Boolean,
    isExporting: Boolean,
    exportProgressMessage: String?,
    imageWidth: Int,
    imageHeight: Int,
    onDismiss: () -> Unit,
    onConfirmExport: (format: MediaStoreExporter.ExportFormat, quality: Int) -> Unit
) {
    if (!isOpen) return

    var selectedFormat by remember { mutableStateOf(MediaStoreExporter.ExportFormat.JPEG) }
    var selectedQuality by remember { mutableIntStateOf(95) }

    AlertDialog(
        onDismissRequest = {
            if (!isExporting) onDismiss()
        },
        containerColor = DarkStudioSurface,
        title = {
            Column {
                Text(
                    text = "EXPORT PHOTOGRAPH",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp
                    ),
                    color = StudioTextPrimary
                )
                if (imageWidth > 0 && imageHeight > 0) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.PhotoSizeSelectActual,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Preserving original: ${imageWidth} × ${imageHeight} px",
                            fontSize = 12.sp,
                            color = StudioTextMuted
                        )
                    }
                }
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (isExporting) {
                    // Export Progress State
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 24.dp)
                    ) {
                        CircularProgressIndicator(
                            color = AccentTeal,
                            strokeWidth = 3.dp,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = exportProgressMessage ?: "Processing full resolution photo...",
                            fontSize = 13.sp,
                            color = StudioTextPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                } else {
                    // 1. Format Selection
                    Text(
                        text = "FILE FORMAT",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = StudioTextSecondary,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        FormatOption(
                            label = "JPEG",
                            description = "Standard • High efficiency",
                            isSelected = selectedFormat == MediaStoreExporter.ExportFormat.JPEG,
                            onClick = { selectedFormat = MediaStoreExporter.ExportFormat.JPEG },
                            modifier = Modifier.weight(1f),
                            testTag = "export_format_jpeg"
                        )
                        FormatOption(
                            label = "PNG",
                            description = "Lossless • Studio quality",
                            isSelected = selectedFormat == MediaStoreExporter.ExportFormat.PNG,
                            onClick = { selectedFormat = MediaStoreExporter.ExportFormat.PNG },
                            modifier = Modifier.weight(1f),
                            testTag = "export_format_png"
                        )
                    }

                    // 2. JPEG Quality Selector
                    if (selectedFormat == MediaStoreExporter.ExportFormat.JPEG) {
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = "JPEG QUALITY",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = StudioTextSecondary,
                            letterSpacing = 1.sp
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            listOf(80, 90, 95, 100).forEach { quality ->
                                val isSelected = selectedQuality == quality
                                QualityPill(
                                    quality = quality,
                                    isDefault = quality == 95,
                                    isSelected = isSelected,
                                    onClick = { selectedQuality = quality },
                                    modifier = Modifier.weight(1f),
                                    testTag = "quality_$quality"
                                )
                            }
                        }
                    }

                    // 3. Destination Directory
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(DarkStudioSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Folder,
                            contentDescription = null,
                            tint = AccentTeal,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Pictures / AmanColorLab",
                            fontSize = 11.sp,
                            color = StudioTextSecondary
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (!isExporting) {
                Button(
                    onClick = { onConfirmExport(selectedFormat, selectedQuality) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentTeal,
                        contentColor = Color(0xFF0F172A)
                    ),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("confirm_export_button")
                ) {
                    Text("Save Photo", fontWeight = FontWeight.Bold)
                }
            }
        },
        dismissButton = {
            if (!isExporting) {
                OutlinedButton(
                    onClick = onDismiss,
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("cancel_export_button")
                ) {
                    Text("Cancel", color = StudioTextSecondary)
                }
            }
        }
    )
}

@Composable
private fun FormatOption(
    label: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(if (isSelected) AccentTeal.copy(alpha = 0.15f) else DarkStudioSurfaceVariant)
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) AccentTeal else DarkStudioBorder,
                shape = RoundedCornerShape(8.dp)
            )
            .clickable { onClick() }
            .padding(12.dp)
            .testTag(testTag)
    ) {
        Column {
            Text(
                text = label,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) AccentTeal else StudioTextPrimary
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                fontSize = 10.sp,
                color = StudioTextMuted
            )
        }
    }
}

@Composable
private fun QualityPill(
    quality: Int,
    isDefault: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    testTag: String
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) AccentTeal else DarkStudioSurfaceVariant)
            .border(
                width = 1.dp,
                color = if (isSelected) AccentTeal else DarkStudioBorder,
                shape = RoundedCornerShape(6.dp)
            )
            .clickable { onClick() }
            .padding(vertical = 8.dp)
            .testTag(testTag),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "$quality%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isSelected) Color(0xFF0F172A) else StudioTextPrimary
            )
            if (isDefault) {
                Text(
                    text = "Default",
                    fontSize = 9.sp,
                    color = if (isSelected) Color(0xFF0F172A).copy(alpha = 0.7f) else StudioTextMuted
                )
            }
        }
    }
}
