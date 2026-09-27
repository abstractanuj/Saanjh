package com.geecee.escapelauncher.core.data.database

import androidx.room.*
import com.geecee.escapelauncher.core.data.entity.CalendarNoteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CalendarNoteDao {
    @Query("SELECT * FROM calendar_notes")
    fun getAllNotesFlow(): Flow<List<CalendarNoteEntity>>

    @Query("SELECT * FROM calendar_notes WHERE date = :date")
    suspend fun getNoteForDate(date: String): CalendarNoteEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: CalendarNoteEntity)

    @Delete
    suspend fun deleteNote(note: CalendarNoteEntity)
    
    @Query("DELETE FROM calendar_notes WHERE date = :date")
    suspend fun deleteNoteForDate(date: String)
}
