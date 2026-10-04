package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Compare
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary

@Composable
fun TopBar(
    hasPhoto: Boolean,
    fileName: String,
    imageWidth: Int,
    imageHeight: Int,
    isSplitActive: Boolean,
    isAdjustOpen: Boolean,
    onToggleSplit: () -> Unit,
    onToggleAdjust: () -> Unit,
    onPickGallery: () -> Unit,
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface)
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // App Branding
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Sleek circular color ring icon
                Row(
                    modifier = Modifier
                        .size(16.dp)
                        .clip(CircleShape)
                        .background(AccentTeal)
                ) {
                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .background(AccentOrange)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Text(
                        text = "AMAN COLOR LAB",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.8.sp
                        ),
                        color = StudioTextPrimary
                    )
                    if (hasPhoto && imageWidth > 0 && imageHeight > 0) {
                        Text(
                            text = "$fileName • ${imageWidth}×${imageHeight}px",
                            style = MaterialTheme.typography.bodySmall.copy(fontSize = 11.sp),
                            color = StudioTextMuted
                        )
                    }
                }
            }

            // Quick actions if photo loaded
            if (hasPhoto) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Split view comparison toggle
                    IconButton(
                        onClick = onToggleSplit,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("split_view_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Compare,
                            contentDescription = "Split Comparison",
                            tint = if (isSplitActive) AccentTeal else StudioTextSecondary
                        )
                    }

                    // Adjustments toggle
                    IconButton(
                        onClick = onToggleAdjust,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("adjust_panel_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Tune,
                            contentDescription = "Manual Adjustments",
                            tint = if (isAdjustOpen) AccentTeal else StudioTextSecondary
                        )
                    }

                    // Change photo
                    IconButton(
                        onClick = onPickGallery,
                        modifier = Modifier
                            .size(40.dp)
                            .testTag("change_photo_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.AddPhotoAlternate,
                            contentDescription = "Select Another Photo",
                            tint = StudioTextSecondary
                        )
                    }
                }
            }
        }
    }
}
