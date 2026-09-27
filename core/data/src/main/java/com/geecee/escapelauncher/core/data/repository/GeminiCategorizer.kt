package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.model.InstalledApp
import com.google.gson.Gson
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class GeminiCategorizer @Inject constructor(
    private val httpClient: OkHttpClient
) {
    private val apiKey = "AIzaSyCPb3am7EyyuuzpDLbwn1kUWGRgrxO9a-k"
    private val apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=$apiKey"
    private val gson = Gson()

    suspend fun categorizeApps(apps: List<InstalledApp>): Map<String, String> = withContext(Dispatchers.IO) {
        if (apps.isEmpty()) return@withContext emptyMap()

        val appListString = apps.joinToString("\n") { "${it.packageName} - ${it.displayName}" }
        val prompt = """
            You are an expert at categorizing Android apps. Categorize each app below into exactly one of the following categories:
            
            - Create: Tools for making things (editing, drawing, coding, office, cameras).
            - Connect: Communication and social media (messaging, email, social networks).
            - Consume: Entertainment and media consumption (streaming, readers, browsers, news, video players).
            - Manage: Organization and productivity (calendars, notes, reminders, files, settings, banking).
            - Travel: Navigation and travel (maps, ride-sharing, booking).
            - Utility: General purpose tools (calculators, flashlights, clocks, weather, system tools).
            
            Return ONLY a valid JSON array of objects. Example:
            [{"package":"com.whatsapp","category":"Connect"}, {"package":"com.adobe.reader","category":"Consume"}]
            
            Do not include any text before or after the JSON. No markdown.
            
            Apps:
            $appListString
        """.trimIndent()

        val requestBody = mapOf(
            "contents" to listOf(
                mapOf(
                    "parts" to listOf(
                        mapOf("text" to prompt)
                    )
                )
            )
        )

        val jsonBody = gson.toJson(requestBody)
        val mediaType = "application/json; charset=utf-8".toMediaType()
        val request = Request.Builder()
            .url(apiUrl)
            .post(jsonBody.toRequestBody(mediaType))
            .build()

        try {
            val response = httpClient.newCall(request).execute()
            if (!response.isSuccessful) {
                return@withContext fallbackCategorization(apps)
            }

            val responseString = response.body?.string() ?: return@withContext fallbackCategorization(apps)
            val geminiResponse = gson.fromJson(responseString, GeminiResponse::class.java)
            val jsonText = geminiResponse.candidates.firstOrNull()?.content?.parts?.firstOrNull()?.text ?: ""
            
            // Extract JSON from potential markdown code blocks
            val cleanedJson = jsonText.trim().removeSurrounding("```json", "```").trim()
            val listType = object : com.google.gson.reflect.TypeToken<List<AppCategoryResult>>() {}.type
            val results: List<AppCategoryResult> = gson.fromJson(cleanedJson, listType)
            
            results.associate { it.`package` to it.category }
        } catch (e: Exception) {
            fallbackCategorization(apps)
        }
    }

    private fun fallbackCategorization(apps: List<InstalledApp>): Map<String, String> {
        return apps.associate { it.packageName to "Utility" }
    }

    private data class GeminiResponse(val candidates: List<Candidate>)
    private data class Candidate(val content: Content)
    private data class Content(val parts: List<Part>)
    private data class Part(val text: String)
    private data class AppCategoryResult(val `package`: String, val category: String)
}
