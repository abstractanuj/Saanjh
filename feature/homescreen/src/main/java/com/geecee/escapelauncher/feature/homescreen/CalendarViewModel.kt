package com.geecee.escapelauncher.feature.homescreen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.geecee.escapelauncher.core.data.database.CalendarNoteDao
import com.geecee.escapelauncher.core.data.entity.CalendarNoteEntity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val calendarNoteDao: CalendarNoteDao
) : ViewModel() {

    val allNotes: StateFlow<List<CalendarNoteEntity>> = calendarNoteDao.getAllNotesFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun saveNote(date: String, text: String) {
        viewModelScope.launch {
            if (text.isBlank()) {
                calendarNoteDao.deleteNoteForDate(date)
            } else {
                calendarNoteDao.insertNote(CalendarNoteEntity(date, text))
            }
        }
    }
}
