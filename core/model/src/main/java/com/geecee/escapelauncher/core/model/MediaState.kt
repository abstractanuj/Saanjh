package com.geecee.escapelauncher.core.model

data class MediaState(
    val isPlaying: Boolean = false,
    val title: String? = null,
    val artist: String? = null,
    val packageName: String? = null,
    val lastActiveTime: Long = 0L
)
