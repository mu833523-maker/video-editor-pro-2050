package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.model.MediaType
import com.example.ui.components.*
import com.example.ui.theme.*
import com.example.viewmodel.ToolPanelType
import com.example.viewmodel.VideoEditorViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                VideoEditorStudioScreen()
            }
        }
    }
}

@Composable
fun VideoEditorStudioScreen(
    viewModel: VideoEditorViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()
    var showExportModal by remember { mutableStateOf(false) }

    // Multi-Select Gallery Picker for real Video/Photo/Audio imports
    val galleryPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetMultipleContents()
    ) { uris ->
        uris.forEachIndexed { index, uri ->
            val fileName = uri.lastPathSegment?.split("/")?.lastOrNull() ?: "Imported Media $index"
            val isAudio = uri.toString().contains("audio", ignoreCase = true)
            val isPhoto = uri.toString().contains("image", ignoreCase = true)
            val type = when {
                isAudio -> MediaType.AUDIO
                isPhoto -> MediaType.PHOTO
                else -> MediaType.VIDEO
            }
            viewModel.addImportedMedia(fileName, type, uri.toString())
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .testTag("video_editor_studio_root"),
        containerColor = DarkBackground,
        contentWindowInsets = WindowInsets.safeDrawing
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(DarkBackground)
        ) {
            // Top Studio Navigation Bar
            StudioTopBar(
                canUndo = state.canUndo,
                canRedo = state.canRedo,
                isPro = state.isProUser,
                onUndo = { viewModel.undo() },
                onRedo = { viewModel.redo() },
                onImportClick = {
                    // Launch device gallery picker for videos and photos
                    try {
                        galleryPicker.launch("*/*")
                    } catch (e: Exception) {
                        viewModel.addImportedMedia("Stock Cyber Clip 4K", MediaType.VIDEO)
                    }
                },
                onExportClick = { showExportModal = true },
                onProClick = { viewModel.setActiveToolPanel(ToolPanelType.PRO_STORE) }
            )

            // Optional Google AdMob Simulation Banner (Free Users)
            if (state.adBannerVisible && !state.isProUser) {
                AdMobBannerBar(
                    onDismiss = { viewModel.dismissAdBanner() },
                    onUpgrade = { viewModel.setActiveToolPanel(ToolPanelType.PRO_STORE) }
                )
            }

            // Live 60FPS Video Preview Canvas & Monitor
            VideoPreviewCanvas(
                aspectRatio = state.aspectRatio,
                playheadMs = state.playheadMs,
                isPlaying = state.isPlaying,
                totalDurationMs = state.totalDurationMs,
                clips = state.clips,
                overlays = state.overlays,
                captions = state.captions,
                appliedEffects = state.appliedEffects,
                colorGrading = state.colorGrading,
                showWatermark = state.showWatermark,
                onTogglePlayPause = { viewModel.togglePlayPause() },
                onSelectAspectRatio = { viewModel.setAspectRatio(it) },
                modifier = Modifier.weight(1f, fill = false)
            )

            // Timeline Transport & Scrubber Controls
            TimelineTransportControls(
                isPlaying = state.isPlaying,
                playheadMs = state.playheadMs,
                totalDurationMs = state.totalDurationMs,
                zoom = state.timelineZoom,
                onPlayPause = { viewModel.togglePlayPause() },
                onStepBackward = { viewModel.seekTo(state.playheadMs - 1000L) },
                onStepForward = { viewModel.seekTo(state.playheadMs + 1000L) },
                onZoomChange = { viewModel.setTimelineZoom(it) }
            )

            // Multi-Layer Pro Timeline Tracks
            MultiTrackTimeline(
                clips = state.clips,
                overlays = state.overlays,
                audios = state.audios,
                captions = state.captions,
                effects = state.appliedEffects,
                playheadMs = state.playheadMs,
                totalDurationMs = state.totalDurationMs,
                zoom = state.timelineZoom,
                selectedClipId = state.selectedClipId,
                onSeek = { viewModel.seekTo(it) },
                onSelectClip = { viewModel.selectClip(it) },
                onSplitClip = { viewModel.splitCurrentClip() },
                onDeleteClip = { viewModel.deleteSelectedClip() },
                onDuplicateClip = { viewModel.duplicateSelectedClip() },
                onOpenTransitions = { viewModel.setActiveToolPanel(ToolPanelType.TRANSITIONS) },
                modifier = Modifier.weight(1f)
            )

            // Active Tool Panel (Slides up when a tool category is chosen)
            ToolPanelContainer(
                viewModel = viewModel,
                activePanel = state.activeToolPanel,
                onClose = { viewModel.closeToolPanel() }
            )

            // Bottom Master Toolbar
            ToolbarControls(
                activePanel = state.activeToolPanel,
                onSelectPanel = { viewModel.setActiveToolPanel(it) }
            )

            // Status Bar
            Surface(
                color = DarkSurface,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(22.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "⚡ ${state.statusMessage}",
                        color = CyberCyan,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = "ENGINE: 60FPS HARDWARE ACCEL",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Export Dialog
        if (showExportModal || state.isExporting || state.exportedVideoReady) {
            ExportModalDialog(
                viewModel = viewModel,
                onDismiss = {
                    showExportModal = false
                    viewModel.dismissExport()
                }
            )
        }

        // AdMob Interstitial Dialog
        if (state.adInterstitialVisible && !state.isProUser) {
            AdMobInterstitialDialog(
                onDismiss = { viewModel.dismissInterstitialAd() },
                onUpgrade = { viewModel.setActiveToolPanel(ToolPanelType.PRO_STORE) }
            )
        }
    }
}

@Composable
private fun StudioTopBar(
    canUndo: Boolean,
    canRedo: Boolean,
    isPro: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    onImportClick: () -> Unit,
    onExportClick: () -> Unit,
    onProClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(52.dp),
        color = DarkSurface,
        border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // App Branding Title
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(listOf(CyberCyan, CyberMagenta))
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Text("🎬", fontSize = 14.sp)
                }
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "PRO 2050",
                            color = TextPrimary,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Black,
                            letterSpacing = 0.5.sp
                        )
                        if (isPro) {
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(CyberGold)
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text("VIP", color = DarkBackground, fontSize = 8.sp, fontWeight = FontWeight.Black)
                            }
                        }
                    }
                    Text(
                        text = "CAPCUT & VN EVOLVED",
                        color = CyberCyan,
                        fontSize = 8.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Undo / Redo Actions
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                IconButton(
                    onClick = onUndo,
                    enabled = canUndo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Undo,
                        contentDescription = "Undo",
                        tint = if (canUndo) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }

                IconButton(
                    onClick = onRedo,
                    enabled = canRedo,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Redo,
                        contentDescription = "Redo",
                        tint = if (canRedo) TextPrimary else TextMuted,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            // Primary Action Buttons: Import, VIP Pro, 4K Export
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                // Import Media Button
                Button(
                    onClick = onImportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = DarkSurfaceElevated),
                    contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberCyan.copy(alpha = 0.4f)),
                    modifier = Modifier.height(32.dp).testTag("import_button")
                ) {
                    Icon(imageVector = Icons.Default.AddPhotoAlternate, contentDescription = "Import", tint = CyberCyan, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("+ Import", color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }

                // Export 4K Button
                Button(
                    onClick = onExportClick,
                    colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.height(32.dp).testTag("export_button")
                ) {
                    Icon(imageVector = Icons.Default.CloudUpload, contentDescription = "Export", tint = DarkBackground, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Export", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
private fun TimelineTransportControls(
    isPlaying: Boolean,
    playheadMs: Long,
    totalDurationMs: Long,
    zoom: Float,
    onPlayPause: () -> Unit,
    onStepBackward: () -> Unit,
    onStepForward: () -> Unit,
    onZoomChange: (Float) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(38.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(0.5.dp, GlassBorder)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Playback Transport buttons
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Replay,
                    contentDescription = "-1s",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onStepBackward() }
                )

                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (isPlaying) CyberMagenta else CyberCyan)
                        .clickable { onPlayPause() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                        contentDescription = if (isPlaying) "Pause" else "Play",
                        tint = DarkBackground,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Icon(
                    imageVector = Icons.Default.Forward10,
                    contentDescription = "+1s",
                    tint = TextSecondary,
                    modifier = Modifier
                        .size(20.dp)
                        .clickable { onStepForward() }
                )
            }

            // Timeline Zoom Slider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Icon(imageVector = Icons.Default.ZoomOut, contentDescription = "Zoom Out", tint = TextMuted, modifier = Modifier.size(16.dp))
                Slider(
                    value = zoom,
                    onValueChange = onZoomChange,
                    valueRange = 0.5f..3.0f,
                    modifier = Modifier.width(100.dp),
                    colors = SliderDefaults.colors(thumbColor = CyberCyan, activeTrackColor = CyberCyan)
                )
                Icon(imageVector = Icons.Default.ZoomIn, contentDescription = "Zoom In", tint = TextMuted, modifier = Modifier.size(16.dp))
            }
        }
    }
}

// Preserve Greeting for tests compatibility
@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(text = "Hello $name!", modifier = modifier, color = TextPrimary)
}
