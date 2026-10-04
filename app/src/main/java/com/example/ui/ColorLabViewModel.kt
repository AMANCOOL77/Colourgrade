package com.example.ui

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.engine.ColorEngine
import com.example.engine.CubeLutParser
import com.example.model.ColorPreset
import com.example.model.CubeLut
import com.example.model.ManualAdjustments
import com.example.storage.ImageDecoder
import com.example.storage.MediaStoreExporter
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.InputStream

data class EditorUiState(
    val hasPhoto: Boolean = false,
    val sourceUri: Uri? = null,
    val fileName: String = "",
    val originalWidth: Int = 0,
    val originalHeight: Int = 0,
    val activePreset: ColorPreset = ColorPreset.ALL[0],
    val activeLut: CubeLut? = null,
    val intensity: Float = 100f,
    val adjustments: ManualAdjustments = ManualAdjustments.DEFAULT,
    val isProcessing: Boolean = false,
    val isComparingBefore: Boolean = false,
    val isSplitViewActive: Boolean = false,
    val splitPosition: Float = 0.5f,
    val importedLuts: List<CubeLut> = emptyList(),
    val showAdjustPanel: Boolean = false,
    val showExportDialog: Boolean = false,
    val isExporting: Boolean = false,
    val exportProgressMessage: String? = null,
    val userNotice: String? = null,
    val selectedTab: EditorTab = EditorTab.BUILT_IN
)

enum class EditorTab {
    BUILT_IN,
    MY_LUTS
}

class ColorLabViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditorUiState())
    val uiState: StateFlow<EditorUiState> = _uiState.asStateFlow()

    // Persistent Bitmaps
    var originalPreviewBitmap: Bitmap? = null
        private set

    private var _processedBitmapState = MutableStateFlow<Bitmap?>(null)
    val processedBitmapState: StateFlow<Bitmap?> = _processedBitmapState.asStateFlow()

    // Thumbnail cache for presets
    private val _presetThumbnails = MutableStateFlow<Map<String, Bitmap>>(emptyMap())
    val presetThumbnails: StateFlow<Map<String, Bitmap>> = _presetThumbnails.asStateFlow()

    private var renderJob: Job? = null
    private var thumbnailJob: Job? = null

    /**
     * Loads a photo selected via SAF or Camera.
     */
    fun loadPhoto(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true, userNotice = null) }
            val context = getApplication<Application>().applicationContext
            val result = ImageDecoder.decodeForPreview(context, uri)

            result.fold(
                onSuccess = { decoded ->
                    originalPreviewBitmap = decoded.previewBitmap
                    _processedBitmapState.value = decoded.previewBitmap
                    _uiState.update {
                        it.copy(
                            hasPhoto = true,
                            sourceUri = uri,
                            fileName = decoded.fileName,
                            originalWidth = decoded.originalWidth,
                            originalHeight = decoded.originalHeight,
                            activePreset = ColorPreset.ALL[0],
                            activeLut = null,
                            intensity = 100f,
                            adjustments = ManualAdjustments.DEFAULT,
                            isProcessing = false,
                            isComparingBefore = false
                        )
                    }
                    generateThumbnails(decoded.previewBitmap)
                },
                onFailure = { error ->
                    val errorMsg = error.message ?: "Failed to open image."
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            userNotice = errorMsg
                        )
                    }
                }
            )
        }
    }

    /**
     * Select a built-in color grade preset.
     */
    fun selectPreset(preset: ColorPreset) {
        if (_uiState.value.activePreset.id == preset.id && _uiState.value.activeLut == null) return
        _uiState.update {
            it.copy(
                activePreset = preset,
                activeLut = null,
                selectedTab = EditorTab.BUILT_IN
            )
        }
        triggerRender()
    }

    /**
     * Select an imported .cube LUT.
     */
    fun selectLut(lut: CubeLut) {
        if (_uiState.value.activeLut?.id == lut.id) return
        _uiState.update {
            it.copy(
                activeLut = lut,
                selectedTab = EditorTab.MY_LUTS
            )
        }
        triggerRender()
    }

    /**
     * Update color grade intensity (0..100%).
     */
    fun updateIntensity(intensity: Float) {
        val clamped = intensity.coerceIn(0f, 100f)
        _uiState.update { it.copy(intensity = clamped) }
        triggerRender(debounceMs = 15)
    }

    /**
     * Update manual adjustments.
     */
    fun updateAdjustments(adjustments: ManualAdjustments) {
        _uiState.update { it.copy(adjustments = adjustments) }
        triggerRender(debounceMs = 20)
    }

    /**
     * Reset manual adjustments only.
     */
    fun resetAdjustments() {
        _uiState.update { it.copy(adjustments = ManualAdjustments.DEFAULT) }
        triggerRender()
    }

    /**
     * Full reset to original photo (clears preset, LUT, intensity, adjustments).
     * Never deletes the photo!
     */
    fun resetAll() {
        val original = originalPreviewBitmap ?: return
        _uiState.update {
            it.copy(
                activePreset = ColorPreset.ALL[0],
                activeLut = null,
                intensity = 100f,
                adjustments = ManualAdjustments.DEFAULT,
                isComparingBefore = false,
                isSplitViewActive = false
            )
        }
        _processedBitmapState.value = original
    }

    /**
     * Before / After controls.
     */
    fun setComparingBefore(isComparing: Boolean) {
        _uiState.update { it.copy(isComparingBefore = isComparing) }
    }

    fun toggleSplitView() {
        _uiState.update { it.copy(isSplitViewActive = !it.isSplitViewActive) }
    }

    fun setSplitPosition(position: Float) {
        _uiState.update { it.copy(splitPosition = position.coerceIn(0.05f, 0.95f)) }
    }

    fun setAdjustPanelVisible(visible: Boolean) {
        _uiState.update { it.copy(showAdjustPanel = visible) }
    }

    fun setSelectedTab(tab: EditorTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun dismissNotice() {
        _uiState.update { it.copy(userNotice = null) }
    }

    /**
     * Import a .cube LUT file from InputStream.
     */
    fun importCubeLut(inputStream: InputStream, displayName: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isProcessing = true) }
            val result = CubeLutParser.parse(inputStream, displayName)
            result.fold(
                onSuccess = { lut ->
                    _uiState.update {
                        it.copy(
                            importedLuts = it.importedLuts + lut,
                            activeLut = lut,
                            selectedTab = EditorTab.MY_LUTS,
                            isProcessing = false,
                            userNotice = "Imported LUT: ${lut.name}"
                        )
                    }
                    triggerRender()
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            userNotice = error.message ?: "Invalid or unsupported LUT file."
                        )
                    }
                }
            )
        }
    }

    /**
     * Triggers asynchronous rendering from persistent originalPreviewBitmap.
     * Ensures preview never blanks or disappears on error or cancellation.
     */
    private fun triggerRender(debounceMs: Long = 0) {
        val original = originalPreviewBitmap ?: return
        renderJob?.cancel()

        renderJob = viewModelScope.launch {
            if (debounceMs > 0) {
                delay(debounceMs)
            }
            _uiState.update { it.copy(isProcessing = true) }

            try {
                val currentState = _uiState.value
                val rendered = ColorEngine.processImage(
                    sourceBitmap = original,
                    preset = currentState.activePreset,
                    lut = currentState.activeLut,
                    intensity = currentState.intensity,
                    adjustments = currentState.adjustments
                )
                // Successfully rendered: update processed bitmap
                _processedBitmapState.value = rendered
                _uiState.update { it.copy(isProcessing = false) }
            } catch (_: CancellationException) {
                // Ignore cancellation from newer slider updates
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        userNotice = "Processing warning: ${e.localizedMessage ?: "Using fallback render"}"
                    )
                }
            }
        }
    }

    /**
     * Pre-renders small thumbnails for all built-in presets.
     */
    private fun generateThumbnails(previewBitmap: Bitmap) {
        thumbnailJob?.cancel()
        thumbnailJob = viewModelScope.launch(Dispatchers.Default) {
            val thumbSize = 80
            val aspect = previewBitmap.width.toFloat() / previewBitmap.height.toFloat()
            val tw = if (aspect >= 1f) (thumbSize * aspect).toInt() else thumbSize
            val th = if (aspect >= 1f) thumbSize else (thumbSize / aspect).toInt()

            val smallScaled = try {
                Bitmap.createScaledBitmap(previewBitmap, tw, th, true)
            } catch (_: Exception) {
                return@launch
            }

            val map = mutableMapOf<String, Bitmap>()
            for (preset in ColorPreset.ALL) {
                try {
                    val graded = if (preset.id == "preset_original") {
                        smallScaled
                    } else {
                        ColorEngine.processImage(
                            sourceBitmap = smallScaled,
                            preset = preset,
                            lut = null,
                            intensity = 100f,
                            adjustments = ManualAdjustments.DEFAULT
                        )
                    }
                    map[preset.id] = graded
                } catch (_: Exception) { }
            }
            _presetThumbnails.value = map
        }
    }

    // Export controls
    fun openExportDialog() {
        _uiState.update { it.copy(showExportDialog = true) }
    }

    fun dismissExportDialog() {
        _uiState.update { it.copy(showExportDialog = false, isExporting = false) }
    }

    fun executeExport(format: MediaStoreExporter.ExportFormat, quality: Int) {
        val uri = _uiState.value.sourceUri ?: return
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isExporting = true,
                    exportProgressMessage = "Loading full resolution photograph..."
                )
            }
            val context = getApplication<Application>().applicationContext

            val fullResResult = ImageDecoder.decodeFullResolution(context, uri)
            val fullBitmap = fullResResult.getOrElse {
                // If decoding full resolution fails, fallback safely to previewBitmap
                originalPreviewBitmap
            }

            if (fullBitmap == null) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        showExportDialog = false,
                        userNotice = "Failed to load photograph for export."
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(exportProgressMessage = "Applying color grade at full resolution...") }

            val currentState = _uiState.value
            val gradedFull = try {
                ColorEngine.processImage(
                    sourceBitmap = fullBitmap,
                    preset = currentState.activePreset,
                    lut = currentState.activeLut,
                    intensity = currentState.intensity,
                    adjustments = currentState.adjustments
                )
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isExporting = false,
                        showExportDialog = false,
                        userNotice = "Export processing failed: ${e.localizedMessage}"
                    )
                }
                return@launch
            }

            _uiState.update { it.copy(exportProgressMessage = "Saving to Pictures/AmanColorLab...") }

            val saveResult = MediaStoreExporter.exportPhoto(context, gradedFull, format, quality)
            saveResult.fold(
                onSuccess = { savedUri ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            showExportDialog = false,
                            userNotice = "Photo saved successfully"
                        )
                    }
                },
                onFailure = { error ->
                    _uiState.update {
                        it.copy(
                            isExporting = false,
                            showExportDialog = false,
                            userNotice = "Export failed: ${error.localizedMessage ?: "Storage error"}"
                        )
                    }
                }
            )
        }
    }
}
