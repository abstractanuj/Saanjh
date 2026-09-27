package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "news_articles")
data class NewsArticleEntity(
    @PrimaryKey val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val publishedAt: Long,
    val fetchedAt: Long = System.currentTimeMillis()
)
