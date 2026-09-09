package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*

@Composable
fun MultiTrackTimeline(
    clips: List<VideoClip>,
    overlays: List<OverlayItem>,
    audios: List<AudioItem>,
    captions: List<TextCaption>,
    effects: List<AppliedEffect>,
    playheadMs: Long,
    totalDurationMs: Long,
    zoom: Float,
    selectedClipId: String?,
    onSeek: (Long) -> Unit,
    onSelectClip: (String) -> Unit,
    onSplitClip: () -> Unit,
    onDeleteClip: () -> Unit,
    onDuplicateClip: () -> Unit,
    onOpenTransitions: () -> Unit,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    // ms to dp conversion ratio based on zoom: 1 second = (60 * zoom) dp
    val pxPerSec = (70 * zoom).dp
    val totalTimelineWidth = ((totalDurationMs / 1000f) * 70 * zoom).dp.coerceAtLeast(360.dp)

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(TimelineTrackBg)
    ) {
        // Quick Timeline Action Bar (Cut/Split, Duplicate, Delete, Transition, Zoom)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(DarkSurface)
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Edit quick tools
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                // Split Button
                TimelineActionChip(
                    icon = Icons.Default.ContentCut,
                    label = "Split",
                    onClick = onSplitClip,
                    tint = CyberCyan
                )

                // Delete Button
                TimelineActionChip(
                    icon = Icons.Default.Delete,
                    label = "Delete",
                    onClick = onDeleteClip,
                    tint = CyberRed
                )

                // Duplicate Button
                TimelineActionChip(
                    icon = Icons.Default.ContentCopy,
                    label = "Duplicate",
                    onClick = onDuplicateClip,
                    tint = CyberPurple
                )

                // Transition Button
                TimelineActionChip(
                    icon = Icons.Default.Transform,
                    label = "Transition",
                    onClick = onOpenTransitions,
                    tint = CyberGold
                )
            }

            // Zoom indicator
            Text(
                text = "${String.format("%.1f", zoom)}x",
                color = TextSecondary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace,
                fontWeight = FontWeight.Bold
            )
        }

        // Multi-Layer Scrollable Tracks Container
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(230.dp)
        ) {
            // Horizontal scrollable layers
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(scrollState)
                    .padding(horizontal = 16.dp, vertical = 6.dp)
                    .width(totalTimelineWidth)
            ) {
                // 1. Time Ruler
                TimeRuler(totalDurationMs = totalDurationMs, zoom = zoom, width = totalTimelineWidth)

                Spacer(modifier = Modifier.height(6.dp))

                // 2. Track: Video Layer
                VideoTrackRow(
                    clips = clips,
                    zoom = zoom,
                    selectedClipId = selectedClipId,
                    onSelectClip = onSelectClip
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 3. Track: Overlay / PIP Layer
                OverlayTrackRow(
                    overlays = overlays,
                    totalDurationMs = totalDurationMs,
                    zoom = zoom
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 4. Track: Audio Layer (Waveform + Beat Dots)
                AudioTrackRow(
                    audios = audios,
                    totalDurationMs = totalDurationMs,
                    zoom = zoom
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 5. Track: Subtitle / Captions Layer
                CaptionTrackRow(
                    captions = captions,
                    totalDurationMs = totalDurationMs,
                    zoom = zoom
                )

                Spacer(modifier = Modifier.height(4.dp))

                // 6. Track: Effects Layer
                EffectTrackRow(
                    effects = effects,
                    totalDurationMs = totalDurationMs,
                    zoom = zoom
                )
            }

            // Draggable Scrubber Needle / Playhead
            val playheadOffsetDp = ((playheadMs / 1000f) * 70 * zoom).dp - scrollState.value.dp
            Box(
                modifier = Modifier
                    .offset(x = playheadOffsetDp.coerceAtLeast(0.dp))
                    .width(2.dp)
                    .fillMaxHeight()
                    .background(CyberMagenta)
                    .pointerInput(totalDurationMs, zoom) {
                        detectDragGestures { change, dragAmount ->
                            change.consume()
                            val deltaSec = dragAmount.x / (70 * zoom)
                            val newMs = (playheadMs + (deltaSec * 1000).toLong()).coerceIn(0L, totalDurationMs)
                            onSeek(newMs)
                        }
                    }
            ) {
                // Playhead top handle
                Box(
                    modifier = Modifier
                        .size(14.dp)
                        .align(Alignment.TopCenter)
                        .offset(y = (-2).dp)
                        .clip(CircleShape)
                        .background(CyberMagenta)
                        .border(1.dp, TextPrimary, CircleShape)
                )
            }
        }
    }
}

