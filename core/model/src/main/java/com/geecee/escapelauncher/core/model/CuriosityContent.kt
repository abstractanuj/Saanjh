package com.geecee.escapelauncher.core.model

sealed class CuriosityContent {
    data class Question(val text: String) : CuriosityContent() {
        init { require(text.isNotBlank()) }
    }
    data class Music(val text: String, val songName: String) : CuriosityContent() {
        init {
            require(text.isNotBlank())
            require(songName.isNotBlank())
        }
    }
    data class Movie(val text: String, val movieName: String) : CuriosityContent() {
        init {
            require(text.isNotBlank())
            require(movieName.isNotBlank())
        }
    }
    data object None : CuriosityContent()
}
