package com.example.ui

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FilterDrama
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.FileProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.storage.ImageDecoder
import com.example.ui.components.AdjustmentsPanel
import com.example.ui.components.BottomActionBar
import com.example.ui.components.ExportDialog
import com.example.ui.components.IntensitySlider
import com.example.ui.components.PhotoPreview
import com.example.ui.components.PresetThumbnailStrip
import com.example.ui.components.TopBar
import com.example.ui.theme.AccentAmber
import com.example.ui.theme.AccentOrange
import com.example.ui.theme.AccentTeal
import com.example.ui.theme.DarkStudioBg
import com.example.ui.theme.DarkStudioBorder
import com.example.ui.theme.DarkStudioSurface
import com.example.ui.theme.DarkStudioSurfaceVariant
import com.example.ui.theme.StudioTextMuted
import com.example.ui.theme.StudioTextPrimary
import com.example.ui.theme.StudioTextSecondary
import java.io.File

@Composable
fun AmanColorLabApp(
    viewModel: ColorLabViewModel = viewModel()
) {
    val context = LocalContext.current
    val uiState by viewModel.uiState.collectAsState()
    val processedBitmap by viewModel.processedBitmapState.collectAsState()
    val thumbnails by viewModel.presetThumbnails.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }

    // Display user messages / alerts
    LaunchedEffect(uiState.userNotice) {
        uiState.userNotice?.let { msg ->
            snackbarHostState.showSnackbar(msg)
            viewModel.dismissNotice()
        }
    }

    // 1. Photo Picker Launcher (Gallery / Files via SAF)
    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            viewModel.loadPhoto(uri)
        }
    }

    // 2. Camera Capture Launcher
    var cameraTempUri by remember { mutableStateOf<Uri?>(null) }
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success && cameraTempUri != null) {
            viewModel.loadPhoto(cameraTempUri!!)
        }
    }

    fun launchCamera() {
        try {
            val cacheFile = File(context.cacheDir, "camera_capture_${System.currentTimeMillis()}.jpg")
            val authority = "${context.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(context, authority, cacheFile)
            cameraTempUri = uri
            cameraLauncher.launch(uri)
        } catch (_: Exception) {
            // Fallback or error handled gracefully
        }
    }

    // 3. LUT File Picker Launcher (SAF OpenDocument for .cube files)
    val lutPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            val fileName = ImageDecoder.queryFileName(context, uri) ?: "imported.cube"
            context.contentResolver.openInputStream(uri)?.let { stream ->
                viewModel.importCubeLut(stream, fileName)
            }
        }
    }

    Scaffold(
        containerColor = DarkStudioBg,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopBar(
                hasPhoto = uiState.hasPhoto,
                fileName = uiState.fileName,
                imageWidth = uiState.originalWidth,
                imageHeight = uiState.originalHeight,
                isSplitActive = uiState.isSplitViewActive,
                isAdjustOpen = uiState.showAdjustPanel,
                onToggleSplit = { viewModel.toggleSplitView() },
                onToggleAdjust = { viewModel.setAdjustPanelVisible(!uiState.showAdjustPanel) },
                onPickGallery = { photoPickerLauncher.launch(arrayOf("image/*", "*/*")) },
                onLaunchCamera = { launchCamera() }
            )
        },
        bottomBar = {
            if (uiState.hasPhoto) {
                BottomActionBar(
                    isComparingBefore = uiState.isComparingBefore,
                    onStartHoldBefore = { viewModel.setComparingBefore(true) },
                    onReleaseHoldBefore = { viewModel.setComparingBefore(false) },
                    onImportLut = { lutPickerLauncher.launch(arrayOf("*/*")) },
                    onReset = { viewModel.resetAll() },
                    onExport = { viewModel.openExportDialog() }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            if (!uiState.hasPhoto) {
                // First Screen: Welcome & Photo Selection
                FirstScreen(
                    onSelectPhoto = { photoPickerLauncher.launch(arrayOf("image/*", "*/*")) },
                    onLaunchCamera = { launchCamera() }
                )
            } else {
                // Main Editor View
                Column(modifier = Modifier.fillMaxSize()) {
                    // Large Photo Preview
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        PhotoPreview(
                            originalBitmap = viewModel.originalPreviewBitmap,
                            processedBitmap = processedBitmap,
                            isProcessing = uiState.isProcessing,
                            isComparingBefore = uiState.isComparingBefore,
                            isSplitViewActive = uiState.isSplitViewActive,
                            splitPosition = uiState.splitPosition,
                            onSplitPositionChange = { viewModel.setSplitPosition(it) },
                            onStartHoldBefore = { viewModel.setComparingBefore(true) },
                            onReleaseHoldBefore = { viewModel.setComparingBefore(false) }
                        )
                    }

                    // Collapsible Adjustments Panel
                    AdjustmentsPanel(
                        isVisible = uiState.showAdjustPanel,
                        adjustments = uiState.adjustments,
                        onAdjustmentsChange = { viewModel.updateAdjustments(it) },
                        onResetAdjustments = { viewModel.resetAdjustments() },
                        onClose = { viewModel.setAdjustPanelVisible(false) }
                    )

                    // Intensity Slider
                    IntensitySlider(
                        intensity = uiState.intensity,
                        onIntensityChange = { viewModel.updateIntensity(it) }
                    )

                    // Preset & LUT Strip
                    PresetThumbnailStrip(
                        selectedTab = uiState.selectedTab,
                        onTabSelected = { viewModel.setSelectedTab(it) },
                        activePreset = uiState.activePreset,
                        activeLut = uiState.activeLut,
                        importedLuts = uiState.importedLuts,
                        presetThumbnails = thumbnails,
                        onSelectPreset = { viewModel.selectPreset(it) },
                        onSelectLut = { viewModel.selectLut(it) },
                        onImportLutClick = { lutPickerLauncher.launch(arrayOf("*/*")) }
                    )
                }
            }

            // Export Dialog
            ExportDialog(
                isOpen = uiState.showExportDialog,
                isExporting = uiState.isExporting,
                exportProgressMessage = uiState.exportProgressMessage,
                imageWidth = uiState.originalWidth,
                imageHeight = uiState.originalHeight,
                onDismiss = { viewModel.dismissExportDialog() },
                onConfirmExport = { format, quality ->
                    viewModel.executeExport(format, quality)
                }
            )
        }
    }
}

