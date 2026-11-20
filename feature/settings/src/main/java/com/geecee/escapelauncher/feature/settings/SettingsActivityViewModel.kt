package com.geecee.escapelauncher.feature.settings

import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.core.data.repository.SettingsRepositoryImpl
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class SettingsActivityViewModel @Inject constructor(
    settingsRepository: SettingsRepositoryImpl
) : ViewModel() {
    val settings = settingsRepository.settings
}
