package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "content_metadata")
data class ContentMetadataEntity(
    @PrimaryKey val type: String, // "questions", "music"
    val lastGeneratedAt: Long
)
