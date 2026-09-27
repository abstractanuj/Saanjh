package com.geecee.escapelauncher.core.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "modifiedApps",
    indices = [
        Index(value = ["favouritePosition"]),
        Index(value = ["isHidden"]),
        Index(value = ["isChallenge"])
    ]
)
data class ModifiedAppEntity(
    @PrimaryKey val packageId: String,
    val displayName: String?,
    val isHidden: Boolean,
    val isChallenge: Boolean,
    val favouritePosition: Double?,
    val category: String? = null,
    val themeColor: Int? = null,
    val installTime: Long? = null
)
