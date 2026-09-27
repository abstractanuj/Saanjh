package com.geecee.escapelauncher.core.common

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import timber.log.Timber

class AppLoggerTest {

    private val loggedEntries = mutableListOf<LogEntry>()

    data class LogEntry(
        val priority: Int,
        val tag: String?,
        val message: String,
        val throwable: Throwable?
    )

    @Before
    fun setUp() {
        Timber.uprootAll()
        loggedEntries.clear()
        Timber.plant(object : Timber.Tree() {
            override fun log(priority: Int, tag: String?, message: String, t: Throwable?) {
                loggedEntries.add(LogEntry(priority, tag, message, t))
            }
        })
    }

    @Test
    fun testVerboseLogging() {
        AppLogger.v("Verbose test", tag = "TestTag")
        assertEquals(1, loggedEntries.size)
        val entry = loggedEntries[0]
        assertEquals(2, entry.priority)
        assertEquals("TestTag", entry.tag)
        assertEquals("Verbose test", entry.message)
    }

    @Test
    fun testDebugLogging() {
        AppLogger.d("Debug test", tag = "TestTag")
        assertEquals(1, loggedEntries.size)
        val entry = loggedEntries[0]
        assertEquals(3, entry.priority)
        assertEquals("TestTag", entry.tag)
        assertEquals("Debug test", entry.message)
    }

    @Test
    fun testInfoLogging() {
        AppLogger.i("Info test", tag = "TestTag")
        assertEquals(1, loggedEntries.size)
        val entry = loggedEntries[0]
        assertEquals(4, entry.priority)
        assertEquals("TestTag", entry.tag)
        assertEquals("Info test", entry.message)
    }

    @Test
    fun testWarningLogging() {
        val exception = RuntimeException("Warn exception")
        AppLogger.w("Warning test", t = exception, tag = "TestTag")
        assertEquals(1, loggedEntries.size)
        val entry = loggedEntries[0]
        assertEquals(5, entry.priority)
        assertEquals("TestTag", entry.tag)
        assertNotNull(entry.throwable)
        assertEquals("Warn exception", entry.throwable?.message)
    }

    @Test
    fun testErrorLogging() {
        val exception = RuntimeException("Error exception")
        AppLogger.e("Error test", t = exception, tag = "TestTag")
        assertEquals(1, loggedEntries.size)
        val entry = loggedEntries[0]
        assertEquals(6, entry.priority)
        assertEquals("TestTag", entry.tag)
        assertNotNull(entry.throwable)
        assertEquals("Error exception", entry.throwable?.message)
    }
}
