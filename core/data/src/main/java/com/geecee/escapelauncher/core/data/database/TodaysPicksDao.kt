package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.geecee.escapelauncher.core.data.entity.TodaysPickArticleEntity
import com.geecee.escapelauncher.core.data.entity.TodaysPickVideoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TodaysPicksDao {
    @Query("SELECT * FROM todays_picks_articles WHERE fetchedAt > :cutoffTime ORDER BY publishedAt DESC")
    fun getRecentArticles(cutoffTime: Long): Flow<List<TodaysPickArticleEntity>>
    
    @Query("SELECT * FROM todays_picks_videos WHERE fetchedAt > :cutoffTime ORDER BY publishedAt DESC")
    fun getRecentVideos(cutoffTime: Long): Flow<List<TodaysPickVideoEntity>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<TodaysPickArticleEntity>)
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertVideos(videos: List<TodaysPickVideoEntity>)
    
    @Query("DELETE FROM todays_picks_articles")
    suspend fun deleteAllArticles()
    
    @Query("DELETE FROM todays_picks_videos")
    suspend fun deleteAllVideos()

    @Transaction
    suspend fun refreshData(articles: List<TodaysPickArticleEntity>, videos: List<TodaysPickVideoEntity>) {
        deleteAllArticles()
        deleteAllVideos()
        if (articles.isNotEmpty()) insertArticles(articles)
        if (videos.isNotEmpty()) insertVideos(videos)
    }

    @Query("DELETE FROM todays_picks_articles WHERE fetchedAt < :cutoffTime")
    suspend fun deleteOldArticles(cutoffTime: Long)
    
    @Query("DELETE FROM todays_picks_videos WHERE fetchedAt < :cutoffTime")
    suspend fun deleteOldVideos(cutoffTime: Long)
}
