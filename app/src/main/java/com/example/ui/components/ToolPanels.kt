package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import com.example.viewmodel.ToolPanelType
import com.example.viewmodel.VideoEditorViewModel

@Composable
fun ToolPanelContainer(
    viewModel: VideoEditorViewModel,
    activePanel: ToolPanelType,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()

    AnimatedVisibility(
        visible = activePanel != ToolPanelType.NONE,
        modifier = modifier
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 280.dp),
            color = DarkSurface,
            shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, GlassBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // Header bar of panel
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = when (activePanel) {
                            ToolPanelType.EDIT_TOOLS -> "✂️ Clip Edit Tools"
                            ToolPanelType.SPEED -> "⚡ Speed & Curve Velocity (0.1x - 10x)"
                            ToolPanelType.FILTERS -> "🎨 100+ Pro Filters & LUTs"
                            ToolPanelType.COLOR_GRADE -> "🎛️ Color Grading & Tone Curves"
                            ToolPanelType.EFFECTS -> "✨ VFX & Keyframe Studio"
                            ToolPanelType.TEXT_CAPTIONS -> "💬 AI Auto Captions & Animated Text"
                            ToolPanelType.AUDIO_STUDIO -> "🎵 Audio Studio & Sound FX"
                            ToolPanelType.OVERLAY_PIP -> "🔲 PIP Video & Animated Stickers"
                            ToolPanelType.TRANSITIONS -> "🔄 50+ Transitions"
                            ToolPanelType.CHROMA_KEY -> "🟢 Chroma Key Green Screen"
                            ToolPanelType.PRO_STORE -> "👑 Video Editor Pro VIP ($4.99)"
                            ToolPanelType.FLUTTER_CODE -> "📱 Flutter Project Source Code"
                            else -> ""
                        },
                        color = CyberCyan,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    IconButton(
                        onClick = onClose,
                        modifier = Modifier.size(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                // Panel Content based on selection
                Box(modifier = Modifier.weight(1f)) {
                    when (activePanel) {
                        ToolPanelType.EDIT_TOOLS -> EditToolsPanel(
                            clip = state.currentClip,
                            onSplit = { viewModel.splitCurrentClip() },
                            onDelete = { viewModel.deleteSelectedClip() },
                            onDuplicate = { viewModel.duplicateSelectedClip() },
                            onToggleReverse = { viewModel.toggleReverseSelectedClip() },
                            onFreeze = { viewModel.freezeCurrentFrame() },
                            onAddKeyframe = { viewModel.addKeyframeToSelectedClip() }
                        )
                        ToolPanelType.SPEED -> SpeedCurvePanel(
                            currentSpeed = state.currentClip?.speed ?: 1.0f,
                            currentPreset = state.currentClip?.speedCurvePreset ?: SpeedCurvePreset.STANDARD,
                            onSetSpeed = { viewModel.setClipSpeed(it) },
                            onSetPreset = { viewModel.setClipSpeedCurvePreset(it) }
                        )
                        ToolPanelType.FILTERS -> FilterLutPanel(
                            activeFilterId = state.activeFilterId,
                            onSelectFilter = { viewModel.applyFilter(it) }
                        )
                        ToolPanelType.COLOR_GRADE -> ColorGradingPanel(
                            settings = state.colorGrading,
                            onUpdate = { viewModel.updateColorGrading(it) }
                        )
                        ToolPanelType.EFFECTS -> EffectsPanel(
                            appliedEffects = state.appliedEffects,
                            onAddEffect = { viewModel.addEffect(it) },
                            onRemoveEffect = { viewModel.removeEffect(it) }
                        )
                        ToolPanelType.TEXT_CAPTIONS -> TextCaptionsPanel(
                            onGenerateAutoCaptions = { viewModel.generateAiAutoCaptions() },
                            onAddCustomText = { text, style -> viewModel.addTextCaption(text, style) }
                        )
                        ToolPanelType.AUDIO_STUDIO -> AudioStudioPanel(
                            onAddMusic = { viewModel.addMusicTrack(it) },
                            onAddSfx = { viewModel.addSfx(it) },
                            onRecordVo = { viewModel.recordVoiceOver() },
                            onExtractAudio = { viewModel.extractAudioFromVideo() }
                        )
                        ToolPanelType.OVERLAY_PIP -> OverlayPipPanel(
                            onAddPip = { title, type, asset -> viewModel.addOverlay(title, type, asset) }
                        )
                        ToolPanelType.TRANSITIONS -> TransitionsPanel(
                            currentTransition = state.currentClip?.transition ?: TransitionType.NONE,
                            onSelectTransition = { viewModel.setClipTransition(it) }
                        )
                        ToolPanelType.CHROMA_KEY -> ChromaKeyPanel(
                            clip = state.currentClip,
                            onUpdate = { enabled, col, tol, soft, despill ->
                                viewModel.updateChromaKey(enabled, col, tol, soft, despill)
                            }
                        )
                        ToolPanelType.PRO_STORE -> ProStorePanel(
                            isPro = state.isProUser,
                            onPurchasePro = { viewModel.purchaseProLifetime() },
                            onWatchRewardedAd = { viewModel.watchRewardedAdFor4K() }
                        )
                        ToolPanelType.FLUTTER_CODE -> FlutterCodeViewerPanel()
                        else -> Unit
                    }
                }
            }
        }
    }
}

