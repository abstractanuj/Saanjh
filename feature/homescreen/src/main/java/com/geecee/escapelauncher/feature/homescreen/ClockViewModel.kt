package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import com.geecee.escapelauncher.domain.time.GetCurrentTimePartsUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.LocalTime
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ClockViewModel @Inject constructor(
    getCurrentTimeParts: GetCurrentTimePartsUseCase,
) : ViewModel() {

    private val _timeParts = MutableStateFlow(getCurrentTimeParts.invoke(LocalTime.now(), twelveHourDisplay = false))
    val timeParts: StateFlow<Triple<Int, Int, Boolean>> = _timeParts.asStateFlow()

    override fun onCleared() {
        // Super call is empty in ViewModel, so it can be omitted
    }
}
