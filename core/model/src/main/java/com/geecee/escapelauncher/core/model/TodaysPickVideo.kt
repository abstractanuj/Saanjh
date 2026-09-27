package com.geecee.escapelauncher.core.model

/**
 * Domain model for a YouTube video pick.
 */
data class TodaysPickVideo(
    override val id: String,     // Video ID (from YouTube feed)
    val title: String,           // Video title
    val youtubeLink: String,     // https://youtube.com/watch?v=ID
    val channel: String,         // "TED Talks", etc.
    override val publishedAt: Long, // Upload timestamp
    val duration: String? = null // Optional: "22:45"
) : TodaysPickItem {
    init {
        require(id.isNotBlank()) { "Video ID cannot be blank" }
        require(title.isNotBlank()) { "Video title cannot be blank" }
        require(youtubeLink.startsWith("http")) { "Invalid YouTube link: $youtubeLink" }
    }
}
