package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "music_recommendations")
data class MusicRecommendationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String, // e.g., "Moonlight Sonata - Beethoven"
    val createdAt: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false,
    val usedAt: Long? = null
)
