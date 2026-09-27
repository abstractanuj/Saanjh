package com.geecee.escapelauncher.core.model

data class NewsArticle(
    val id: String,
    val title: String,
    val description: String?,
    val url: String,
    val imageUrl: String?,
    val source: String,
    val publishedAt: Long
)
