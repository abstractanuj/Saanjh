package com.geecee.escapelauncher.core.common

import com.geecee.escapelauncher.core.model.AppCategory

/**
 * Utility to classify apps into categories based on package names or other metadata.
 */
object AppClassifier {
    
    private val travelPackages = setOf(
        "com.google.android.apps.maps",
        "com.ubercab",
        "com.airbnb.android",
        "com.booking",
        "com.expedia.bookings",
        "com.tripadvisor.tripadvisor",
        "it.greenroad.ola",
        "com.grabtaxi.passenger",
        "com.skyscanner.android",
        "com.hopper.android",
        "com.kayak.android",
        "com.waze"
    )

    private val socialPackages = setOf(
        "com.facebook.katana",
        "com.instagram.android",
        "com.twitter.android",
        "com.zhiliaoapp.musically", // TikTok
        "com.whatsapp",
        "com.snapchat.android",
        "com.reddit.frontpage",
        "com.linkedin.android",
        "org.telegram.messenger",
        "com.discord"
    )

    private val entertainmentPackages = setOf(
        "com.netflix.mediaclient",
        "com.google.android.youtube",
        "com.spotify.music",
        "com.disney.disneyplus",
        "com.amazon.avod.thirdpartyclient", // Prime Video
        "com.hulu.plus",
        "com.google.android.videos"
    )

    private val gamesPackages = setOf(
        "com.kiloo.subwaysurf",
        "com.roblox.client",
        "com.mojang.minecraftpe",
        "com.supercell.clashofclans",
        "com.activision.callofduty.shooter",
        "com.tencent.ig",
        "com.nianticlabs.pokemongo"
    )

    private val wellbeingPackages = setOf(
        "com.google.android.apps.wellbeing",
        "com.getsomeheadspace.android",
        "com.calm.android",
        "com.fitnesskeeper.runkeeper.pro",
        "com.myfitnesspal.android",
        "com.strava"
    )

    private val financePackages = setOf(
        "com.paypal.android.p2pmobile",
        "com.venmo",
        "com.revolut.revolut",
        "com.google.android.apps.walletnfcrel", // Google Wallet
        "com.bloomberg.android",
        "com.etoro.openbook"
    )

    fun classify(packageName: String, manualOverrides: Map<String, String> = emptyMap()): AppCategory? {
        // 1. Check for manual overrides first
        manualOverrides[packageName]?.let { manualCategoryName ->
            return AppCategory.fromString(manualCategoryName)
        }

        // 2. Fallback to heuristic classification
        return when {
            travelPackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.TRAVEL
            socialPackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.SOCIAL
            entertainmentPackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.ENTERTAINMENT
            gamesPackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.GAMES
            wellbeingPackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.WELLBEING
            financePackages.any { packageName.contains(it, ignoreCase = true) } -> AppCategory.FINANCE
            else -> null
        }
    }
}
