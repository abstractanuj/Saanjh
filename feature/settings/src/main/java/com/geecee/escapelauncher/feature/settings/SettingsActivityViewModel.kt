package com.geecee.escapelauncher.feature.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.repository.SettingsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    private val settingsRepository: SettingsRepositoryImpl
) : ViewModel() {

    val settings = settingsRepository.settings

    fun updateReminderFrequency(hours: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsData()
            val newSettings = current.copy(reminderFrequencyHours = hours)
            settingsRepository.updateSettings(newSettings)
            // Ideally trigger reschedule of WorkManager here
        }
    }

    fun updateReminderTimeWindow(start: String, end: String) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsData()
            val newSettings = current.copy(reminderStartTime = start, reminderEndTime = end)
            settingsRepository.updateSettings(newSettings)
        }
    }

    fun updateRecommendationFrequency(hours: Int) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsData()
            val newSettings = current.copy(recommendationFrequencyHours = hours)
            settingsRepository.updateSettings(newSettings)
            // Ideally trigger reschedule of WorkManager here
        }
    }
    
    fun updateRecommendationTime(time: String) {
        viewModelScope.launch {
            val current = settingsRepository.getSettingsData()
            val newSettings = current.copy(recommendationTime = time)
            settingsRepository.updateSettings(newSettings)
        }
    }
}
