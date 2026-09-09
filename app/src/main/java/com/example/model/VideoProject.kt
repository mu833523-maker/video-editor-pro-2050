package com.example.model

enum class MediaType {
    VIDEO, PHOTO, AUDIO, GIF
}

enum class AspectRatio(val label: String, val widthRatio: Float, val heightRatio: Float) {
    RATIO_16_9("16:9 YouTube", 16f, 9f),
    RATIO_9_16("9:16 TikTok/Reels", 9f, 16f),
    RATIO_1_1("1:1 Square", 1f, 1f),
    RATIO_4_5("4:5 Portrait", 4f, 5f),
    RATIO_21_9("21:9 Cinema", 21f, 9f)
}

enum class TransitionType(val displayName: String) {
    NONE("None"),
    CROSS_DISSOLVE("Cross Dissolve"),
    GLITCH_FLASH("Glitch Flash"),
    WHIP_PAN_LEFT("Whip Pan Left"),
    WHIP_PAN_RIGHT("Whip Pan Right"),
    ZOOM_IN("Zoom In Punch"),
    ZOOM_OUT("Zoom Out Spin"),
    LIGHT_LEAK("Film Light Leak"),
    BLACK_FADE("Fade to Black"),
    WHITE_FLASH("White Strobe"),
    RIPPLE_WARP("Quantum Ripple")
}

data class KeyframePoint(
    val timeMs: Long,
    val scale: Float = 1.0f,
    val posX: Float = 0f,
    val posY: Float = 0f,
    val opacity: Float = 1.0f,
    val rotation: Float = 0f
)

data class SpeedCurvePoint(
    val progress: Float, // 0.0 to 1.0
    val speed: Float     // 0.1 to 10.0
)

enum class SpeedCurvePreset(val displayName: String, val points: List<SpeedCurvePoint>) {
    STANDARD("Normal 1.0x", listOf(SpeedCurvePoint(0f, 1f), SpeedCurvePoint(1f, 1f))),
    HERO_FLASH("Hero Flash", listOf(SpeedCurvePoint(0f, 0.4f), SpeedCurvePoint(0.3f, 4.0f), SpeedCurvePoint(0.7f, 0.2f), SpeedCurvePoint(1f, 1.5f))),
    BULLET_TIME("Bullet Time", listOf(SpeedCurvePoint(0f, 2.5f), SpeedCurvePoint(0.4f, 0.2f), SpeedCurvePoint(0.7f, 0.2f), SpeedCurvePoint(1f, 3.0f))),
    MONTAGE_RUSH("Montage Rush", listOf(SpeedCurvePoint(0f, 5.0f), SpeedCurvePoint(0.5f, 0.5f), SpeedCurvePoint(1f, 6.0f))),
    JUMP_CUT("Fast Velocity", listOf(SpeedCurvePoint(0f, 0.2f), SpeedCurvePoint(0.5f, 8.0f), SpeedCurvePoint(1f, 0.5f)))
}

data class VideoClip(
    val id: String,
    val name: String,
    val type: MediaType = MediaType.VIDEO,
    val uri: String? = null,
    val colorHex: Long = 0xFF0284C7,
    val durationMs: Long = 6000L,
    val trimStartMs: Long = 0L,
    val trimEndMs: Long = 6000L,
    val speed: Float = 1.0f,
    val speedCurvePreset: SpeedCurvePreset = SpeedCurvePreset.STANDARD,
    val volume: Float = 1.0f,
    val isReversed: Boolean = false,
    val isFrozen: Boolean = false,
    val filterId: String = "original",
    val transition: TransitionType = TransitionType.NONE,
    val chromaKeyEnabled: Boolean = false,
    val chromaTargetColor: Long = 0xFF00FF00, // Green Screen
    val chromaTolerance: Float = 0.45f,
    val chromaSoftness: Float = 0.2f,
    val chromaDespill: Float = 0.3f,
    val keyframes: List<KeyframePoint> = emptyList()
) {
    val effectiveDurationMs: Long
        get() {
            val trimmed = (trimEndMs - trimStartMs).coerceAtLeast(500L)
            return (trimmed / speed.coerceAtLeast(0.1f)).toLong()
        }
}

enum class OverlayType {
    PIP_VIDEO, STICKER, EMOJI, GIF
}

