package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.MusicDao
import com.geecee.escapelauncher.core.data.entity.ContentMetadataEntity
import com.geecee.escapelauncher.core.data.entity.MusicRecommendationEntity
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.inject.Inject
import timber.log.Timber
import javax.inject.Singleton

@Singleton
class GeminiContentGenerator @Inject constructor(
    private val httpClient: OkHttpClient,
    private val musicDao: MusicDao
) {
    private val apiKey = "AIzaSyCPb3am7EyyuuzpDLbwn1kUWGRgrxO9a-k"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
    private val gson = Gson()

    suspend fun generateMusicIfNeeded() {
        val metadata = musicDao.getMetadata()
        val twoDaysInMillis = 2 * 24 * 60 * 60 * 1000L
        if (metadata == null || System.currentTimeMillis() - metadata.lastGeneratedAt > twoDaysInMillis) {

            val recommendations = fetchMusicFromGemini()
            if (recommendations.isNotEmpty()) {
                musicDao.insertRecommendations(recommendations.map { MusicRecommendationEntity(text = it) })
                musicDao.updateMetadata(ContentMetadataEntity("music", System.currentTimeMillis()))

            } else if (metadata == null) {
                Timber.w("Gemini fetch failed, priming with local music.")
                musicDao.insertRecommendations(LocalContent.classicalMusicRecommendations.shuffled().take(20).map { MusicRecommendationEntity(text = it) })
                musicDao.updateMetadata(ContentMetadataEntity("music", System.currentTimeMillis()))
            }
        }
    }

    private suspend fun fetchMusicFromGemini(): List<String> {
        val prompt = """
            Generate 5 deep, multidisciplinary insights on cognitive development, covering history, economics, language, and philosophy (German, Japanese, Hinduism, Stoicism, Aristotle, Darwin). Focus on ideas of great leaders and visionaries. Elaborate on the cause and effect of a major past event, the reasoning behind the decision, and its impact then and now (include specific numbers/metrics where applicable). Connect this to geopolitics, trade, business, stocks, wars, or economies. Keep it profound but accessible, optimized for spaced repetition (ideas worth revisiting every two months). Format each insight as a short, impactful paragraph.
            Return ONLY a valid JSON array of strings. No markdown, no extra text.
        """.trimIndent()
        return fetchListFromGemini(prompt)
    }

    private suspend fun fetchListFromGemini(prompt: String): List<String> {
        val requestBody = mapOf(
            "contents" to listOf(
                mapOf("parts" to listOf(mapOf("text" to prompt)))
            )
        )
        val jsonBody = gson.toJson(requestBody)
        val request = Request.Builder()
            .url(apiUrl)
            .post(jsonBody.toRequestBody("application/json; charset=utf-8".toMediaType()))
            .build()

        return try {
            val response = httpClient.newCall(request).execute()
            val responseString = response.body?.string() ?: return emptyList()
            val geminiResponse = gson.fromJson(responseString, GeminiResponse::class.java)
            val jsonText = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            // Robust JSON extraction
            val startIndex = jsonText.indexOf("[")
            val endIndex = jsonText.lastIndexOf("]")
            if (startIndex == -1 || endIndex == -1 || endIndex < startIndex) return emptyList()
            
            val cleanedJson = jsonText.substring(startIndex, endIndex + 1)
            val listType = object : com.google.gson.reflect.TypeToken<List<String>>() {}.type
            gson.fromJson(cleanedJson, listType)
        } catch (e: Exception) {
            emptyList()
        }
    }

    private data class GeminiResponse(val candidates: List<Candidate>)
    private data class Candidate(val content: Content)
    private data class Content(val parts: List<Part>)
    private data class Part(val text: String)
}
