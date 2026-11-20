package com.geecee.escapelauncher.core.data.di

import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused")
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindModifiedAppsRepository(
        modifiedAppsRepositoryImpl: ModifiedAppsRepositoryImpl
    ): ModifiedAppsRepository
}
