package com.geecee.escapelauncher.core.data.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.geecee.escapelauncher.core.data.entity.CalendarNoteEntity
import com.geecee.escapelauncher.core.data.entity.ContactEntity
import com.geecee.escapelauncher.core.data.entity.MusicRecommendationEntity
import com.geecee.escapelauncher.core.data.entity.ContentMetadataEntity
import com.geecee.escapelauncher.core.data.entity.NewsArticleEntity
import com.geecee.escapelauncher.core.data.entity.TodaysPickArticleEntity
import com.geecee.escapelauncher.core.data.entity.TodaysPickVideoEntity

@Database(
    entities = [
        CalendarNoteEntity::class,
        ContactEntity::class,
        MusicRecommendationEntity::class,
        ContentMetadataEntity::class,
        NewsArticleEntity::class,
        TodaysPickArticleEntity::class,
        TodaysPickVideoEntity::class
    ],
    version = 14,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun calendarNoteDao(): CalendarNoteDao
    abstract fun contactDao(): ContactDao
    abstract fun musicDao(): MusicDao
    abstract fun newsDao(): NewsDao
    abstract fun todaysPicksDao(): TodaysPicksDao
}
