package com.geecee.escapelauncher.core.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.geecee.escapelauncher.core.data.entity.NewsArticleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface NewsDao {
    @Query("SELECT * FROM news_articles ORDER BY publishedAt DESC")
    fun getNewsArticles(): Flow<List<NewsArticleEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticles(articles: List<NewsArticleEntity>)

    @Query("DELETE FROM news_articles")
    suspend fun clearAll()
}
