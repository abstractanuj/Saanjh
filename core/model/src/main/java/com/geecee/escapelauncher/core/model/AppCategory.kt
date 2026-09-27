package com.geecee.escapelauncher.core.model

/**
 * Categories for grouping apps in the app list.
 */
enum class AppCategory(val displayName: String) {
    TRAVEL("Travel"),
    SOCIAL("Social"),
    TOOLS("Tools"),
    ENTERTAINMENT("Entertainment"),
    PRODUCTIVITY("Productivity"),
    FINANCE("Finance"),
    OTHERS("Others");

    companion object {
        fun fromString(value: String): AppCategory {
            return entries.find { it.name.equals(value, ignoreCase = true) } ?: OTHERS
        }
    }
}
