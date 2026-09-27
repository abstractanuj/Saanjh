package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "curiosity_questions",
    indices = [
        Index(value = ["isUsed"]),
        Index(value = ["createdAt"]),
        Index(value = ["usedAt"])
    ]
)
data class CuriosityQuestionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val text: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isUsed: Boolean = false,
    val usedAt: Long? = null
)
