package com.example.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.model.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.UUID

enum class ToolPanelType {
    NONE,
    EDIT_TOOLS,
    SPEED,
    FILTERS,
    COLOR_GRADE,
    EFFECTS,
    TEXT_CAPTIONS,
    AUDIO_STUDIO,
    OVERLAY_PIP,
    TRANSITIONS,
    CHROMA_KEY,
    EXPORT,
    PRO_STORE,
    FLUTTER_CODE
}

data class EditorStateSnapshot(
    val clips: List<VideoClip>,
    val overlays: List<OverlayItem>,
    val audios: List<AudioItem>,
    val captions: List<TextCaption>,
    val appliedEffects: List<AppliedEffect>,
    val colorGrading: ColorGradingSettings
)

data class VideoEditorUiState(
    val clips: List<VideoClip> = MediaStock.defaultClips,
    val overlays: List<OverlayItem> = listOf(
        OverlayItem(
            id = "ov_1",
            title = "Cyber 2050",
            type = OverlayType.STICKER,
            assetContent = "🚀",
            startMs = 1000L,
            endMs = 7000L,
            posX = 0.65f,
            posY = -0.6f,
            scale = 1.3f
        )
    ),
    val audios: List<AudioItem> = listOf(MediaStock.stockMusic.first()),
    val captions: List<TextCaption> = MediaStock.autoCaptionsPreset,
    val appliedEffects: List<AppliedEffect> = listOf(
        AppliedEffect("eff_1", EffectType.GLITCH, 0.6f, 0L, 8000L)
    ),
    val colorGrading: ColorGradingSettings = ColorGradingSettings(),
    val activeFilterId: String = "cyber_0",
    val aspectRatio: AspectRatio = AspectRatio.RATIO_16_9,
    val playheadMs: Long = 0L,
    val isPlaying: Boolean = false,
    val timelineZoom: Float = 1.0f,
    val selectedClipId: String? = "stock_1",
    val activeToolPanel: ToolPanelType = ToolPanelType.NONE,
    val isProUser: Boolean = false,
    val showWatermark: Boolean = true,
    val exportConfig: ExportConfig = ExportConfig(),
    val isExporting: Boolean = false,
    val exportProgress: Float = 0f,
    val exportEtaSeconds: Int = 0,
    val exportedVideoReady: Boolean = false,
    val adBannerVisible: Boolean = true,
    val adInterstitialVisible: Boolean = false,
    val canUndo: Boolean = false,
    val canRedo: Boolean = false,
    val statusMessage: String = "Ready to edit at 60 FPS"
) {
    val totalDurationMs: Long
        get() = clips.sumOf { it.effectiveDurationMs }.coerceAtLeast(1000L)

    val currentClip: VideoClip?
        get() = clips.find { it.id == selectedClipId } ?: clips.firstOrNull()
}

class VideoEditorViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(VideoEditorUiState())
    val uiState: StateFlow<VideoEditorUiState> = _uiState.asStateFlow()

    private var playbackJob: Job? = null
    private val undoStack = mutableListOf<EditorStateSnapshot>()
    private val redoStack = mutableListOf<EditorStateSnapshot>()

    init {
        saveSnapshot()
    }

    private fun saveSnapshot() {
        val s = _uiState.value
        val snapshot = EditorStateSnapshot(
            clips = s.clips,
            overlays = s.overlays,
            audios = s.audios,
            captions = s.captions,
            appliedEffects = s.appliedEffects,
            colorGrading = s.colorGrading
        )
        undoStack.add(snapshot)
        if (undoStack.size > 20) undoStack.removeAt(0)
        redoStack.clear()
        updateUndoRedoStatus()
    }

    private fun updateUndoRedoStatus() {
        _uiState.update {
            it.copy(
                canUndo = undoStack.size > 1,
                canRedo = redoStack.isNotEmpty()
            )
        }
    }

    fun undo() {
        if (undoStack.size <= 1) return
        val current = undoStack.removeAt(undoStack.lastIndex)
        redoStack.add(current)
        val target = undoStack.last()
        _uiState.update {
            it.copy(
                clips = target.clips,
                overlays = target.overlays,
                audios = target.audios,
                captions = target.captions,
                appliedEffects = target.appliedEffects,
                colorGrading = target.colorGrading,
                statusMessage = "Undo performed"
            )
        }
        updateUndoRedoStatus()
    }

    fun redo() {
        if (redoStack.isEmpty()) return
        val next = redoStack.removeAt(redoStack.lastIndex)
        undoStack.add(next)
        _uiState.update {
            it.copy(
                clips = next.clips,
                overlays = next.overlays,
                audios = next.audios,
                captions = next.captions,
                appliedEffects = next.appliedEffects,
                colorGrading = next.colorGrading,
                statusMessage = "Redo performed"
            )
        }
        updateUndoRedoStatus()
    }

    fun togglePlayPause() {
        if (_uiState.value.isPlaying) {
            pause()
        } else {
            play()
        }
    }

    fun play() {
        playbackJob?.cancel()
        _uiState.update { it.copy(isPlaying = true) }
        playbackJob = viewModelScope.launch {
            val frameDelayMs = 16L // ~60fps
            while (isActive) {
                delay(frameDelayMs)
                _uiState.update { state ->
                    val next = state.playheadMs + (frameDelayMs * 1.5f).toLong()
                    if (next >= state.totalDurationMs) {
                        state.copy(playheadMs = 0L, isPlaying = true)
                    } else {
                        state.copy(playheadMs = next)
                    }
                }
            }
        }
    }

    fun pause() {
        playbackJob?.cancel()
        playbackJob = null
        _uiState.update { it.copy(isPlaying = false) }
    }

    fun seekTo(positionMs: Long) {
        val bounded = positionMs.coerceIn(0L, _uiState.value.totalDurationMs)
        _uiState.update { it.copy(playheadMs = bounded) }
    }

    fun setAspectRatio(ratio: AspectRatio) {
        _uiState.update { it.copy(aspectRatio = ratio, statusMessage = "Canvas set to ${ratio.label}") }
    }

    fun setTimelineZoom(zoom: Float) {
        _uiState.update { it.copy(timelineZoom = zoom.coerceIn(0.5f, 4.0f)) }
    }

    fun selectClip(id: String) {
        _uiState.update { it.copy(selectedClipId = id) }
    }

    fun setActiveToolPanel(panel: ToolPanelType) {
        _uiState.update {
            it.copy(activeToolPanel = if (it.activeToolPanel == panel) ToolPanelType.NONE else panel)
        }
    }

    fun closeToolPanel() {
        _uiState.update { it.copy(activeToolPanel = ToolPanelType.NONE) }
    }

    // 1. IMPORT
    fun addImportedMedia(name: String, type: MediaType, uri: String? = null) {
        saveSnapshot()
        val newClip = VideoClip(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = name,
            type = type,
            uri = uri,
            colorHex = when(type) {
                MediaType.VIDEO -> 0xFF0284C7
                MediaType.PHOTO -> 0xFFD97706
                MediaType.GIF -> 0xFFDB2777
                MediaType.AUDIO -> 0xFF059669
            },
            durationMs = 5000L,
            trimStartMs = 0L,
            trimEndMs = 5000L
        )
        _uiState.update {
            it.copy(
                clips = it.clips + newClip,
                selectedClipId = newClip.id,
                statusMessage = "Imported: $name"
            )
        }
    }

    // 3. EDIT TOOLS: Cut/Split, Trim, Delete, Duplicate, Reverse, Freeze
    fun splitCurrentClip() {
        val state = _uiState.value
        val clip = state.currentClip ?: return
        saveSnapshot()

        // Find time inside clip
        var accumulated = 0L
        var targetIndex = -1
        for (i in state.clips.indices) {
            val c = state.clips[i]
            val dur = c.effectiveDurationMs
            if (state.playheadMs >= accumulated && state.playheadMs < accumulated + dur) {
                targetIndex = i
                break
            }
            accumulated += dur
        }

        if (targetIndex == -1) targetIndex = state.clips.indexOfFirst { it.id == clip.id }
        if (targetIndex == -1) return

        val targetClip = state.clips[targetIndex]
        val localOffset = (state.playheadMs - accumulated).coerceIn(500L, targetClip.effectiveDurationMs - 500L)
        val splitMs = targetClip.trimStartMs + (localOffset * targetClip.speed).toLong()

        val part1 = targetClip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = "${targetClip.name} Pt1",
            trimEndMs = splitMs
        )
        val part2 = targetClip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = "${targetClip.name} Pt2",
            trimStartMs = splitMs
        )

        val updated = state.clips.toMutableList()
        updated.removeAt(targetIndex)
        updated.add(targetIndex, part1)
        updated.add(targetIndex + 1, part2)

        _uiState.update {
            it.copy(clips = updated, selectedClipId = part2.id, statusMessage = "Split clip into 2 segments")
        }
    }

    fun trimSelectedClip(startMs: Long, endMs: Long) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) {
                    c.copy(
                        trimStartMs = startMs.coerceAtLeast(0L),
                        trimEndMs = endMs.coerceAtLeast(startMs + 500L)
                    )
                } else c
            }
            state.copy(clips = updated, statusMessage = "Clip trimmed")
        }
    }

    fun deleteSelectedClip() {
        val clipId = _uiState.value.selectedClipId ?: return
        if (_uiState.value.clips.size <= 1) {
            _uiState.update { it.copy(statusMessage = "Cannot delete the only remaining clip") }
            return
        }
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.filterNot { it.id == clipId }
            state.copy(
                clips = updated,
                selectedClipId = updated.firstOrNull()?.id,
                statusMessage = "Clip deleted"
            )
        }
    }

    fun duplicateSelectedClip() {
        val clip = _uiState.value.currentClip ?: return
        saveSnapshot()
        val copy = clip.copy(
            id = "clip_" + UUID.randomUUID().toString().take(6),
            name = "${clip.name} (Copy)"
        )
        val idx = _uiState.value.clips.indexOfFirst { it.id == clip.id }
        val updated = _uiState.value.clips.toMutableList()
        updated.add(idx + 1, copy)
        _uiState.update {
            it.copy(clips = updated, selectedClipId = copy.id, statusMessage = "Clip duplicated")
        }
    }

    fun toggleReverseSelectedClip() {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) c.copy(isReversed = !c.isReversed) else c
            }
            state.copy(clips = updated, statusMessage = "Reverse playback toggled")
        }
    }

    fun freezeCurrentFrame() {
        val clip = _uiState.value.currentClip ?: return
        saveSnapshot()
        val freezeClip = clip.copy(
            id = "freeze_" + UUID.randomUUID().toString().take(6),
            name = "${clip.name} [Freeze 2s]",
            type = MediaType.PHOTO,
            durationMs = 2000L,
            trimStartMs = 0L,
            trimEndMs = 2000L,
            isFrozen = true
        )
        val idx = _uiState.value.clips.indexOfFirst { it.id == clip.id }
        val updated = _uiState.value.clips.toMutableList()
        updated.add(idx + 1, freezeClip)
        _uiState.update {
            it.copy(clips = updated, selectedClipId = freezeClip.id, statusMessage = "Inserted 2-second freeze frame")
        }
    }

    // 4. SPEED & VELOCITY CURVE
    fun setClipSpeed(speed: Float) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) c.copy(speed = speed.coerceIn(0.1f, 10.0f)) else c
            }
            state.copy(clips = updated, statusMessage = "Speed set to ${String.format("%.1fx", speed)}")
        }
    }

    fun setClipSpeedCurvePreset(preset: SpeedCurvePreset) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) c.copy(speedCurvePreset = preset) else c
            }
            state.copy(clips = updated, statusMessage = "Velocity curve: ${preset.displayName}")
        }
    }

    // 5. FILTERS & COLOR GRADING
    fun applyFilter(filterId: String) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) c.copy(filterId = filterId) else c
            }
            state.copy(
                clips = updated,
                activeFilterId = filterId,
                statusMessage = "LUT applied: $filterId"
            )
        }
    }

    fun updateColorGrading(grading: ColorGradingSettings) {
        _uiState.update { it.copy(colorGrading = grading) }
    }

    // 6. EFFECTS & KEYFRAMING
    fun addEffect(type: EffectType) {
        saveSnapshot()
        val newEff = AppliedEffect(
            id = "eff_" + UUID.randomUUID().toString().take(5),
            effectType = type,
            intensity = 0.75f,
            startMs = _uiState.value.playheadMs,
            endMs = (_uiState.value.playheadMs + 5000L).coerceAtMost(_uiState.value.totalDurationMs)
        )
        _uiState.update {
            it.copy(
                appliedEffects = it.appliedEffects + newEff,
                statusMessage = "Added effect: ${type.label}"
            )
        }
    }

    fun removeEffect(id: String) {
        saveSnapshot()
        _uiState.update {
            it.copy(
                appliedEffects = it.appliedEffects.filterNot { e -> e.id == id },
                statusMessage = "Effect removed"
            )
        }
    }

    fun addKeyframeToSelectedClip() {
        val clip = _uiState.value.currentClip ?: return
        saveSnapshot()
        val newKey = KeyframePoint(
            timeMs = _uiState.value.playheadMs,
            scale = 1.25f,
            rotation = 5f
        )
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clip.id) c.copy(keyframes = c.keyframes + newKey) else c
            }
            state.copy(clips = updated, statusMessage = "Keyframe added at ${state.playheadMs}ms")
        }
    }

    // 7. TEXT & AUTO CAPTIONS
    fun generateAiAutoCaptions() {
        saveSnapshot()
        _uiState.update {
            it.copy(
                captions = MediaStock.autoCaptionsPreset,
                statusMessage = "AI Auto Captions generated with Karaoke timing!"
            )
        }
    }

    fun addTextCaption(text: String, style: TextStylePreset) {
        saveSnapshot()
        val newCap = TextCaption(
            id = "cap_" + UUID.randomUUID().toString().take(6),
            text = text,
            startMs = _uiState.value.playheadMs,
            endMs = (_uiState.value.playheadMs + 3500L).coerceAtMost(_uiState.value.totalDurationMs),
            style = style
        )
        _uiState.update {
            it.copy(
                captions = it.captions + newCap,
                statusMessage = "Text overlay added"
            )
        }
    }

    fun deleteCaption(id: String) {
        saveSnapshot()
        _uiState.update {
            it.copy(captions = it.captions.filterNot { c -> c.id == id })
        }
    }

    // 8. AUDIO: Music, SFX, Voice Over, Beat Detection
    fun addMusicTrack(music: AudioItem) {
        saveSnapshot()
        _uiState.update {
            it.copy(
                audios = it.audios + music.copy(id = "music_" + UUID.randomUUID().toString().take(5)),
                statusMessage = "Added audio track: ${music.title}"
            )
        }
    }

    fun addSfx(title: String) {
        saveSnapshot()
        val sfx = AudioItem(
            id = "sfx_" + UUID.randomUUID().toString().take(5),
            title = title,
            artist = "Sound FX 2050",
            startMs = _uiState.value.playheadMs,
            endMs = _uiState.value.playheadMs + 1500L,
            volume = 0.9f
        )
        _uiState.update {
            it.copy(audios = it.audios + sfx, statusMessage = "SFX added: $title")
        }
    }

    fun recordVoiceOver(recordedDurationMs: Long = 3000L) {
        saveSnapshot()
        val vo = AudioItem(
            id = "vo_" + UUID.randomUUID().toString().take(5),
            title = "Mic Voice-Over",
            artist = "Studio Microphone",
            startMs = _uiState.value.playheadMs,
            endMs = _uiState.value.playheadMs + recordedDurationMs,
            isVoiceOver = true,
            volume = 1.0f
        )
        _uiState.update {
            it.copy(audios = it.audios + vo, statusMessage = "Voice-over recorded successfully")
        }
    }

    fun extractAudioFromVideo() {
        val clip = _uiState.value.currentClip ?: return
        saveSnapshot()
        val extracted = AudioItem(
            id = "ext_" + UUID.randomUUID().toString().take(5),
            title = "${clip.name} [Extracted Audio]",
            artist = "Original Track",
            startMs = 0L,
            endMs = clip.effectiveDurationMs,
            volume = 1.0f
        )
        _uiState.update {
            it.copy(audios = it.audios + extracted, statusMessage = "Extracted audio layer")
        }
    }

    // 9. OVERLAYS & PIP
    fun addOverlay(title: String, type: OverlayType, assetContent: String) {
        saveSnapshot()
        val ov = OverlayItem(
            id = "ov_" + UUID.randomUUID().toString().take(6),
            title = title,
            type = type,
            assetContent = assetContent,
            startMs = _uiState.value.playheadMs,
            endMs = (_uiState.value.playheadMs + 4000L).coerceAtMost(_uiState.value.totalDurationMs)
        )
        _uiState.update {
            it.copy(overlays = it.overlays + ov, statusMessage = "Added overlay: $title")
        }
    }

    fun deleteOverlay(id: String) {
        saveSnapshot()
        _uiState.update {
            it.copy(overlays = it.overlays.filterNot { o -> o.id == id })
        }
    }

    // 10. TRANSITIONS
    fun setClipTransition(transition: TransitionType) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) c.copy(transition = transition) else c
            }
            state.copy(clips = updated, statusMessage = "Transition set: ${transition.displayName}")
        }
    }

    // 11. GREEN SCREEN / CHROMA KEY
    fun updateChromaKey(
        enabled: Boolean,
        targetColor: Long = 0xFF00FF00,
        tolerance: Float = 0.45f,
        softness: Float = 0.2f,
        despill: Float = 0.3f
    ) {
        val clipId = _uiState.value.selectedClipId ?: return
        saveSnapshot()
        _uiState.update { state ->
            val updated = state.clips.map { c ->
                if (c.id == clipId) {
                    c.copy(
                        chromaKeyEnabled = enabled,
                        chromaTargetColor = targetColor,
                        chromaTolerance = tolerance,
                        chromaSoftness = softness,
                        chromaDespill = despill
                    )
                } else c
            }
            state.copy(clips = updated, statusMessage = if (enabled) "Chroma key active" else "Chroma key disabled")
        }
    }

    // 12. EXPORT SYSTEM
    fun updateExportConfig(config: ExportConfig) {
        _uiState.update { it.copy(exportConfig = config) }
    }

    fun startExport() {
        if (!_uiState.value.isProUser && _uiState.value.exportConfig.resolution.isProOnly) {
            _uiState.update {
                it.copy(
                    activeToolPanel = ToolPanelType.PRO_STORE,
                    statusMessage = "4K / 2K requires Pro upgrade or Rewarded Ad"
                )
            }
            return
        }

        pause()
        _uiState.update {
            it.copy(
                isExporting = true,
                exportProgress = 0f,
                exportEtaSeconds = 6,
                exportedVideoReady = false,
                statusMessage = "Rendering multi-track video..."
            )
        }

        viewModelScope.launch {
            for (step in 1..20) {
                delay(300L)
                val p = step / 20f
                val remaining = ((1f - p) * 6).toInt()
                _uiState.update {
                    it.copy(
                        exportProgress = p,
                        exportEtaSeconds = remaining
                    )
                }
            }
            _uiState.update {
                it.copy(
                    isExporting = false,
                    exportedVideoReady = true,
                    adInterstitialVisible = !it.isProUser,
                    statusMessage = "Export Complete! Saved to Gallery."
                )
            }
        }
    }

    fun dismissExport() {
        _uiState.update { it.copy(exportedVideoReady = false, isExporting = false) }
    }

    // MONETIZATION: AdMob & In-App Purchase
    fun purchaseProLifetime() {
        _uiState.update {
            it.copy(
                isProUser = true,
                showWatermark = false,
                adBannerVisible = false,
                adInterstitialVisible = false,
                activeToolPanel = ToolPanelType.NONE,
                statusMessage = "VIP Pro $4.99 Unlocked! All Features Available."
            )
        }
    }

    fun watchRewardedAdFor4K() {
        viewModelScope.launch {
            _uiState.update { it.copy(statusMessage = "Loading Rewarded Ad...") }
            delay(1500L)
            _uiState.update {
                it.copy(
                    showWatermark = false,
                    statusMessage = "Rewarded Ad watched! 4K Export & No Watermark unlocked for 24 hours."
                )
            }
        }
    }

    fun dismissInterstitialAd() {
        _uiState.update { it.copy(adInterstitialVisible = false) }
    }

    fun dismissAdBanner() {
        _uiState.update { it.copy(adBannerVisible = false) }
    }

    fun toggleWatermark() {
        if (!_uiState.value.isProUser) {
            _uiState.update {
                it.copy(
                    activeToolPanel = ToolPanelType.PRO_STORE,
                    statusMessage = "Removing watermark requires Pro upgrade or Rewarded Ad"
                )
            }
            return
        }
        _uiState.update { it.copy(showWatermark = !it.showWatermark) }
    }
}