@Composable
private fun TimelineActionChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(6.dp),
        color = DarkSurfaceElevated,
        border = androidx.compose.foundation.BorderStroke(1.dp, tint.copy(alpha = 0.3f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 6.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Icon(imageVector = icon, contentDescription = label, tint = tint, modifier = Modifier.size(14.dp))
            Text(text = label, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun TimeRuler(totalDurationMs: Long, zoom: Float, width: androidx.compose.ui.unit.Dp) {
    val totalSeconds = (totalDurationMs / 1000).toInt().coerceAtLeast(5)
    Row(
        modifier = Modifier
            .width(width)
            .height(18.dp)
            .background(DarkBackground),
        verticalAlignment = Alignment.CenterVertically
    ) {
        for (sec in 0..totalSeconds) {
            Box(
                modifier = Modifier.width((70 * zoom).dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .width(1.dp)
                            .height(10.dp)
                            .background(TextMuted)
                    )
                    Spacer(modifier = Modifier.width(3.dp))
                    Text(
                        text = "${sec}s",
                        color = TextMuted,
                        fontSize = 9.sp,
                        fontFamily = FontFamily.Monospace
                    )
                }
            }
        }
    }
}

@Composable
private fun VideoTrackRow(
    clips: List<VideoClip>,
    zoom: Float,
    selectedClipId: String?,
    onSelectClip: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .height(48.dp)
            .fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        clips.forEach { clip ->
            val clipWidthDp = ((clip.effectiveDurationMs / 1000f) * 70 * zoom).dp.coerceAtLeast(40.dp)
            val isSelected = clip.id == selectedClipId

            Box(
                modifier = Modifier
                    .width(clipWidthDp)
                    .fillMaxHeight()
                    .padding(end = 3.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                Color(clip.colorHex),
                                Color(clip.colorHex).copy(alpha = 0.7f)
                            )
                        )
                    )
                    .border(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) CyberCyan else GlassBorder,
                        shape = RoundedCornerShape(8.dp)
                    )
                    .clickable { onSelectClip(clip.id) }
                    .padding(horizontal = 6.dp, vertical = 4.dp)
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = clip.name,
                            color = TextPrimary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                        if (clip.speed != 1.0f) {
                            Text(
                                text = "${String.format("%.1f", clip.speed)}x",
                                color = CyberGold,
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "${clip.effectiveDurationMs / 1000}s",
                            color = TextSecondary,
                            fontSize = 9.sp
                        )
                        if (clip.chromaKeyEnabled) {
                            Text(text = "🟢 Key", color = CyberGreen, fontSize = 9.sp)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OverlayTrackRow(
    overlays: List<OverlayItem>,
    totalDurationMs: Long,
    zoom: Float
) {
    Row(
        modifier = Modifier
            .height(28.dp)
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Layers,
            contentDescription = "PIP",
            tint = CyberMagenta,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        overlays.forEach { item ->
            val startOffset = ((item.startMs / 1000f) * 70 * zoom).dp
            val itemWidth = (((item.endMs - item.startMs) / 1000f) * 70 * zoom).dp.coerceAtLeast(30.dp)
            Box(
                modifier = Modifier
                    .width(itemWidth)
                    .height(22.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClipPipColor.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = "${item.assetContent} ${item.title}",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(4.dp))
        }
    }
}

@Composable
private fun AudioTrackRow(
    audios: List<AudioItem>,
    totalDurationMs: Long,
    zoom: Float
) {
    Row(
        modifier = Modifier
            .height(34.dp)
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.7f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.MusicNote,
            contentDescription = "Audio",
            tint = CyberGreen,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        audios.forEach { audio ->
            val audioWidth = (((audio.endMs - audio.startMs) / 1000f) * 70 * zoom).dp.coerceAtLeast(60.dp)
            Box(
                modifier = Modifier
                    .width(audioWidth)
                    .height(28.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClipAudioColor.copy(alpha = 0.8f))
                    .border(1.dp, CyberGreen.copy(alpha = 0.4f), RoundedCornerShape(4.dp))
                    .padding(horizontal = 6.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = audio.title,
                        color = TextPrimary,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                    // Beat markers (yellow dots)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        audio.beatMarkers.take(5).forEach { _ ->
                            Box(
                                modifier = Modifier
                                    .size(4.dp)
                                    .clip(CircleShape)
                                    .background(CyberGold)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CaptionTrackRow(
    captions: List<TextCaption>,
    totalDurationMs: Long,
    zoom: Float
) {
    Row(
        modifier = Modifier
            .height(26.dp)
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.TextFields,
            contentDescription = "Captions",
            tint = CyberGold,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        captions.forEach { cap ->
            val capWidth = (((cap.endMs - cap.startMs) / 1000f) * 70 * zoom).dp.coerceAtLeast(36.dp)
            Box(
                modifier = Modifier
                    .width(capWidth)
                    .height(20.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClipTextColor.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = cap.text,
                    color = TextPrimary,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}

@Composable
private fun EffectTrackRow(
    effects: List<AppliedEffect>,
    totalDurationMs: Long,
    zoom: Float
) {
    Row(
        modifier = Modifier
            .height(24.dp)
            .fillMaxWidth()
            .background(DarkSurfaceElevated.copy(alpha = 0.3f), RoundedCornerShape(6.dp))
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.AutoFixHigh,
            contentDescription = "FX",
            tint = CyberPurple,
            modifier = Modifier.size(14.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))
        effects.forEach { fx ->
            val fxWidth = (((fx.endMs - fx.startMs) / 1000f) * 70 * zoom).dp.coerceAtLeast(32.dp)
            Box(
                modifier = Modifier
                    .width(fxWidth)
                    .height(18.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(ClipEffectColor.copy(alpha = 0.85f))
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    text = fx.effectType.label,
                    color = TextPrimary,
                    fontSize = 9.sp,
                    maxLines = 1
                )
            }
            Spacer(modifier = Modifier.width(3.dp))
        }
    }
}
