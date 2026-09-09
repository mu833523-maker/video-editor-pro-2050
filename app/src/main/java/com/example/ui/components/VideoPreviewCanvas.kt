package com.example.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.*
import com.example.ui.theme.*
import kotlin.math.sin

@Composable
fun VideoPreviewCanvas(
    aspectRatio: AspectRatio,
    playheadMs: Long,
    isPlaying: Boolean,
    totalDurationMs: Long,
    clips: List<VideoClip>,
    overlays: List<OverlayItem>,
    captions: List<TextCaption>,
    appliedEffects: List<AppliedEffect>,
    colorGrading: ColorGradingSettings,
    showWatermark: Boolean,
    onTogglePlayPause: () -> Unit,
    onSelectAspectRatio: (AspectRatio) -> Unit,
    modifier: Modifier = Modifier
) {
    // Determine active clip at playhead
    var accumulated = 0L
    var activeClip: VideoClip? = null
    for (clip in clips) {
        val dur = clip.effectiveDurationMs
        if (playheadMs >= accumulated && playheadMs <= accumulated + dur) {
            activeClip = clip
            break
        }
        accumulated += dur
    }
    if (activeClip == null) activeClip = clips.firstOrNull()

    // Animation for 60fps glitch / pulse / particles
    val infiniteTransition = rememberInfiniteTransition(label = "playbackAnim")
    val pulseAnim by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(2000, easing = LinearEasing), RepeatMode.Restart),
        label = "pulse"
    )
    val glitchJitter by infiniteTransition.animateFloat(
        initialValue = -8f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(tween(120, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "jitter"
    )

    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(DarkBackground)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Aspect Ratio Selector Chips & Timecode Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Aspect ratio pill
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                listOf(
                    AspectRatio.RATIO_16_9,
                    AspectRatio.RATIO_9_16,
                    AspectRatio.RATIO_1_1
                ).forEach { ratio ->
                    val isSelected = ratio == aspectRatio
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) CyberCyan.copy(alpha = 0.25f) else DarkSurfaceElevated)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) CyberCyan else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .clickable { onSelectAspectRatio(ratio) }
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = ratio.label.split(" ").first(),
                            color = if (isSelected) CyberCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            // Timecode Badge
            Surface(
                color = DarkSurfaceElevated,
                shape = RoundedCornerShape(6.dp),
                border = Stroke(width = 1f).let { null }
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(6.dp)
                            .clip(CircleShape)
                            .background(if (isPlaying) CyberGreen else CyberMagenta)
                    )
                    Text(
                        text = formatTimecode(playheadMs) + " / " + formatTimecode(totalDurationMs),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = TextPrimary,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Preview Canvas Box with Aspect Ratio
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 180.dp, max = 280.dp)
                .aspectRatio(aspectRatio.widthRatio / aspectRatio.heightRatio, matchHeightConstraintsFirst = true)
                .clip(RoundedCornerShape(12.dp))
                .border(1.5.dp, GlassBorder, RoundedCornerShape(12.dp))
                .background(Color.Black)
                .clickable { onTogglePlayPause() },
            contentAlignment = Alignment.Center
        ) {
            // Custom Canvas Rendering Pipeline
            Canvas(modifier = Modifier.fillMaxSize()) {
                val canvasWidth = size.width
                val canvasHeight = size.height

                // 1. Background / Chroma Key Base
                if (activeClip?.chromaKeyEnabled == true) {
                    // Render replaced cyberpunk background
                    drawRect(
                        brush = Brush.verticalGradient(
                            colors = listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF052E16))
                        )
                    )
                    // Grid lines
                    for (x in 0..10) {
                        val gx = canvasWidth * (x / 10f)
                        drawLine(
                            color = Color(0x3300FF9D),
                            start = Offset(gx, 0f),
                            end = Offset(gx, canvasHeight),
                            strokeWidth = 1f
                        )
                    }
                } else {
                    // Regular Video Background
                    val baseColor = Color(activeClip?.colorHex ?: 0xFF0284C7)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(baseColor.copy(alpha = 0.85f), Color(0xFF05070D)),
                            center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                            radius = canvasWidth * 0.75f
                        )
                    )
                }

                // 2. Animated Subject / Motion Visuals
                drawFilmSubject(
                    width = canvasWidth,
                    height = canvasHeight,
                    clip = activeClip,
                    progress = (playheadMs % 4000L) / 4000f,
                    pulseAnim = pulseAnim,
                    appliedEffects = appliedEffects,
                    jitter = glitchJitter
                )

                // 3. Glitch RGB split scanlines if active
                if (appliedEffects.any { it.effectType == EffectType.GLITCH }) {
                    drawGlitchScanlines(canvasWidth, canvasHeight, glitchJitter)
                }

                // 4. VHS 1998 scan lines & timestamp
                if (appliedEffects.any { it.effectType == EffectType.VHS_1998 }) {
                    drawVhsArtifacts(canvasWidth, canvasHeight, pulseAnim)
                }

                // 5. Particles / Stardust
                if (appliedEffects.any { it.effectType == EffectType.PARTICLES }) {
                    drawParticleField(canvasWidth, canvasHeight, pulseAnim)
                }

                // 6. Color Grading Vignette
                if (colorGrading.vignette > 0f) {
                    val vignetteAlpha = (colorGrading.vignette / 100f).coerceIn(0f, 0.9f)
                    drawRect(
                        brush = Brush.radialGradient(
                            colors = listOf(Color.Transparent, Color.Black.copy(alpha = vignetteAlpha)),
                            center = Offset(canvasWidth / 2f, canvasHeight / 2f),
                            radius = canvasWidth * 0.65f
                        )
                    )
                }
            }

            // Overlay PIP / Stickers
            overlays.filter { playheadMs in it.startMs..it.endMs }.forEach { item ->
                Box(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .offset(
                            x = (item.posX * 120).dp,
                            y = (item.posY * 80).dp
                        )
                ) {
                    Text(
                        text = item.assetContent,
                        fontSize = (32 * item.scale).sp,
                        modifier = Modifier.padding(4.dp)
                    )
                }
            }

            // Animated Text Captions & Subtitles
            captions.filter { playheadMs in it.startMs..it.endMs }.forEach { caption ->
                val progress = ((playheadMs - caption.startMs).toFloat() / (caption.endMs - caption.startMs)).coerceIn(0f, 1f)
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 20.dp, start = 16.dp, end = 16.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            when (caption.style) {
                                TextStylePreset.SUBTITLE_BOX -> Color.Black.copy(alpha = 0.75f)
                                TextStylePreset.CYBER_NEON -> Color(0x99001428)
                                else -> Color.Transparent
                            }
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = caption.text,
                        color = when (caption.style) {
                            TextStylePreset.CYBER_NEON -> CyberCyan
                            TextStylePreset.KINETIC_POP -> CyberMagenta
                            TextStylePreset.CHROME_3D -> CyberGold
                            else -> TextPrimary
                        },
                        fontSize = caption.fontSizeSp.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        fontFamily = if (caption.style == TextStylePreset.TYPEWRITER) FontFamily.Monospace else FontFamily.SansSerif
                    )
                }
            }

            // Watermark (Free User Corner Stamp)
            if (showWatermark) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.dp, CyberCyan.copy(alpha = 0.4f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text("⚡", fontSize = 10.sp)
                        Text(
                            text = "VIDEO EDITOR PRO 2050",
                            color = CyberCyan,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }

            // Center Play Icon when Paused
            if (!isPlaying) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(Color.Black.copy(alpha = 0.65f))
                        .border(1.5.dp, CyberCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = "Play",
                        tint = CyberCyan,
                        modifier = Modifier.size(32.dp)
                    )
                }
            }
        }
    }
}

