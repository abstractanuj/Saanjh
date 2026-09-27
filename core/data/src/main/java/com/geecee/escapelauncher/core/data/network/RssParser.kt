package com.geecee.escapelauncher.core.data.network

import androidx.core.text.HtmlCompat
import com.geecee.escapelauncher.core.model.TodaysPickArticle
import com.geecee.escapelauncher.core.model.TodaysPickVideo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserFactory
import java.io.StringReader
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import javax.inject.Inject
import javax.inject.Singleton
import timber.log.Timber

interface RssParser {
    suspend fun fetchAndParseArticleFeed(url: String, sourceName: String): Result<List<TodaysPickArticle>>
    suspend fun fetchAndParseYoutubeFeed(url: String, channelName: String): Result<List<TodaysPickVideo>>
    fun parseArticleFeed(xml: String, sourceName: String): Result<List<TodaysPickArticle>>
    fun parseYoutubeFeed(xml: String, channelName: String): Result<List<TodaysPickVideo>>
}

@Singleton
class SimpleRssParser @Inject constructor(
    private val rssFeedService: RssFeedService
) : RssParser {

    private val rssDateFormat = SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss Z", Locale.US)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
        timeZone = TimeZone.getTimeZone("UTC")
    }

    private val parserFactory = XmlPullParserFactory.newInstance().apply {
        isNamespaceAware = true
    }

    private val contentNamespace = "http://purl.org/rss/1.0/modules/content/"

    override suspend fun fetchAndParseArticleFeed(url: String, sourceName: String): Result<List<TodaysPickArticle>> = withContext(Dispatchers.IO) {
        try {

            val response = rssFeedService.fetchFeed(url)
            if (!response.isSuccessful) {
                Timber.tag("TodaysPicks").e("Article feed HTTP error: ${response.code()} for $sourceName")
                return@withContext Result.failure(Exception("HTTP ${response.code()}"))
            }
            val body = response.body()?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            parseArticleFeed(body, sourceName)
        } catch (e: Exception) {
            Timber.tag("TodaysPicks").e("Failed to fetch $url: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun fetchAndParseYoutubeFeed(url: String, channelName: String): Result<List<TodaysPickVideo>> = withContext(Dispatchers.IO) {
        try {

            val response = rssFeedService.fetchFeed(url)
            if (!response.isSuccessful) {
                val errorBody = response.errorBody()?.string()?.take(200)
                Timber.tag("TodaysPicks").e("YouTube feed HTTP error: ${response.code()} for $channelName. Body: $errorBody")
                return@withContext Result.failure(Exception("HTTP ${response.code()}"))
            }
            val body = response.body()?.string() ?: return@withContext Result.failure(Exception("Empty body"))
            parseYoutubeFeed(body, channelName).also {
                if (it.isSuccess) {
                    val count = it.getOrNull()?.size ?: 0
                    if (count == 0) {
                        Timber.tag("TodaysPicks").w("YouTube feed parsed but found 0 videos for $channelName. Body snippet: ${body.take(200)}")
                    }
                }
            }
        } catch (e: Exception) {
            Timber.tag("TodaysPicks").e("Failed to fetch YouTube $url: ${e.message}")
            Result.failure(e)
        }
    }

    override fun parseArticleFeed(xml: String, sourceName: String): Result<List<TodaysPickArticle>> {
        return try {
            val articles = mutableListOf<TodaysPickArticle>()
            val parser = parserFactory.newPullParser()
            parser.setInput(StringReader(xml))

            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG && parser.name == "item") {
                    articles.add(parseArticleItem(parser, sourceName))
                }
                eventType = parser.next()
            }
            Result.success(articles)
        } catch (e: Exception) {
            Timber.tag("TodaysPicks").e("Failed to parse $sourceName: ${e.message}")
            Result.failure(e)
        }
    }

    override fun parseYoutubeFeed(xml: String, channelName: String): Result<List<TodaysPickVideo>> {
        return try {
            val videos = mutableListOf<TodaysPickVideo>()
            val parser = parserFactory.newPullParser()
            parser.setInput(StringReader(xml))

            var actualChannelName = channelName
            var eventType = parser.eventType
            while (eventType != XmlPullParser.END_DOCUMENT) {
                if (eventType == XmlPullParser.START_TAG) {
                    when (parser.name) {
                        "title" -> {
                            // The first <title> in the feed is usually the channel name
                            // We check depth == 2 because depth 1 is <feed>
                            if (parser.depth == 2) {
                                val feedTitle = parser.nextText().cleanChannelName()
                                if (feedTitle.isNotEmpty()) {
                                    actualChannelName = feedTitle
                                }
                            }
                        }
                        "entry" -> {
                            val video = parseYoutubeEntry(parser, actualChannelName)
                            // Filter out Shorts based on URL
                            if (!video.youtubeLink.contains("/shorts/")) {
                                videos.add(video)
                            }
                        }
                    }
                }
                eventType = parser.next()
            }
            Result.success(videos)
        } catch (e: Exception) {
            Timber.tag("TodaysPicks").e("Failed to parse $channelName: ${e.message}")
            Result.failure(e)
        }
    }

    private fun parseArticleItem(parser: XmlPullParser, sourceName: String): TodaysPickArticle {
        var title = ""
        var link = ""
        var description = ""
        var pubDate = 0L

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "item")) {
            if (eventType == XmlPullParser.START_TAG) {
                when {
                    parser.name == "title" -> title = parser.nextText()
                    parser.name == "link" -> link = parser.nextText()
                    parser.name == "description" || (parser.namespace == contentNamespace && parser.name == "encoded") -> {
                        val text = try { parser.nextText().stripHtml() } catch (e: Exception) { "" }
                        if (text.length > description.length) {
                            description = text
                        }
                    }
                    parser.name == "pubDate" -> {
                        val dateStr = parser.nextText()
                        pubDate = try { rssDateFormat.parse(dateStr)?.time ?: 0L } catch (e: Exception) { 0L }
                    }
                }
            }
            eventType = parser.next()
        }

        // Clean up title and summary from common RSS artifacts
        val cleanTitle = title.trim().replace(Regex("\\s+"), " ")
        val cleanSummary = description.trim()
            .replace(Regex("\\s+"), " ")
            .take(500) // Max summary length

        return TodaysPickArticle(
            id = link,
            headline = cleanTitle,
            summary = cleanSummary,
            link = link,
            source = sourceName,
            publishedAt = pubDate
        )
    }

    private fun parseYoutubeEntry(parser: XmlPullParser, channelName: String): TodaysPickVideo {
        var title = ""
        var link = ""
        var published = 0L
        var videoId = ""

        var eventType = parser.next()
        while (!(eventType == XmlPullParser.END_TAG && parser.name == "entry")) {
            if (eventType == XmlPullParser.START_TAG) {
                val name = parser.name
                when {
                    name == "title" -> title = parser.nextText()
                    name == "link" -> {
                        val href = parser.getAttributeValue(null, "href")
                        if (href != null) link = href
                    }
                    name == "published" -> {
                        val dateStr = parser.nextText()
                        published = try { isoDateFormat.parse(dateStr)?.time ?: 0L } catch (e: Exception) { 0L }
                    }
                    name.contains("videoId") -> {
                        videoId = parser.nextText()
                    }
                }
            }
            eventType = parser.next()
        }

        // Outside-the-box fallback: If videoId is still empty, scrape it from the link or ID tag
        if (videoId.isEmpty() && link.isNotEmpty()) {
            videoId = link.substringAfter("v=", "").substringBefore("&", "")
            if (videoId.isEmpty()) {
                videoId = link.substringAfterLast("/")
            }
        }

        return TodaysPickVideo(
            id = videoId.ifEmpty { java.util.UUID.randomUUID().toString() },
            title = title.ifEmpty { "YouTube Video" },
            youtubeLink = if (link.startsWith("http")) link else "https://www.youtube.com/watch?v=$videoId",
            channel = channelName,
            publishedAt = published
        )
    }

    private fun String.stripHtml(): String {
        // Remove HTML tags using Android's Html tool
        val spanned = HtmlCompat.fromHtml(this, HtmlCompat.FROM_HTML_MODE_LEGACY)
        val str = StringBuilder()
        // Manually filter out Object Replacement Characters (\uFFFC) which represent images/videos
        for (i in 0 until spanned.length) {
            val c = spanned[i]
            if (c.code != 0xFFFC && c.code != 0xFFFD) {
                str.append(c)
            }
        }
        
        return str.toString()
            .replace("\u00A0", " ") // Non-breaking space
            .replace(Regex("\\s+"), " ") // Collapse multiple spaces/newlines
            .trim()
    }

    private fun String.cleanChannelName(): String {
        return this.replace(" - YouTube", "", ignoreCase = true)
            .replace("- YouTube", "", ignoreCase = true)
            .replace(" YouTube", "", ignoreCase = true)
            .trim()
    }
}
