package com.example.model

object MediaStock {
    val defaultClips = listOf(
        VideoClip(
            id = "stock_1",
            name = "Neo-Tokyo 2050 Cyber City",
            type = MediaType.VIDEO,
            colorHex = 0xFF00F0FF,
            durationMs = 6000L,
            trimStartMs = 0L,
            trimEndMs = 6000L,
            speed = 1.0f,
            filterId = "cyber_0",
            transition = TransitionType.CROSS_DISSOLVE
        ),
        VideoClip(
            id = "stock_2",
            name = "Quantum Warp Horizon",
            type = MediaType.VIDEO,
            colorHex = 0xFFFF007A,
            durationMs = 5000L,
            trimStartMs = 0L,
            trimEndMs = 5000L,
            speed = 1.0f,
            filterId = "scifi_1",
            transition = TransitionType.GLITCH_FLASH
        ),
        VideoClip(
            id = "stock_3",
            name = "Green Screen Cyber Dancer",
            type = MediaType.VIDEO,
            colorHex = 0xFF00FF66,
            durationMs = 7000L,
            trimStartMs = 0L,
            trimEndMs = 7000L,
            speed = 1.0f,
            chromaKeyEnabled = true,
            chromaTargetColor = 0xFF00FF00,
            chromaTolerance = 0.5f,
            filterId = "original",
            transition = TransitionType.ZOOM_IN
        ),
        VideoClip(
            id = "stock_4",
            name = "Supercar Neon Velocity",
            type = MediaType.VIDEO,
            colorHex = 0xFFFFB800,
            durationMs = 6000L,
            trimStartMs = 0L,
            trimEndMs = 6000L,
            speed = 1.2f,
            filterId = "cin_1",
            transition = TransitionType.NONE
        )
    )

    val stockMusic = listOf(
        AudioItem(
            id = "music_1",
            title = "Cyber Synthwave 2050",
            artist = "Neon Matrix Sound",
            startMs = 0L,
            endMs = 18000L,
            volume = 0.8f,
            beatMarkers = listOf(1000L, 2000L, 3000L, 4000L, 6000L, 8000L, 10000L, 12000L, 14000L, 16000L),
            waveformPoints = listOf(0.4f, 0.7f, 0.9f, 0.6f, 0.85f, 0.5f, 0.95f, 0.4f, 0.8f, 0.9f, 0.65f, 0.75f)
        ),
        AudioItem(
            id = "music_2",
            title = "Cinematic Bass Drop & Trailer",
            artist = "Hollywood FX Lab",
            startMs = 0L,
            endMs = 15000L,
            volume = 0.9f,
            beatMarkers = listOf(1500L, 3000L, 4500L, 6000L, 9000L, 12000L),
            waveformPoints = listOf(0.2f, 0.3f, 0.5f, 0.95f, 0.9f, 0.85f, 0.4f, 0.3f, 0.8f, 1.0f, 0.9f, 0.6f)
        ),
        AudioItem(
            id = "music_3",
            title = "Lo-Fi Tokyo Midnight Rain",
            artist = "Chilled Horizon",
            startMs = 0L,
            endMs = 20000L,
            volume = 0.7f,
            beatMarkers = listOf(2000L, 4000L, 6000L, 8000L, 10000L, 14000L, 18000L),
            waveformPoints = listOf(0.3f, 0.4f, 0.5f, 0.4f, 0.6f, 0.5f, 0.4f, 0.5f, 0.6f, 0.4f, 0.5f, 0.3f)
        )
    )

    val stockSfx = listOf(
        "Whoosh Cinematic" to "💨 Cinematic fast air whoosh transition",
        "Bass Drop Impact" to "💥 Heavy sub-bass earthquake hit",
        "Laser Beam Zap" to "⚡ High-voltage sci-fi laser shot",
        "Glitch Noise Burst" to "📡 CRT television static glitch",
        "Camera Shutter Snap" to "📸 Vintage SLR shutter click",
        "Rewind Cassette Tape" to "⏪ Fast forward cassette spin",
        "Vinyl Record Scratch" to "💿 Hip-hop vinyl turntable scratch",
        "Pop Bubble Notification" to "✨ Crisp modern UI bubble pop"
    )

    val stockStickers = listOf(
        "🔥" to "Fire Flame",
        "⚡" to "Electric Bolt",
        "🚀" to "Rocket Boost",
        "👑" to "Pro Crown",
        "💯" to "Hundred Score",
        "🤖" to "Cyborg AI",
        "🛸" to "Alien UFO",
        "🕶️" to "Cyber Shades",
        "⭐" to "Neon Star",
        "💥" to "Comic Boom",
        "🎯" to "Target Lock",
        "💎" to "Cyber Diamond",
        "❤️‍🔥" to "Heart Fire",
        "✨" to "Magic Sparkles",
        "🔴 REC" to "Record Tag",
        "2050" to "Year 2050 Badge"
    )

    val autoCaptionsPreset = listOf(
        TextCaption("cap_1", "WELCOME TO VIDEO EDITOR PRO 2050", 0L, 2500L, TextStylePreset.CYBER_NEON, isAutoCaption = true),
        TextCaption("cap_2", "CAPCUT & VN EVOLVED WITH 60FPS TIMELINE", 2500L, 5500L, TextStylePreset.KINETIC_POP, isAutoCaption = true),
        TextCaption("cap_3", "CHROMA KEY GREEN SCREEN ACTIVE", 5500L, 9000L, TextStylePreset.CHROME_3D, isAutoCaption = true),
        TextCaption("cap_4", "READY FOR 4K 60FPS CINEMATIC EXPORT", 9000L, 14000L, TextStylePreset.CINEMATIC_BOLD, isAutoCaption = true)
    )
}
