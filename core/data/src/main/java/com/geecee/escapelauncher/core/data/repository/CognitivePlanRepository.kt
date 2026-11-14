package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.model.DailyRecommendation
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CognitivePlanRepository @Inject constructor() {
    suspend fun getTodayRecommendation(): DailyRecommendation {
        return DailyRecommendation(
            morningQuestion = "Deleted Cognitive Question",
            eveningFilmTitle = "Stalker",
            eveningFilmDirector = "Andrei Tarkovsky",
            eveningPrompt = "A journey into the human soul."
        )
    }
}