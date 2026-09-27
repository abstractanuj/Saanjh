package com.geecee.escapelauncher.core.data.network

import com.geecee.escapelauncher.core.model.NewsArticle
import com.google.gson.Gson
import okhttp3.OkHttpClient
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewsRemoteDataSource @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private val gson = Gson()

    suspend fun fetchNews(): List<NewsArticle> {
        // Placeholder implementation
        return listOf(
            NewsArticle(
                id = "1",
                title = "Welcome to Escape Launcher News",
                description = "Stay updated with the latest headlines right here.",
                url = "https://github.com/geecee/Escape-Launcher",
                imageUrl = null,
                source = "Escape Launcher",
                publishedAt = System.currentTimeMillis()
            )
        )
    }
}
