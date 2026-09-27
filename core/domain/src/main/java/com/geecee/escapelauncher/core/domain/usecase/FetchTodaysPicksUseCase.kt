package com.geecee.escapelauncher.core.domain.usecase

import com.geecee.escapelauncher.core.domain.repository.TodaysPicksRepository
import com.geecee.escapelauncher.core.model.TodaysPicks
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

/**
 * Use case to fetch and refresh "Today's Picks".
 * Handles stale check (6 hours) and returns the data stream.
 */
class FetchTodaysPicksUseCase @Inject constructor(
    private val repository: TodaysPicksRepository
) {
    operator fun invoke(): Flow<TodaysPicks> {
        // We use a very long cutoff for the UI stream so data doesn't disappear 
        // if a background refresh fails. The 6h logic is handled in refreshIfNeeded.
        val oneWeekInMillis = 7 * 24 * 60 * 60 * 1000L
        val cutoffTime = System.currentTimeMillis() - oneWeekInMillis
        
        return repository.getTodaysPicks(cutoffTime)
    }

    suspend fun refreshIfNeeded() {
        val sixHoursInMillis = 6 * 60 * 60 * 1000L
        val cutoffTime = System.currentTimeMillis() - sixHoursInMillis
        
        val currentPicks = repository.getTodaysPicks(cutoffTime).first()
        
        // Only refresh if data is older than 6 hours
        if (System.currentTimeMillis() - currentPicks.lastUpdated > sixHoursInMillis) {
            repository.refreshTodaysPicks()
        }
    }
}
