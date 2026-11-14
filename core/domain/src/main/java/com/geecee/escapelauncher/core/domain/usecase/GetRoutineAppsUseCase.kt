package com.geecee.escapelauncher.core.domain.usecase

import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flatMapLatest
import java.util.Calendar
import javax.inject.Inject

class GetRoutineAppsUseCase @Inject constructor(
    private val settingsRepository: SettingsRepository
) {
    /**
     * Returns a flow of a set of package names corresponding to the current routine based on the time of day.
     * Morning: 5:00 - 11:59
     * Afternoon: 12:00 - 16:59
     * Evening: 17:00 - 20:59
     * Night: 21:00 - 4:59
     */
    operator fun invoke(): Flow<Set<String>> {
        // Emit the current time category, and update it every minute (or whenever it changes)
        val timeCategoryFlow = flow {
            while (true) {
                val calendar = Calendar.getInstance()
                val hour = calendar.get(Calendar.HOUR_OF_DAY)
                val category = when (hour) {
                    in 5..11 -> TimeCategory.MORNING
                    in 12..16 -> TimeCategory.AFTERNOON
                    in 17..20 -> TimeCategory.EVENING
                    else -> TimeCategory.NIGHT
                }
                emit(category)
                // Sleep until the top of the next minute
                val now = System.currentTimeMillis()
                val nextMinute = ((now / 60000) + 1) * 60000
                delay(nextMinute - now)
            }
        }

        return timeCategoryFlow.flatMapLatest { category ->
            when (category) {
                TimeCategory.MORNING -> settingsRepository.routineAppsMorning
                TimeCategory.AFTERNOON -> settingsRepository.routineAppsAfternoon
                TimeCategory.EVENING -> settingsRepository.routineAppsEvening
                TimeCategory.NIGHT -> settingsRepository.routineAppsNight
            }
        }
    }

    enum class TimeCategory {
        MORNING, AFTERNOON, EVENING, NIGHT
    }
}