private fun DrawScope.drawFilmSubject(
    width: Float,
    height: Float,
    clip: VideoClip?,
    progress: Float,
    pulseAnim: Float,
    appliedEffects: List<AppliedEffect>,
    jitter: Float
) {
    val cx = width / 2f + (if (appliedEffects.any { it.effectType == EffectType.GLITCH }) jitter else 0f)
    val cy = height / 2f
    val radius = (height * 0.28f) * (if (appliedEffects.any { it.effectType == EffectType.ZOOM_3D }) 1.25f else 1.0f)

    // Animated subject hologram / cinematic graphic
    drawCircle(
        brush = Brush.sweepGradient(
            colors = listOf(CyberCyan, CyberMagenta, CyberPurple, CyberCyan)
        ),
        radius = radius,
        center = Offset(cx, cy),
        style = Stroke(width = 4.dp.toPx())
    )

    // Inner orbiting rings
    val angle = progress * 360f
    val rad = Math.toRadians(angle.toDouble())
    val orbX = cx + (radius * 0.75f * kotlin.math.cos(rad)).toFloat()
    val orbY = cy + (radius * 0.75f * kotlin.math.sin(rad)).toFloat()
    drawCircle(
        color = CyberCyan,
        radius = 8.dp.toPx(),
        center = Offset(orbX, orbY)
    )

    // Clip Title display in center
    val clipName = clip?.name ?: "Stock Clip"
}

private fun DrawScope.drawGlitchScanlines(width: Float, height: Float, jitter: Float) {
    for (i in 0..15) {
        val y = (height * (i / 15f) + (jitter * 5f)) % height
        drawLine(
            color = if (i % 2 == 0) CyberMagenta.copy(alpha = 0.45f) else CyberCyan.copy(alpha = 0.45f),
            start = Offset(0f, y),
            end = Offset(width, y),
            strokeWidth = 2.dp.toPx()
        )
    }
}

private fun DrawScope.drawVhsArtifacts(width: Float, height: Float, pulse: Float) {
    // Scanline distortion band
    val bandY = (height * pulse) % height
    drawRect(
        color = Color.White.copy(alpha = 0.12f),
        topLeft = Offset(0f, bandY),
        size = Size(width, 24.dp.toPx())
    )
}

private fun DrawScope.drawParticleField(width: Float, height: Float, pulse: Float) {
    for (i in 0..25) {
        val px = (width * ((i * 37) % 100 / 100f))
        val py = (height * (((i * 59) % 100 / 100f) + pulse)) % height
        drawCircle(
            color = CyberCyan.copy(alpha = 0.6f),
            radius = (1.5f + (i % 3)).dp.toPx(),
            center = Offset(px, py)
        )
    }
}

fun formatTimecode(ms: Long): String {
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    val frames = ((ms % 1000) / 16.66f).toInt().coerceIn(0, 59)
    return String.format("%02d:%02d:%02d", minutes, seconds, frames)
}
