package com.geecee.escapelauncher.core.model

/**
 * Categories for grouping apps in the app list.
 */
data class AppCategory(val name: String, val displayName: String) {
    companion object {
        val TRAVEL = AppCategory("TRAVEL", "Travel")
        val SOCIAL = AppCategory("SOCIAL", "Social")
        val ENTERTAINMENT = AppCategory("ENTERTAINMENT", "Entertainment")
        val FINANCE = AppCategory("FINANCE", "Finance")
        val GAMES = AppCategory("GAMES", "Games")
        val WELLBEING = AppCategory("WELLBEING", "Wellbeing")
        
        val entries = listOf(TRAVEL, SOCIAL, ENTERTAINMENT, FINANCE, GAMES, WELLBEING)

        fun fromString(value: String): AppCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) }
                ?: AppCategory(value.uppercase(), value)
        }
    }
}
