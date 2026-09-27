package com.geecee.escapelauncher.core.domain.repository

import com.geecee.escapelauncher.core.model.TodaysPicks
import kotlinx.coroutines.flow.Flow

interface TodaysPicksRepository {
    fun getTodaysPicks(cutoffTime: Long): Flow<TodaysPicks>
    suspend fun refreshTodaysPicks(): Result<Unit>
}
