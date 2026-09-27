package com.geecee.escapelauncher.core.data.network

import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class SimpleRssParserTest {

    private lateinit var parser: SimpleRssParser

    @Before
    fun setUp() {
        parser = SimpleRssParser(mockk())
    }

    @Test
    fun testParseAeonSampleFeed() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Aeon Essays</title>
                <item>
                  <title>How to become the person you want to be</title>
                  <link>https://aeon.co/guides/how-to-become-the-person-you-want-to-be</link>
                  <description>Close the intention-action gap and live the way you want</description>
                  <pubDate>Mon, 10 Aug 2026 10:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
        """.trimIndent()
        
        val result = parser.parseArticleFeed(sampleXml, "Aeon")
        
        assertTrue(result.isSuccess)
        val articles = result.getOrNull()!!
        assertEquals(1, articles.size)
        assertEquals("How to become the person you want to be", articles[0].headline)
        assertEquals("Aeon", articles[0].source)
    }
    
    @Test
    fun testParseMultipleItems() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Psyche</title>
                <item>
                  <title>Article 1</title>
                  <link>https://psyche.co/1</link>
                  <description>Summary 1</description>
                  <pubDate>Mon, 10 Aug 2026 10:00:00 GMT</pubDate>
                </item>
                <item>
                  <title>Article 2</title>
                  <link>https://psyche.co/2</link>
                  <description>Summary 2</description>
                  <pubDate>Tue, 11 Aug 2026 10:00:00 GMT</pubDate>
                </item>
              </channel>
            </rss>
        """.trimIndent()
        
        val result = parser.parseArticleFeed(sampleXml, "Psyche")
        
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }
    
    @Test
    fun testParseEmptyFeed() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <rss version="2.0">
              <channel>
                <title>Empty Feed</title>
              </channel>
            </rss>
        """.trimIndent()
        
        val result = parser.parseArticleFeed(sampleXml, "Empty")
        
        assertTrue(result.isSuccess)
        assertEquals(0, result.getOrNull()!!.size)
    }
}
