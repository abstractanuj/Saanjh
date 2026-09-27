package com.geecee.escapelauncher.core.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DataModelTest {
    
    @Test
    fun testTodaysPickArticleCreation() {
        val article = TodaysPickArticle(
            id = "1",
            headline = "Test Article",
            summary = "This is a test summary",
            link = "https://example.com",
            source = "Aeon",
            publishedAt = System.currentTimeMillis(),
            isSummaryExpanded = false
        )
        
        assertEquals("Test Article", article.headline)
        assertEquals("Aeon", article.source)
        assertFalse(article.isSummaryExpanded)
    }
    
    @Test
    fun testTodaysPickVideoCreation() {
        val video = TodaysPickVideo(
            id = "vid123",
            title = "Test Video",
            youtubeLink = "https://youtube.com/watch?v=vid123",
            channel = "TED Talks",
            publishedAt = System.currentTimeMillis(),
            duration = "15:30"
        )
        
        assertEquals("Test Video", video.title)
        assertEquals("TED Talks", video.channel)
        assertTrue(video.youtubeLink.contains("vid123"))
    }
    
    @Test
    fun testTodaysPicksWrapper() {
        val article = TodaysPickArticle(
            id = "1",
            headline = "Test",
            summary = "Summary",
            link = "https://example.com",
            source = "Aeon",
            publishedAt = System.currentTimeMillis()
        )
        
        val video = TodaysPickVideo(
            id = "vid123",
            title = "Video",
            youtubeLink = "https://youtube.com/watch?v=vid123",
            channel = "TED",
            publishedAt = System.currentTimeMillis()
        )
        
        val picks = TodaysPicks(
            articles = listOf(article),
            videos = listOf(video),
            lastUpdated = System.currentTimeMillis()
        )
        
        assertEquals(1, picks.articles.size)
        assertEquals(1, picks.videos.size)
        assertEquals("TED", picks.videos.first().channel)
    }
}
