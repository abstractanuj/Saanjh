package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.CuriosityQuestionEntity
import com.geecee.escapelauncher.core.data.entity.ContentMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CuriosityDao {
    @Query("SELECT * FROM curiosity_questions WHERE isUsed = 0 ORDER BY createdAt ASC LIMIT 1")
    fun getNextUnusedQuestion(): Flow<CuriosityQuestionEntity?>

    @Query("SELECT * FROM curiosity_questions WHERE isUsed = 1 ORDER BY usedAt DESC LIMIT 1")
    fun getLastUsedQuestion(): Flow<CuriosityQuestionEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuestions(questions: List<CuriosityQuestionEntity>)

    @Query("UPDATE curiosity_questions SET isUsed = 1, usedAt = :timestamp WHERE id = :id")
    suspend fun markAsUsed(id: Long, timestamp: Long)

    @Query("SELECT * FROM content_metadata WHERE type = 'questions'")
    suspend fun getMetadata(): ContentMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMetadata(metadata: ContentMetadataEntity)
}
