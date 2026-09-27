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
class YoutubeRssParserTest {
    
    private lateinit var parser: SimpleRssParser

    @Before
    fun setUp() {
        parser = SimpleRssParser(mockk())
    }
    
    @Test
    fun testParseYoutubeFeed() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:yt="http://www.youtube.com/xml/schemas/2015">
              <title>TED Talks</title>
              <entry>
                <yt:videoId>abc123def456</yt:videoId>
                <title>How to manage anxiety</title>
                <link href="https://www.youtube.com/watch?v=abc123def456" rel="alternate"/>
                <published>2026-08-10T10:00:00+00:00</published>
              </entry>
            </feed>
        """.trimIndent()
        
        val result = parser.parseYoutubeFeed(sampleXml, "TED Talks")
        
        assertTrue(result.isSuccess)
        val videos = result.getOrNull()!!
        assertEquals(1, videos.size)
        assertEquals("How to manage anxiety", videos[0].title)
        assertEquals("TED Talks", videos[0].channel)
        assertTrue(videos[0].youtubeLink.contains("abc123def456"))
    }
    
    @Test
    fun testParseMultipleVideos() {
        val sampleXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <feed xmlns="http://www.w3.org/2005/Atom" xmlns:yt="http://www.youtube.com/xml/schemas/2015">
              <title>Channel</title>
              <entry>
                <yt:videoId>vid1</yt:videoId>
                <title>Video 1</title>
                <link href="https://www.youtube.com/watch?v=vid1" rel="alternate"/>
                <published>2026-08-10T10:00:00+00:00</published>
              </entry>
              <entry>
                <yt:videoId>vid2</yt:videoId>
                <title>Video 2</title>
                <link href="https://www.youtube.com/watch?v=vid2" rel="alternate"/>
                <published>2026-08-11T10:00:00+00:00</published>
              </entry>
            </feed>
        """.trimIndent()
        
        val result = parser.parseYoutubeFeed(sampleXml, "Channel")
        
        assertTrue(result.isSuccess)
        assertEquals(2, result.getOrNull()!!.size)
    }
}