@Composable
private fun FirstScreen(
    onSelectPhoto: () -> Unit,
    onLaunchCamera: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // App Aperture Symbol
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(DarkStudioSurfaceVariant)
                .border(2.dp, AccentTeal.copy(alpha = 0.6f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.FilterDrama,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(36.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "AMAN COLOR LAB",
            style = MaterialTheme.typography.headlineMedium.copy(
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp
            ),
            color = StudioTextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Color grade your photographs with cinematic presets and LUTs.",
            style = MaterialTheme.typography.bodyMedium,
            color = StudioTextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Action Buttons: Select Photo & Camera
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = onSelectPhoto,
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AccentTeal,
                    contentColor = Color(0xFF0F172A)
                ),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("select_photo_button")
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Select Photo",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            }

            OutlinedButton(
                onClick = onLaunchCamera,
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, DarkStudioBorder),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(vertical = 14.dp),
                modifier = Modifier
                    .weight(1f)
                    .testTag("camera_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CameraAlt,
                    contentDescription = null,
                    tint = StudioTextPrimary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Camera",
                    fontWeight = FontWeight.SemiBold,
                    color = StudioTextPrimary,
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Supported: JPEG • PNG • DNG where available",
            fontSize = 11.sp,
            color = StudioTextMuted
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Professional Studio Specs Card
        Surface(
            color = DarkStudioSurface,
            shape = RoundedCornerShape(12.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, DarkStudioBorder),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                FeatureRow(
                    icon = Icons.Default.Layers,
                    title = "Non-Destructive Grading",
                    description = "Original pixel data remains pristine at all times"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Tune,
                    title = "3D .cube LUT Engine",
                    description = "Trilinear interpolated color matrices (17x, 33x, 65x)"
                )
                Spacer(modifier = Modifier.height(12.dp))
                FeatureRow(
                    icon = Icons.Default.Security,
                    title = "100% On-Device Processing",
                    description = "No internet required. Photographs never leave your phone"
                )
            }
        }
    }
}

@Composable
private fun FeatureRow(
    icon: ImageVector,
    title: String,
    description: String
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(DarkStudioSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AccentTeal,
                modifier = Modifier.size(16.dp)
            )
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(
                text = title,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = StudioTextPrimary
            )
            Text(
                text = description,
                fontSize = 10.sp,
                color = StudioTextMuted
            )
        }
    }
}
