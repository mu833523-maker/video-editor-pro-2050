package com.example.model

object FilterCatalog {
    val categories = listOf(
        "All",
        "Cinematic",
        "Cyberpunk 2050",
        "Retro VHS",
        "Moody Teal",
        "Anime Glow",
        "Gold Sunset",
        "Black & White",
        "Urban Street",
        "Matrix Green",
        "Sci-Fi Neon"
    )

    val filters: List<FilterLut> = buildList {
        // Original
        add(FilterLut("original", "Original Raw", "All", 0xFFFFFFFF, 0f, 0f, 0f, 0f))

        // Cinematic Category (15 filters)
        val cinematicNames = listOf(
            "Hollywood 35mm", "Teal & Orange Pro", "Blade Runner 2049", "IMAX Golden Hour",
            "Kodak Vision3", "Fujifilm Eterna", "Film Noir Dark", "Oppenheimer B&W",
            "Dune Arrakis Sand", "Interstellar Deep", "Cyber Gotham", "Matrix Green Code",
            "Marvel Blockbuster", "Tarantino 70s", "Wes Anderson Pastel"
        )
        val cinTints = listOf(
            0xFFFFAA44, 0xFF00E5FF, 0xFFFF0055, 0xFFFFD700,
            0xFFFFE0B2, 0xFF80CBC4, 0xFFB0BEC5, 0xFFFFFFFF,
            0xFFFFCC80, 0xFF81D4FA, 0xFF7986CB, 0xFF69F0AE,
            0xFFFF5252, 0xFFFFB74D, 0xFFF48FB1
        )
        for (i in cinematicNames.indices) {
            add(FilterLut("cin_$i", cinematicNames[i], "Cinematic", cinTints[i], 5f, 15f, 10f, (i * 24f) % 360f))
        }

        // Cyberpunk 2050 Category (15 filters)
        val cyberNames = listOf(
            "Neo Tokyo 2050", "Laser Pink Synth", "Glitch Acid", "Hologram Blue",
            "Quantum Core", "Electric Violet", "Cybernetic Chrome", "Night City Horizon",
            "Neuro Linker", "Android Pulse", "Megacity Fog", "Data Stream",
            "Plasma Beam", "Cyborg Red Alert", "Orbital Flare"
        )
        val cyberTints = listOf(
            0xFF00F0FF, 0xFFFF007A, 0xFF76FF03, 0xFF40C4FF,
            0xFF7C4DFF, 0xFFE040FB, 0xFFECEFF1, 0xFFFF6E40,
            0xFF1DE9B6, 0xFFFF1744, 0xFF78909C, 0xFF00E676,
            0xFFD500F9, 0xFFFF3D00, 0xFFFFD600
        )
        for (i in cyberNames.indices) {
            add(FilterLut("cyber_$i", cyberNames[i], "Cyberpunk 2050", cyberTints[i], 10f, 25f, 30f, (i * 25f) % 360f))
        }

        // Retro VHS Category (12 filters)
        val retroNames = listOf(
            "VHS 1985 Tape", "Polaroid 600", "Super 8mm Film", "Kodak Portra 400",
            "Sepia Dust", "Cassette Rewind", "Camcorder 90s", "Vintage Arcade",
            "CRT Monitor Scan", "Faded Nostalgia", "Old Paris 1968", "Analog Drift"
        )
        for (i in retroNames.indices) {
            add(FilterLut("retro_$i", retroNames[i], "Retro VHS", 0xFFD7CCC8, -5f, 10f, -10f, (i * 30f) % 360f))
        }

        // Moody Teal (12 filters)
        val moodyNames = listOf(
            "Deep Teal Sea", "Arctic Ice", "Shadow Cyan", "Nordic Frost",
            "Midnight Ocean", "Abyssal Blue", "Cobalt Mist", "Rainy Window",
            "Steel Blue", "Submarine Gloom", "Glacier Fade", "Aquamarine Pro"
        )
        for (i in moodyNames.indices) {
            add(FilterLut("moody_$i", moodyNames[i], "Moody Teal", 0xFF00B4D8, -10f, 20f, 15f, (180f + i * 10f) % 360f))
        }

        // Anime Glow (12 filters)
        val animeNames = listOf(
            "Makoto Shinkai Sky", "Studio Ghibli Meadow", "Cyber Anime Spark", "Tokyo Sunset Pastel",
            "Cherry Blossom Bloom", "Neon Akihabara", "Kawaii Softness", "Fantasy Sunlight",
            "Manga Halftone", "Magical Girl Aura", "Mecha Horizon", "Dragon Breath Gold"
        )
        for (i in animeNames.indices) {
            add(FilterLut("anime_$i", animeNames[i], "Anime Glow", 0xFFFF80AB, 15f, 10f, 25f, (i * 28f) % 360f))
        }

        // Gold Sunset (12 filters)
        val goldNames = listOf(
            "California Sunset", "Sahara Warmth", "Golden Hour Glow", "Honey Amber",
            "Tuscan Vineyard", "Desert Dune Dusk", "Autumn Leaves", "Campfire Flame",
            "Copper Metallic", "Sunburst Ray", "Apricot Dream", "Moroccan Gold"
        )
        for (i in goldNames.indices) {
            add(FilterLut("gold_$i", goldNames[i], "Gold Sunset", 0xFFFFB300, 10f, 12f, 20f, (30f + i * 8f) % 360f))
        }

        // Black & White (12 filters)
        val bwNames = listOf(
            "High Contrast Monochrome", "Silver Gelatin", "Charcoal Shadow", "Newspaper Print",
            "Street Leica M", "Dark Velvet Noir", "Soft Grain B&W", "Dramatic Silhouette",
            "Studio Platinum", "Vintage Daguerreotype", "Moonlit Fog", "Pure Minimalist"
        )
        for (i in bwNames.indices) {
            add(FilterLut("bw_$i", bwNames[i], "Black & White", 0xFFCFD8DC, 0f, 35f, -100f, 0f))
        }

        // Urban Street (12 filters)
        val urbanNames = listOf(
            "NYC Concrete", "Tokyo Underground", "Berlin Techno", "London Rain",
            "Subway Graffiti", "Asphalt & Neon", "Industrial Steel", "Rooftop Smog",
            "Downtown Night", "Skatepark 90s", "Cyber Alleyway", "Neon Reflection"
        )
        for (i in urbanNames.indices) {
            add(FilterLut("urban_$i", urbanNames[i], "Urban Street", 0xFF90A4AE, 0f, 20f, 5f, (i * 32f) % 360f))
        }

        // Matrix Green & Sci-Fi Neon (16 filters)
        val scifiNames = listOf(
            "Matrix Digital Rain", "Emerald Toxic", "Bioluminescent Forest", "Radioactive Core",
            "Alien Xenomorph", "Hacker Terminal", "Neon Cyan Ray", "Ultraviolet Blacklight",
            "Quantum Strobe", "Warp Speed Violet", "Laser Red Target", "Stargate Vortex",
            "Cyber Reactor", "Supernova Blast", "Cosmic Dust", "Event Horizon"
        )
        val scifiTints = listOf(
            0xFF00FF66, 0xFF39FF14, 0xFF00FA9A, 0xFF76FF03,
            0xFF00E676, 0xFF1DE9B6, 0xFF00E5FF, 0xFFD500F9,
            0xFF651FFF, 0xFF3D5AFE, 0xFFFF1744, 0xFFFF007A,
            0xFFFF9100, 0xFFFFEA00, 0xFF00E5FF, 0xFFE040FB
        )
        for (i in scifiNames.indices) {
            add(FilterLut("scifi_$i", scifiNames[i], if (i < 8) "Matrix Green" else "Sci-Fi Neon", scifiTints[i], 8f, 25f, 35f, (i * 22f) % 360f))
        }
    }
}