// 1. Edit Tools
@Composable
fun EditToolsPanel(
    clip: VideoClip?,
    onSplit: () -> Unit,
    onDelete: () -> Unit,
    onDuplicate: () -> Unit,
    onToggleReverse: () -> Unit,
    onFreeze: () -> Unit,
    onAddKeyframe: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ToolActionCard("Split Clip", Icons.Default.ContentCut, CyberCyan, onSplit)
        ToolActionCard("Delete", Icons.Default.Delete, CyberRed, onDelete)
        ToolActionCard("Duplicate", Icons.Default.ContentCopy, CyberPurple, onDuplicate)
        ToolActionCard(if (clip?.isReversed == true) "Forward" else "Reverse", Icons.Default.FastRewind, CyberGold, onToggleReverse)
        ToolActionCard("Freeze Frame (2s)", Icons.Default.AcUnit, CyberCyan, onFreeze)
        ToolActionCard("Add Keyframe", Icons.Default.PinDrop, CyberMagenta, onAddKeyframe)
    }
}

@Composable
fun ToolActionCard(
    title: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(width = 100.dp, height = 90.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(DarkSurfaceElevated)
            .border(1.dp, color.copy(alpha = 0.35f), RoundedCornerShape(10.dp))
            .clickable { onClick() }
            .padding(8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = icon, contentDescription = title, tint = color, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(text = title, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// 2. Speed & Velocity Curve Panel
@Composable
fun SpeedCurvePanel(
    currentSpeed: Float,
    currentPreset: SpeedCurvePreset,
    onSetSpeed: (Float) -> Unit,
    onSetPreset: (SpeedCurvePreset) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Linear Speed: ${String.format("%.1fx", currentSpeed)}", color = TextPrimary, fontSize = 12.sp)
            Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                listOf(0.5f, 1.0f, 2.0f, 5.0f, 10.0f).forEach { spd ->
                    Text(
                        text = "${spd}x",
                        color = if (currentSpeed == spd) CyberCyan else TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(if (currentSpeed == spd) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                            .clickable { onSetSpeed(spd) }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Slider(
            value = currentSpeed,
            onValueChange = onSetSpeed,
            valueRange = 0.1f..10.0f,
            colors = SliderDefaults.colors(
                thumbColor = CyberCyan,
                activeTrackColor = CyberCyan,
                inactiveTrackColor = DarkSurfaceHighlight
            )
        )

        Text("Curve Speed (Velocity Presets):", color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SpeedCurvePreset.values().forEach { preset ->
                val isSelected = currentPreset == preset
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberMagenta.copy(alpha = 0.25f) else DarkSurfaceElevated)
                        .border(1.dp, if (isSelected) CyberMagenta else GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onSetPreset(preset) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = preset.displayName,
                        color = if (isSelected) CyberMagenta else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// 3. Filters & LUT Panel (100+ Filters)
@Composable
fun FilterLutPanel(
    activeFilterId: String,
    onSelectFilter: (String) -> Unit
) {
    var selectedCategory by remember { mutableStateOf("All") }

    val filteredList = remember(selectedCategory) {
        if (selectedCategory == "All") FilterCatalog.filters
        else FilterCatalog.filters.filter { it.category == selectedCategory }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        // Category Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            FilterCatalog.categories.forEach { cat ->
                val isCatSelected = selectedCategory == cat
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isCatSelected) CyberCyan else DarkSurfaceElevated)
                        .clickable { selectedCategory = cat }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = cat,
                        color = if (isCatSelected) DarkBackground else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        // Filters Horizontal Scroll
        LazyRow(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(filteredList) { lut ->
                val isSelected = lut.id == activeFilterId
                Box(
                    modifier = Modifier
                        .size(width = 86.dp, height = 100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(
                            width = if (isSelected) 2.dp else 1.dp,
                            color = if (isSelected) CyberCyan else GlassBorder,
                            shape = RoundedCornerShape(8.dp)
                        )
                        .clickable { onSelectFilter(lut.id) }
                        .padding(4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(44.dp)
                                .clip(RoundedCornerShape(4.dp))
                                .background(
                                    Brush.verticalGradient(
                                        colors = listOf(Color(lut.previewTint), Color(lut.previewTint).copy(alpha = 0.4f))
                                    )
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text("LUT", color = Color.White, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                        }
                        Text(
                            text = lut.name,
                            color = if (isSelected) CyberCyan else TextPrimary,
                            fontSize = 10.sp,
                            maxLines = 2,
                            fontWeight = FontWeight.SemiBold,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            }
        }
    }
}

// 4. Color Grading Panel
@Composable
fun ColorGradingPanel(
    settings: ColorGradingSettings,
    onUpdate: (ColorGradingSettings) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        GradingSlider("Brightness", settings.brightness, -100f..100f) { onUpdate(settings.copy(brightness = it)) }
        GradingSlider("Contrast", settings.contrast, -100f..100f) { onUpdate(settings.copy(contrast = it)) }
        GradingSlider("Saturation", settings.saturation, -100f..100f) { onUpdate(settings.copy(saturation = it)) }
        GradingSlider("Warmth / Temp", settings.warmth, -100f..100f) { onUpdate(settings.copy(warmth = it)) }
        GradingSlider("Vignette", settings.vignette, 0f..100f) { onUpdate(settings.copy(vignette = it)) }
        GradingSlider("HSL Hue Shift", settings.hueShift, 0f..360f) { onUpdate(settings.copy(hueShift = it)) }
    }
}

@Composable
fun GradingSlider(label: String, value: Float, range: ClosedFloatingPointRange<Float>, onChange: (Float) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, color = TextPrimary, fontSize = 11.sp, modifier = Modifier.width(110.dp))
        Slider(
            value = value,
            onValueChange = onChange,
            valueRange = range,
            modifier = Modifier.weight(1f),
            colors = SliderDefaults.colors(thumbColor = CyberGreen, activeTrackColor = CyberGreen)
        )
        Text(text = value.toInt().toString(), color = TextSecondary, fontSize = 10.sp, modifier = Modifier.width(36.dp))
    }
}

// 5. Effects Panel
@Composable
fun EffectsPanel(
    appliedEffects: List<AppliedEffect>,
    onAddEffect: (EffectType) -> Unit,
    onRemoveEffect: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Add Visual Effect:", color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EffectType.values().forEach { eff ->
                val isAdded = appliedEffects.any { it.effectType == eff }
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isAdded) CyberPurple.copy(alpha = 0.3f) else DarkSurfaceElevated)
                        .border(1.dp, if (isAdded) CyberPurple else GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onAddEffect(eff) }
                        .padding(horizontal = 12.dp, vertical = 8.dp)
                ) {
                    Text(
                        text = eff.label,
                        color = if (isAdded) CyberPurple else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))
        Text("Active Effects on Timeline:", color = TextSecondary, fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            appliedEffects.forEach { eff ->
                Surface(
                    color = ClipEffectColor.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(6.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, CyberPurple)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(text = eff.effectType.label, color = TextPrimary, fontSize = 10.sp)
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Remove",
                            tint = CyberRed,
                            modifier = Modifier
                                .size(14.dp)
                                .clickable { onRemoveEffect(eff.id) }
                        )
                    }
                }
            }
        }
    }
}

// 6. Text & AI Auto Captions Panel
@Composable
fun TextCaptionsPanel(
    onGenerateAutoCaptions: () -> Unit,
    onAddCustomText: (String, TextStylePreset) -> Unit
) {
    var textInput by remember { mutableStateOf("") }
    var selectedStyle by remember { mutableStateOf(TextStylePreset.CYBER_NEON) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        // AI Auto Captions Generator Button
        Button(
            onClick = onGenerateAutoCaptions,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = CyberCyan),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = "AI", tint = DarkBackground)
            Spacer(modifier = Modifier.width(6.dp))
            Text("⚡ Generate AI Auto Captions (Karaoke Sync)", color = DarkBackground, fontWeight = FontWeight.Bold, fontSize = 12.sp)
        }

        Spacer(modifier = Modifier.height(8.dp))

        // Custom Text Field
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TextField(
                value = textInput,
                onValueChange = { textInput = it },
                placeholder = { Text("Enter text...", fontSize = 12.sp) },
                modifier = Modifier.weight(1f),
                singleLine = true,
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = DarkSurfaceElevated,
                    unfocusedContainerColor = DarkSurfaceElevated,
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary
                )
            )

            Button(
                onClick = {
                    if (textInput.isNotBlank()) {
                        onAddCustomText(textInput, selectedStyle)
                        textInput = ""
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("Add Text", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("Typography Style Preset:", color = TextSecondary, fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            TextStylePreset.values().forEach { style ->
                val isSelected = style == selectedStyle
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) CyberCyan.copy(alpha = 0.2f) else DarkSurfaceElevated)
                        .border(1.dp, if (isSelected) CyberCyan else GlassBorder, RoundedCornerShape(6.dp))
                        .clickable { selectedStyle = style }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = style.label,
                        color = if (isSelected) CyberCyan else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

// 7. Audio Studio Panel
@Composable
fun AudioStudioPanel(
    onAddMusic: (AudioItem) -> Unit,
    onAddSfx: (String) -> Unit,
    onRecordVo: () -> Unit,
    onExtractAudio: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Voice Over Button
            Button(
                onClick = onRecordVo,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyberRed),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Mic", modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Record Mic VO", fontSize = 11.sp)
            }

            // Extract Audio Button
            Button(
                onClick = onExtractAudio,
                modifier = Modifier.weight(1f),
                colors = ButtonDefaults.buttonColors(containerColor = CyberGreen),
                shape = RoundedCornerShape(8.dp)
            ) {
                Icon(imageVector = Icons.Default.GraphicEq, contentDescription = "Extract", tint = DarkBackground, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Extract Audio", color = DarkBackground, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Stock Music Tracks:", color = TextSecondary, fontSize = 11.sp)
        MediaStock.stockMusic.forEach { music ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(DarkSurfaceElevated)
                    .clickable { onAddMusic(music) }
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🎵 ${music.title}", color = TextPrimary, fontSize = 11.sp)
                Text(text = "+ Add", color = CyberCyan, fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(6.dp))
        Text("SFX Sound Effects:", color = TextSecondary, fontSize = 11.sp)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            MediaStock.stockSfx.forEach { (name, _) ->
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(6.dp))
                        .clickable { onAddSfx(name) }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(text = name, color = CyberGold, fontSize = 10.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

// 8. Overlay & PIP Panel
@Composable
fun OverlayPipPanel(
    onAddPip: (String, OverlayType, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Text("Add PIP Video or Animated Stickers:", color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = { onAddPip("PIP Video 2050", OverlayType.PIP_VIDEO, "🎥 PIP") },
                colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text("+ PIP Video Clip", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        Text("Cyber Stickers & Emojis:", color = TextSecondary, fontSize = 11.sp)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            MediaStock.stockStickers.forEach { (emoji, name) ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(DarkSurfaceElevated)
                        .border(1.dp, GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onAddPip(name, OverlayType.STICKER, emoji) },
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = emoji, fontSize = 22.sp)
                }
            }
        }
    }
}

// 9. Transitions Panel
@Composable
fun TransitionsPanel(
    currentTransition: TransitionType,
    onSelectTransition: (TransitionType) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Apply Transition to Next Clip:", color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TransitionType.values().forEach { trans ->
                val isSelected = currentTransition == trans
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isSelected) CyberGold.copy(alpha = 0.25f) else DarkSurfaceElevated)
                        .border(1.dp, if (isSelected) CyberGold else GlassBorder, RoundedCornerShape(8.dp))
                        .clickable { onSelectTransition(trans) }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = trans.displayName,
                        color = if (isSelected) CyberGold else TextPrimary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

// 10. Chroma Key Green Screen Panel
@Composable
fun ChromaKeyPanel(
    clip: VideoClip?,
    onUpdate: (Boolean, Long, Float, Float, Float) -> Unit
) {
    var enabled by remember(clip) { mutableStateOf(clip?.chromaKeyEnabled ?: false) }
    var tolerance by remember(clip) { mutableStateOf(clip?.chromaTolerance ?: 0.45f) }
    var softness by remember(clip) { mutableStateOf(clip?.chromaSoftness ?: 0.2f) }
    var despill by remember(clip) { mutableStateOf(clip?.chromaDespill ?: 0.3f) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Enable Chroma Key (Green Screen)", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    onUpdate(enabled, 0xFF00FF00, tolerance, softness, despill)
                },
                colors = SwitchDefaults.colors(checkedThumbColor = CyberGreen, checkedTrackColor = CyberGreen.copy(alpha = 0.5f))
            )
        }

        if (enabled) {
            Spacer(modifier = Modifier.height(6.dp))
            GradingSlider("Tolerance", tolerance * 100f, 0f..100f) {
                tolerance = it / 100f
                onUpdate(true, 0xFF00FF00, tolerance, softness, despill)
            }
            GradingSlider("Edge Softness", softness * 100f, 0f..100f) {
                softness = it / 100f
                onUpdate(true, 0xFF00FF00, tolerance, softness, despill)
            }
            GradingSlider("Color Despill", despill * 100f, 0f..100f) {
                despill = it / 100f
                onUpdate(true, 0xFF00FF00, tolerance, softness, despill)
            }
        }
    }
}

// 11. Pro Store & Monetization Panel
@Composable
fun ProStorePanel(
    isPro: Boolean,
    onPurchasePro: () -> Unit,
    onWatchRewardedAd: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (isPro) {
            Surface(
                color = CyberGold.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, CyberGold),
                modifier = Modifier.fillMaxWidth().padding(8.dp)
            ) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("👑 VIP PRO STATUS ACTIVE", color = CyberGold, fontSize = 14.sp, fontWeight = FontWeight.Black)
                    Text("All 4K 60FPS exports, Watermark removal, and 100+ LUTs are permanently unlocked.", color = TextPrimary, fontSize = 11.sp, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
        } else {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Button(
                    onClick = onPurchasePro,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberGold),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Unlock PRO Lifetime ($4.99)", color = DarkBackground, fontWeight = FontWeight.Black, fontSize = 11.sp)
                }

                Button(
                    onClick = onWatchRewardedAd,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = CyberMagenta),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Watch Ad for 4K (Free)", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "• Remove Watermark\n• 4K 60FPS ProRes Export\n• All 100+ LUTs & VFX\n• Unlimited PIP Layers\n• Zero AdMob Ads",
                color = TextSecondary,
                fontSize = 11.sp
            )
        }
    }
}

// 12. Flutter Project Code Viewer
@Composable
fun FlutterCodeViewerPanel() {
    var selectedFile by remember { mutableStateOf("pubspec.yaml") }

    val codeMap = remember {
        mapOf(
            "pubspec.yaml" to """
name: video_editor_pro_2050
description: Next-Gen Multi-Track Video Editor with 60FPS Timeline & AI Captions
version: 1.0.0+1
environment:
  sdk: ">=3.0.0 <4.0.0"

dependencies:
  flutter:
    sdk: flutter
  ffmpeg_kit_flutter_full: ^6.0.3
  video_player: ^2.8.2
  file_picker: ^8.0.0
  google_mobile_ads: ^5.0.0
  in_app_purchase: ^3.1.13
  provider: ^6.1.1

flutter:
  uses-material-design: true
""".trimIndent(),
            "main.dart" to """
import 'package:flutter/material.dart';
import 'package:google_mobile_ads/google_mobile_ads.dart';

void main() async {
  WidgetsFlutterBinding.ensureInitialized();
  MobileAds.instance.initialize();
  runApp(const VideoEditorProApp());
}

class VideoEditorProApp extends StatelessWidget {
  const VideoEditorProApp({super.key});

  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Video Editor Pro 2050',
      theme: ThemeData.dark().copyWith(
        scaffoldBackgroundColor: const Color(0xFF080B10),
        primaryColor: const Color(0xFF00F0FF),
      ),
      home: const EditorHomeScreen(),
    );
  }
}
""".trimIndent(),
            "timeline.dart" to """
import 'package:flutter/material.dart';

class MultiTrackTimelineWidget extends StatelessWidget {
  final double zoom;
  final Duration playhead;
  const MultiTrackTimelineWidget({super.key, required this.zoom, required this.playhead});

  @override
  Widget build(BuildContext context) {
    return Container(
      height: 220,
      color: const Color(0xFF0D121C),
      child: ListView(
        scrollDirection: Axis.horizontal,
        children: [
          // Track 1: Main Video
          // Track 2: PIP Overlay
          // Track 3: Audio Waveform
          // Track 4: AI Captions
        ],
      ),
    );
  }
}
""".trimIndent()
        )
    }

    Column(modifier = Modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            codeMap.keys.forEach { fileName ->
                val isSel = fileName == selectedFile
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSel) CyberCyan else DarkSurfaceElevated)
                        .clickable { selectedFile = fileName }
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = fileName,
                        color = if (isSel) DarkBackground else TextPrimary,
                        fontSize = 10.sp,
                        fontFamily = FontFamily.Monospace,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(6.dp))
                .background(Color.Black)
                .padding(8.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Text(
                text = codeMap[selectedFile] ?: "",
                color = CyberGreen,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
