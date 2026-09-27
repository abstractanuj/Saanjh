package com.geecee.escapelauncher.core.model

/**
 * Sealed interface to group different types of "Today's Picks".
 */
sealed interface TodaysPickItem {
    val id: String
    val publishedAt: Long
}

/**
 * Domain model for a long-form article headline.
 */
data class TodaysPickArticle(
    override val id: String,     // Unique ID (often the URL)
    val headline: String,        // Article title
    val summary: String,         // 2-3 line summary
    val link: String,            // Full article URL
    val source: String,          // "Aeon", "Psyche", etc.
    override val publishedAt: Long, // Timestamp
    val isSummaryExpanded: Boolean = false
) : TodaysPickItem {
    init {
        require(id.isNotBlank()) { "Article ID cannot be blank" }
        require(headline.isNotBlank()) { "Article headline cannot be blank" }
        require(link.isNotBlank()) { "Article link cannot be blank" }
    }
}
