package com.geecee.escapelauncher.core.data.di

import android.content.Context
import androidx.room.Room
import com.geecee.escapelauncher.core.data.database.AppDatabase
import com.geecee.escapelauncher.core.data.database.ContactDao
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
            .fallbackToDestructiveMigration(true)
            .build()
    }

    @Provides
    fun provideContactDao(database: AppDatabase): ContactDao = database.contactDao()

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
    fun provideModifiedAppsDao(database: ModifiedAppsDatabase): ModifiedAppsDao = database.modifiedAppsDao()
}
