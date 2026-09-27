package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "todays_picks_videos")
data class TodaysPickVideoEntity(
    @PrimaryKey val id: String,
    val title: String,
    val youtubeLink: String,
    val channel: String,
    val publishedAt: Long,
    val fetchedAt: Long,
    val duration: String? = null
)
