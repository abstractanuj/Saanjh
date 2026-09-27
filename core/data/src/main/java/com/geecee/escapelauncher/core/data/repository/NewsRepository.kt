package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.NewsDao
import com.geecee.escapelauncher.core.data.entity.NewsArticleEntity
import com.geecee.escapelauncher.core.data.network.NewsRemoteDataSource
import com.geecee.escapelauncher.core.model.NewsArticle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRepository @Inject constructor(
    private val newsDao: NewsDao,
    private val remoteDataSource: NewsRemoteDataSource
) {
    val newsArticles: Flow<List<NewsArticle>> = newsDao.getNewsArticles().map { entities ->
        entities.map { it.toModel() }
    }

    suspend fun refreshNews() {
        try {
            val remoteArticles = remoteDataSource.fetchNews()
            newsDao.insertArticles(remoteArticles.map { it.toEntity() })
        } catch (e: Exception) {
            // Log or handle error
        }
    }

    private fun NewsArticleEntity.toModel() = NewsArticle(
        id = id,
        title = title,
        description = description,
        url = url,
        imageUrl = imageUrl,
        source = source,
        publishedAt = publishedAt
    )

    private fun NewsArticle.toEntity() = NewsArticleEntity(
        id = id,
        title = title,
        description = description,
        url = url,
        imageUrl = imageUrl,
        source = source,
        publishedAt = publishedAt
    )
}