data class OverlayItem(
    val id: String,
    val title: String,
    val type: OverlayType,
    val assetContent: String, // Emoji string or asset tag
    val startMs: Long,
    val endMs: Long,
    val posX: Float = 0f, // -1f to 1f normalized
    val posY: Float = 0f,
    val scale: Float = 1f,
    val rotation: Float = 0f,
    val opacity: Float = 1f,
    val animation: String = "None"
)

data class AudioItem(
    val id: String,
    val title: String,
    val artist: String = "Cyber Beats 2050",
    val startMs: Long,
    val endMs: Long,
    val volume: Float = 0.85f,
    val isVoiceOver: Boolean = false,
    val fadeInMs: Long = 500L,
    val fadeOutMs: Long = 500L,
    val beatMarkers: List<Long> = listOf(1000L, 2000L, 3000L, 4000L, 5000L),
    val waveformPoints: List<Float> = listOf(0.2f, 0.4f, 0.8f, 0.9f, 0.6f, 0.3f, 0.7f, 0.95f, 0.5f, 0.2f, 0.7f, 0.4f)
)

data class TextCaption(
    val id: String,
    val text: String,
    val startMs: Long,
    val endMs: Long,
    val style: TextStylePreset = TextStylePreset.CYBER_NEON,
    val fontSizeSp: Float = 22f,
    val textColor: Long = 0xFFFFFFFF,
    val strokeColor: Long = 0xFF00F0FF,
    val isAutoCaption: Boolean = false,
    val posX: Float = 0f,
    val posY: Float = 0.65f // bottom lower third
)

enum class TextStylePreset(val label: String) {
    CYBER_NEON("Cyber Neon 2050"),
    CINEMATIC_BOLD("Cinematic Bold"),
    KINETIC_POP("Kinetic Pop"),
    TYPEWRITER("Typewriter Retro"),
    GLITCH_MATRIX("Glitch Matrix"),
    CHROME_3D("3D Chrome Metal"),
    SUBTITLE_BOX("Classic Subtitle Box")
}

enum class EffectType(val label: String, val iconName: String) {
    GLITCH("Glitch RGB", "glitch"),
    VHS_1998("VHS 1998", "vhs"),
    ZOOM_3D("3D Zoom", "zoom"),
    PARTICLES("Cyber Stardust", "particles"),
    MOTION_BLUR("Motion Blur", "blur"),
    BLOOM_GLOW("Neon Bloom", "bloom")
}

data class AppliedEffect(
    val id: String,
    val effectType: EffectType,
    val intensity: Float = 0.7f,
    val startMs: Long = 0L,
    val endMs: Long = 10000L
)

data class ColorGradingSettings(
    val brightness: Float = 0f,   // -100 to 100
    val contrast: Float = 0f,     // -100 to 100
    val saturation: Float = 0f,   // -100 to 100
    val exposure: Float = 0f,     // -100 to 100
    val warmth: Float = 0f,       // -100 to 100 (cold blue to warm amber)
    val vignette: Float = 0f,     // 0 to 100
    val hueShift: Float = 0f      // 0 to 360 degrees
)

data class FilterLut(
    val id: String,
    val name: String,
    val category: String,
    val previewTint: Long,
    val brightnessBoost: Float = 0f,
    val contrastBoost: Float = 0f,
    val saturationBoost: Float = 0f,
    val hueShiftDeg: Float = 0f
)

enum class ExportResolution(val label: String, val width: Int, val height: Int, val isProOnly: Boolean) {
    RES_720P("720p HD", 1280, 720, false),
    RES_1080P("1080p Full HD", 1920, 1080, false),
    RES_2K("2K QHD (1440p)", 2560, 1440, true),
    RES_4K("4K Ultra HD 2050 (2160p)", 3840, 2160, true)
}

enum class ExportFps(val label: String, val fps: Int, val isProOnly: Boolean) {
    FPS_24("24 FPS (Film)", 24, false),
    FPS_30("30 FPS (Standard)", 30, false),
    FPS_60("60 FPS (Pro Ultra Smooth)", 60, true)
}

data class ExportConfig(
    val resolution: ExportResolution = ExportResolution.RES_1080P,
    val fps: ExportFps = ExportFps.FPS_30,
    val format: String = "MP4 (H.264 High Profile)",
    val bitrateMbps: Int = 25,
    val removeWatermark: Boolean = false
)
