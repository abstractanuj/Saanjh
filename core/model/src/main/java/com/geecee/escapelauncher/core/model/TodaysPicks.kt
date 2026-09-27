package com.geecee.escapelauncher.core.model

/**
 * Combined data for the "Today's Picks" sidebar.
 */
data class TodaysPicks(
    val articles: List<TodaysPickArticle>,
    val videos: List<TodaysPickVideo>,
    val lastUpdated: Long
) {
    init {
        require(lastUpdated >= 0) { "lastUpdated timestamp cannot be negative" }
    }
}
