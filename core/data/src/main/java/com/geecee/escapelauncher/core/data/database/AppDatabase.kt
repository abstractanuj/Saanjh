package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.ContactEntity

@Database(
    entities = [
        ContactEntity::class
    ],
    version = 17,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun contactDao(): ContactDao
}
