package com.geecee.escapelauncher.core.model

data class DailyRecommendation(
    val morningQuestion: String,
    val eveningFilmTitle: String,
    val eveningFilmDirector: String,
    val eveningPrompt: String
)