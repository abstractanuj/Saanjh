package com.geecee.escapelauncher.core.data.di

import android.content.Context
import androidx.room.Room
import com.geecee.escapelauncher.core.data.database.AppDatabase
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDao
import com.geecee.escapelauncher.core.data.database.ModifiedAppsDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideAppDatabase(
        @ApplicationContext context: Context
    ): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "app_usage_database"
        )
        .fallbackToDestructiveMigration(true) // Version 1 -> 2 with TodoEntity
        .build()
    }


    @Provides
    fun provideCalendarNoteDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.CalendarNoteDao {
        return database.calendarNoteDao()
    }

    @Provides
    fun provideContactDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.ContactDao {
        return database.contactDao()
    }

    @Provides
    fun provideCuriosityDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.CuriosityDao {
        return database.curiosityDao()
    }

    @Provides
    fun provideMusicDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.MusicDao {
        return database.musicDao()
    }

    @Provides
    fun provideNewsDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.NewsDao {
        return database.newsDao()
    }

    @Provides
    fun provideTodaysPicksDao(database: AppDatabase): com.geecee.escapelauncher.core.data.database.TodaysPicksDao {
        return database.todaysPicksDao()
    }

    @Provides
    @Singleton
    fun provideModifiedAppsDatabase(
        @ApplicationContext context: Context
    ): ModifiedAppsDatabase {
        return Room.databaseBuilder(
            context = context,
            klass = ModifiedAppsDatabase::class.java,
            "modified_apps_database"
        )
        .fallbackToDestructiveMigration(true)
        .build()
    }

    @Provides
    fun provideModifiedAppsDao(database: ModifiedAppsDatabase): ModifiedAppsDao {
        return database.modifiedAppsDao()
    }
}
