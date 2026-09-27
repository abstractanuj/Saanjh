package com.geecee.escapelauncher.core.data.di

import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepository
import com.geecee.escapelauncher.core.data.repository.ModifiedAppsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Suppress("unused") // It is used by the generated build code.
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    @Singleton
    abstract fun bindModifiedAppsRepository(
        modifiedAppsRepositoryImpl: ModifiedAppsRepositoryImpl
    ): ModifiedAppsRepository

    @Binds
    @Singleton
    abstract fun bindTodaysPicksRepository(
        impl: com.geecee.escapelauncher.core.data.repository.TodaysPicksRepositoryImpl
    ): com.geecee.escapelauncher.core.domain.repository.TodaysPicksRepository
}
