package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FileDownload
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
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
import com.example.ui.theme.StudioTextSecondary

@Composable
fun BottomActionBar(
    isComparingBefore: Boolean,
    onStartHoldBefore: () -> Unit,
    onReleaseHoldBefore: () -> Unit,
    onImportLut: () -> Unit,
    onReset: () -> Unit,
    onExport: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = DarkStudioSurface,
        tonalElevation = 6.dp,
        modifier = modifier
            .fillMaxWidth()
            .background(DarkStudioSurface)
            .navigationBarsPadding()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Import LUT
            BottomActionButton(
                label = "Import LUT",
                icon = Icons.Default.FileUpload,
                onClick = onImportLut,
                testTag = "bottom_action_import_lut"
            )

            // 2. Before / After (Hold to see original)
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isComparingBefore) AccentTeal.copy(alpha = 0.2f) else DarkStudioSurfaceVariant)
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onPress = {
                                onStartHoldBefore()
                                try {
                                    tryAwaitRelease()
                                } finally {
                                    onReleaseHoldBefore()
                                }
                            }
                        )
                    }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
                    .testTag("bottom_action_before_after"),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Default.Visibility,
                        contentDescription = "Hold for Before",
                        tint = if (isComparingBefore) AccentTeal else StudioTextSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (isComparingBefore) "Original" else "Before/After",
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium,
                        color = if (isComparingBefore) AccentTeal else StudioTextSecondary
                    )
                }
            }

            // 3. Reset
            BottomActionButton(
                label = "Reset",
                icon = Icons.Default.Refresh,
                onClick = onReset,
                testTag = "bottom_action_reset"
            )

            // 4. Export (Prominent CTA Button)
            Button(
                onClick = onExport,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTeal,
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp, vertical = 10.dp),
                modifier = Modifier.testTag("bottom_action_export")
            ) {
                Icon(
                    imageVector = Icons.Default.FileDownload,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Export",
                    fontWeight = FontWeight.Bold,
                    fontSize = 13.sp
                )
            }
        }
    }
}

@Composable
private fun BottomActionButton(
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
    testTag: String
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(8.dp),
        color = DarkStudioSurfaceVariant,
        modifier = Modifier.testTag(testTag)
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = StudioTextSecondary,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium,
                color = StudioTextSecondary
            )
        }
    }
}
