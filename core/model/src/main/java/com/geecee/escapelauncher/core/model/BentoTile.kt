package com.geecee.escapelauncher.core.model

/**
 * Represents a piece of content that can live inside a Bento Tile.
 */
sealed interface BentoContent {
    data class App(val packageName: String) : BentoContent
    data class Contact(val id: Long) : BentoContent
    data class Weather(val condition: String, val temperature: Int) : BentoContent
    data class Media(val title: String, val artist: String) : BentoContent
    data class News(val headline: String, val source: String) : BentoContent
    data class Photo(val uri: String) : BentoContent
}

/**
 * Backend infrastructure for "Self card switch/slide".
 * A Tile can hold multiple contents and cycle through them.
 */
data class DynamicTile(
    val id: String,
    val contents: List<BentoContent>,
    val currentContentIndex: Int = 0,
    val autoCycleEnabled: Boolean = false
) {
    init {
        require(id.isNotBlank()) { "Tile ID cannot be blank" }
        if (contents.isNotEmpty()) {
            require(currentContentIndex in contents.indices) {
                "currentContentIndex ($currentContentIndex) out of bounds for contents size (${contents.size})"
            }
        } else {
            require(currentContentIndex == 0) { "Index must be 0 for empty contents" }
        }
    }

    /**
     * Helper to get the current visible content.
     */
    val currentContent: BentoContent? get() = contents.getOrNull(currentContentIndex)
}
