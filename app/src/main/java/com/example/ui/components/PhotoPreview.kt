package com.example.ui.components

import android.graphics.Bitmap
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextPrimary
import kotlin.math.roundToInt

@Composable
fun PhotoPreview(
    originalBitmap: Bitmap?,
    processedBitmap: Bitmap?,
    isProcessing: Boolean,
    isComparingBefore: Boolean,
    isSplitViewActive: Boolean,
    splitPosition: Float,
    onSplitPositionChange: (Float) -> Unit,
    onStartHoldBefore: () -> Unit,
    onReleaseHoldBefore: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkStudioBg)
            .testTag("photo_preview_container"),
        contentAlignment = Alignment.Center
    ) {
        val displayBitmap = if (isComparingBefore) originalBitmap else (processedBitmap ?: originalBitmap)

        if (displayBitmap != null) {
            val imageBitmap = remember(displayBitmap) { displayBitmap.asImageBitmap() }

            if (isSplitViewActive && originalBitmap != null && processedBitmap != null) {
                // Split Screen Comparison View
                val origImageBitmap = remember(originalBitmap) { originalBitmap.asImageBitmap() }
                val procImageBitmap = remember(processedBitmap) { processedBitmap.asImageBitmap() }

                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .clipToBounds()
                        .pointerInput(Unit) {
                            detectDragGestures { change, _ ->
                                change.consume()
                                val newPos = (change.position.x / size.width).coerceIn(0.05f, 0.95f)
                                onSplitPositionChange(newPos)
                            }
                        }
                ) {
                    val canvasWidth = constraints.maxWidth.toFloat()
                    val canvasHeight = constraints.maxHeight.toFloat()
                    val splitX = canvasWidth * splitPosition

                    // Canvas drawing for split-screen comparison
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        val srcWidth = origImageBitmap.width.toFloat()
                        val srcHeight = origImageBitmap.height.toFloat()

                        // Calculate fit center destination rectangle
                        val scale = minOf(size.width / srcWidth, size.height / srcHeight)
                        val dstWidth = srcWidth * scale
                        val dstHeight = srcHeight * scale
                        val dstLeft = (size.width - dstWidth) / 2f
                        val dstTop = (size.height - dstHeight) / 2f
                        val dstRect = Rect(dstLeft, dstTop, dstLeft + dstWidth, dstTop + dstHeight)

                        // 1. Draw Processed image fully
                        drawImage(
                            image = procImageBitmap,
                            dstOffset = IntOffset(dstLeft.roundToInt(), dstTop.roundToInt()),
                            dstSize = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt())
                        )

                        // 2. Clip left region to show Original (Before)
                        clipRect(left = 0f, top = 0f, right = splitX, bottom = size.height) {
                            drawImage(
                                image = origImageBitmap,
                                dstOffset = IntOffset(dstLeft.roundToInt(), dstTop.roundToInt()),
                                dstSize = IntSize(dstWidth.roundToInt(), dstHeight.roundToInt())
                            )
                        }

                        // 3. Draw vertical divider line
                        drawLine(
                            color = Color.White,
                            start = Offset(splitX, dstTop),
                            end = Offset(splitX, dstTop + dstHeight),
                            strokeWidth = 3.dp.toPx()
                        )

                        // 4. Draw center handle circle
                        drawCircle(
                            color = Color.White,
                            radius = 14.dp.toPx(),
                            center = Offset(splitX, dstTop + dstHeight / 2f)
                        )
                        drawCircle(
                            color = Color(0xFF1E293B),
                            radius = 10.dp.toPx(),
                            center = Offset(splitX, dstTop + dstHeight / 2f)
                        )
                    }

                    // Split labels
                    Text(
                        text = "BEFORE",
                        color = Color.White.copy(alpha = 0.8f),
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopStart)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                    Text(
                        text = "AFTER",
                        color = AccentTeal,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(16.dp)
                            .background(Color.Black.copy(alpha = 0.6f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            } else {
                // Standard Single Preview with Press-and-Hold Before/After detection
                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        bitmap = imageBitmap,
                        contentDescription = "Graded photograph preview",
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )

                    // Before indicator badge
                    AnimatedVisibility(
                        visible = isComparingBefore,
                        enter = fadeIn(),
                        exit = fadeOut(),
                        modifier = Modifier
                            .align(Alignment.TopCenter)
                            .padding(top = 16.dp)
                    ) {
                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(20.dp),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.3f))
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(Color.Yellow)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "ORIGINAL (BEFORE)",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold,
                                    letterSpacing = 1.sp,
                                    color = Color.White
                                )
                            }
                        }
                    }
                }
            }
        }

        // Floating "Processing..." status pill
        AnimatedVisibility(
            visible = isProcessing,
            enter = fadeIn(),
            exit = fadeOut(),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
        ) {
            Surface(
                color = DarkStudioSurfaceVariant.copy(alpha = 0.9f),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, AccentTeal.copy(alpha = 0.5f))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = AccentTeal,
                        modifier = Modifier.size(12.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Processing...",
                        color = StudioTextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}
