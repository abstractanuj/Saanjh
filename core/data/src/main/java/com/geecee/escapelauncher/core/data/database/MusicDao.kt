package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.MusicRecommendationEntity
import com.geecee.escapelauncher.core.data.entity.ContentMetadataEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MusicDao {
    @Query("SELECT * FROM music_recommendations WHERE isUsed = 0 ORDER BY createdAt ASC LIMIT 1")
    fun getNextUnusedRecommendation(): Flow<MusicRecommendationEntity?>

    @Query("SELECT * FROM music_recommendations WHERE isUsed = 1 ORDER BY usedAt DESC LIMIT 1")
    fun getLastUsedRecommendation(): Flow<MusicRecommendationEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecommendations(recommendations: List<MusicRecommendationEntity>)

    @Query("UPDATE music_recommendations SET isUsed = 1, usedAt = :timestamp WHERE id = :id")
    suspend fun markAsUsed(id: Long, timestamp: Long)

    @Query("SELECT * FROM content_metadata WHERE type = 'music'")
    suspend fun getMetadata(): ContentMetadataEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun updateMetadata(metadata: ContentMetadataEntity)
}
