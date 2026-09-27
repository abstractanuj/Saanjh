package com.geecee.escapelauncher.core.data.repository

import com.geecee.escapelauncher.core.data.database.CuriosityDao
import com.geecee.escapelauncher.core.data.database.MusicDao
import com.geecee.escapelauncher.core.data.entity.CuriosityQuestionEntity
import com.geecee.escapelauncher.core.data.entity.MusicRecommendationEntity
import com.geecee.escapelauncher.core.model.CuriosityContent
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Calendar
import java.util.Random
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CuriosityRepository @Inject constructor(
    private val curiosityDao: CuriosityDao,
    private val musicDao: MusicDao
) {
    suspend fun getCurrentContent(): CuriosityContent {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val now = System.currentTimeMillis()

        return when (hour) {
            in 10..15 -> { // 10 AM to 4 PM
                val lastUsed = curiosityDao.getLastUsedQuestion().first()
                val activeQuestionText = if (lastUsed != null && isSameHour(lastUsed.usedAt ?: 0, now)) {
                    lastUsed.text
                } else {
                    val next = curiosityDao.getNextUnusedQuestion().first()
                    if (next != null) {
                        curiosityDao.markAsUsed(next.id, now)
                        next.text
                    } else {
                        val seed = (calendar.get(Calendar.DAY_OF_YEAR) * 24 + hour).toLong()
                        val fallbackRandom = Random(seed)
                        val questions = LocalContent.curiosityQuestions
                        questions[fallbackRandom.nextInt(questions.size)]
                    }
                }
                CuriosityContent.Question(activeQuestionText)
            }
            in 16..17 -> { // 4 PM to 6 PM
                val seed = (calendar.get(Calendar.DAY_OF_YEAR) * 24 + hour).toLong()
                val fallbackRandom = Random(seed)
                val movieName = LocalContent.movies[fallbackRandom.nextInt(LocalContent.movies.size)]
                val prefixes = listOf(
                    "You should watch",
                    "How about a movie tonight?",
                    "A great pick for the evening:",
                    "Tonight's movie suggestion:",
                    "Have you seen",
                    "A cinematic masterpiece:"
                )
                val prefix = prefixes[fallbackRandom.nextInt(prefixes.size)]
                CuriosityContent.Movie("$prefix $movieName", movieName)
            }
            else -> { // 6 PM to 10 AM (Longer Song bracket)
                val lastUsed = musicDao.getLastUsedRecommendation().first()
                val shouldRefresh = if (hour in 18..23 || hour == 0) {
                    lastUsed == null || !isSameHour(lastUsed.usedAt ?: 0, now)
                } else {
                    lastUsed == null || now - (lastUsed.usedAt ?: 0) > (2 * 24 * 60 * 60 * 1000L)
                }

                val activeMusicText = if (!shouldRefresh && lastUsed != null) {
                    lastUsed.text
                } else {
                    val next = musicDao.getNextUnusedRecommendation().first()
                    if (next != null) {
                        musicDao.markAsUsed(next.id, now)
                        next.text
                    } else {
                        val seed = (calendar.get(Calendar.DAY_OF_YEAR) * 24 + hour).toLong()
                        val fallbackRandom = Random(seed)
                        val recommendations = LocalContent.classicalMusicRecommendations
                        recommendations[fallbackRandom.nextInt(recommendations.size)]
                    }
                }
                
                val prefixes = listOf(
                    "I thought you might enjoy",
                    "How about some",
                    "Maybe a bit of",
                    "Relax with",
                    "It's a good time for",
                    "You might like listening to",
                    "A peaceful recommendation:",
                    "I've picked this for you:"
                )
                val seed = (calendar.get(Calendar.DAY_OF_YEAR) * 24 + hour).toLong()
                val fallbackRandom = Random(seed)
                val prefix = prefixes[fallbackRandom.nextInt(prefixes.size)]
                CuriosityContent.Music("$prefix $activeMusicText", activeMusicText)
            }
        }
    }

    private fun isSameHour(t1: Long, t2: Long): Boolean {
        val c1 = Calendar.getInstance().apply { timeInMillis = t1 }
        val c2 = Calendar.getInstance().apply { timeInMillis = t2 }
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
               c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR) &&
               c1.get(Calendar.HOUR_OF_DAY) == c2.get(Calendar.HOUR_OF_DAY)
    }
}
