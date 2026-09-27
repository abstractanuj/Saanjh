package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todays_picks_articles")
data class TodaysPickArticleEntity(
    @PrimaryKey val id: String,
    val headline: String,
    val summary: String,
    val link: String,
    val source: String,
    val publishedAt: Long,
    val fetchedAt: Long,
    val isSummaryExpanded: Boolean = false
)
