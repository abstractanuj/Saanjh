package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "calendar_notes")
data class CalendarNoteEntity(
    @PrimaryKey val date: String, // Format: yyyy-MM-dd
    val text: String
)
