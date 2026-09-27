package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.TodaysPicksDao
import com.geecee.escapelauncher.core.data.entity.TodaysPickArticleEntity
import com.geecee.escapelauncher.core.data.entity.TodaysPickVideoEntity
import com.geecee.escapelauncher.core.data.network.RssParser
import com.geecee.escapelauncher.core.domain.repository.SettingsRepository
import com.geecee.escapelauncher.core.domain.repository.TodaysPicksRepository
import com.geecee.escapelauncher.core.model.TodaysPickArticle
import com.geecee.escapelauncher.core.model.TodaysPickVideo
import com.geecee.escapelauncher.core.model.TodaysPicks
import com.geecee.escapelauncher.core.common.AppLogger
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

@Singleton
class TodaysPicksRepositoryImpl @Inject constructor(
    private val rssParser: RssParser,
    private val todaysPicksDao: TodaysPicksDao,
    private val settingsRepository: SettingsRepository
) : TodaysPicksRepository {

    private val articleSources = mapOf(
        "Aeon" to "https://aeon.co/feed.rss",
        "Psyche" to "https://psyche.co/feed.rss",
        "Wait But Why" to "https://waitbutwhy.com/feed",
        "Orion Magazine" to "https://orionmagazine.org/feed/",
        "Lithub" to "https://lithub.com/category/fictionandpoetry/feed/",
        "The School of Life" to "https://www.theschooloflife.com/articles/feed/"
    )

    companion object {
        private const val YOUTUBE_RSS_BASE_URL = "https://www.youtube.com/feeds/videos.xml?channel_id="
    }

    override fun getTodaysPicks(cutoffTime: Long): Flow<TodaysPicks> {
        return combine(
            todaysPicksDao.getRecentArticles(cutoffTime),
            todaysPicksDao.getRecentVideos(cutoffTime)
        ) { articles, videos ->
            TodaysPicks(
                articles = articles.map { it.toDomain() },
                videos = videos.map { it.toDomain() },
                lastUpdated = (articles.map { it.fetchedAt } + videos.map { it.fetchedAt }).maxOrNull() ?: 0L
            )
        }
    }

    override suspend fun refreshTodaysPicks(): Result<Unit> = coroutineScope {
        try {
            val now = System.currentTimeMillis()
            val customChannels = settingsRepository.customYoutubeChannels.first()

            
            val youtubeSources = customChannels.associate { it.id to it.name }

            val articleDeferreds = articleSources.map { (name, url) ->
                async { 
                    retry(3) {
                        withTimeout(10000) {
                            rssParser.fetchAndParseArticleFeed(url, name)
                        }
                    }
                }
            }
            
            val videoDeferreds = youtubeSources.map { (id, name) ->
                async { 
                    retry(3) {
                        withTimeout(10000) {
                            rssParser.fetchAndParseYoutubeFeed("$YOUTUBE_RSS_BASE_URL$id", name)
                        }
                    }
                }
            }

            val articleResults = articleDeferreds.awaitAll()
            val videoResults = videoDeferreds.awaitAll()

            val bestArticles = articleResults
                .mapNotNull { it.getOrNull()?.firstOrNull() }
                .map { it.toEntity(now) }

            val bestVideos = videoResults
                .mapNotNull { it.getOrNull()?.firstOrNull() }
                .map { it.toEntity(now) }



            if (bestArticles.isEmpty() && bestVideos.isEmpty() && (articleSources.isNotEmpty() || youtubeSources.isNotEmpty())) {
                AppLogger.w("TodaysPicks: Refresh failed to get any new data.")
                return@coroutineScope Result.failure(Exception("No new data fetched"))
            }

            todaysPicksDao.refreshData(bestArticles, bestVideos)


            Result.success(Unit)
        } catch (e: Exception) {
            AppLogger.e("TodaysPicks: Background refresh failed: ${e.message}", e)
            Result.failure(e)
        }
    }

    private suspend fun <T> retry(
        times: Int,
        initialDelay: Long = 1000,
        factor: Double = 2.0,
        block: suspend () -> Result<T>
    ): Result<T> {
        var currentDelay = initialDelay
        repeat(times - 1) {
            val result = block()
            if (result.isSuccess) return result
            delay(currentDelay)
            currentDelay = (currentDelay * factor).toLong()
        }
        return block()
    }

    private fun TodaysPickArticleEntity.toDomain() = TodaysPickArticle(
        id = id,
        headline = headline,
        summary = summary,
        link = link,
        source = source,
        publishedAt = publishedAt,
        isSummaryExpanded = isSummaryExpanded
    )

    private fun TodaysPickVideoEntity.toDomain() = TodaysPickVideo(
        id = id,
        title = title,
        youtubeLink = youtubeLink,
        channel = channel,
        publishedAt = publishedAt,
        duration = duration
    )

    private fun TodaysPickArticle.toEntity(fetchedAt: Long) = TodaysPickArticleEntity(
        id = id,
        headline = headline,
        summary = summary,
        link = link,
        source = source,
        publishedAt = publishedAt,
        fetchedAt = fetchedAt,
        isSummaryExpanded = isSummaryExpanded
    )

    private fun TodaysPickVideo.toEntity(fetchedAt: Long) = TodaysPickVideoEntity(
        id = id,
        title = title,
        youtubeLink = youtubeLink,
        channel = channel,
        publishedAt = publishedAt,
        fetchedAt = fetchedAt,
        duration = duration
    )
}
