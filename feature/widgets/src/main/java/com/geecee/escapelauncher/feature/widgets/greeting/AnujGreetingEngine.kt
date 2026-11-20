package com.geecee.escapelauncher.feature.widgets.greeting

import android.content.Context
import java.util.Calendar
import kotlin.random.Random

data class AnujGreeting(
    val phaseLabel: String,
    val message: String
)

object AnujGreetingEngine {

    private const val PREFS_NAME = "saanjh_anuj_greeting_prefs"

    fun nextGreeting(context: Context, widgetKey: String, forceNewRandom: Boolean = false): AnujGreeting {
        val cal = Calendar.getInstance()
        val hour = cal.get(Calendar.HOUR_OF_DAY)
        val minute = cal.get(Calendar.MINUTE)
        val dayOfYear = cal.get(Calendar.DAY_OF_YEAR)

        val (phaseLabel, pool) = when {
            hour in 5..7 -> "brand new morning" to BRAND_NEW_MORNING_GREETINGS
            hour in 8..10 || (hour == 11 && minute < 30) -> "pleasant morning" to PLEASANT_MORNING_GREETINGS
            (hour == 11 && minute >= 30) || hour in 12..14 -> "wonderful day" to WONDERFUL_DAY_GREETINGS
            hour in 15..16 -> "pleasant afternoon" to PLEASANT_AFTERNOON_GREETINGS
            hour in 17..18 -> "saanjh · dusk" to DUSK_GREETINGS
            hour in 19..20 -> "soft twilight" to TWILIGHT_GREETINGS
            else -> "quiet night" to NIGHT_GREETINGS
        }

        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastIdxKey = "last_idx_$widgetKey"
        val lastSlotKey = "last_slot_$widgetKey"

        // Automatically rotate every 20-minute slot or whenever forceNewRandom is requested
        val currentSlot = dayOfYear * 100 + hour * 3 + (minute / 20)
        val savedSlot = prefs.getInt(lastSlotKey, -1)
        val savedIdx = prefs.getInt(lastIdxKey, -1)

        val chosenIdx = if (!forceNewRandom && savedSlot == currentSlot && savedIdx in pool.indices) {
            savedIdx
        } else {
            val candidates = pool.indices.filter { it != savedIdx }
            val next = if (candidates.isNotEmpty()) {
                candidates[Random(System.nanoTime()).nextInt(candidates.size)]
            } else {
                0
            }
            prefs.edit()
                .putInt(lastIdxKey, next)
                .putInt(lastSlotKey, currentSlot)
                .apply()
            next
        }

        return AnujGreeting(
            phaseLabel = phaseLabel,
            message = pool[chosenIdx]
        )
    }

    private val BRAND_NEW_MORNING_GREETINGS = listOf(
        "A brand new day is unfolding, Anuj.",
        "Anuj, the morning is quiet and brand new.",
        "First light on a brand new morning, Anuj.",
        "Good morning, Anuj. A fresh, unwritten day awaits.",
        "Breathe in this brand new day, Anuj.",
        "Anuj, step gently into a brand new morning."
    )

    private val PLEASANT_MORNING_GREETINGS = listOf(
        "Anuj, isn't it a beautiful morning outside?",
        "It's a pleasant, unhurried morning, Anuj.",
        "What a wonderful morning to begin at your own pace, Anuj.",
        "Anuj, notice how crisp and pleasant the morning feels.",
        "A bright, pleasant morning to you, Anuj.",
        "Isn't the morning light calm and inviting, Anuj?"
    )

    private val WONDERFUL_DAY_GREETINGS = listOf(
        "It's a wonderful day, Anuj.",
        "Anuj, pause for a second—it's a wonderful day.",
        "Right in the heart of a wonderful day, Anuj.",
        "A pleasant, sunlit hour of the day, Anuj.",
        "Move lightly through this wonderful day, Anuj.",
        "Anuj, isn't it a pleasant day to simply be?"
    )

    private val PLEASANT_AFTERNOON_GREETINGS = listOf(
        "A warm, pleasant afternoon, Anuj.",
        "Anuj, the afternoon light is turning soft and golden.",
        "Isn't the afternoon air pleasant right now, Anuj?",
        "Still a wonderful day unfolding around you, Anuj.",
        "Anuj, take one unhurried breath in this golden hour.",
        "A quiet, pleasant afternoon to carry you toward evening, Anuj."
    )

    private val DUSK_GREETINGS = listOf(
        "Dusk is settling gently over the day, Anuj.",
        "Anuj, look at the sky—it's a quiet, golden dusk.",
        "Saanjh has arrived, Anuj. Let the day's pace soften.",
        "Isn't the dusk light peaceful outside, Anuj?",
        "A warm dusk closing a wonderful day, Anuj.",
        "Anuj, let the stillness of dusk meet you where you are."
    )

    private val TWILIGHT_GREETINGS = listOf(
        "Soft twilight is wrapping the evening, Anuj.",
        "Anuj, step into the calm hush of twilight.",
        "The pleasant stillness of twilight is here, Anuj.",
        "Under the twilight sky, everything can slow down, Anuj.",
        "A quiet, velvet twilight to unwind in, Anuj.",
        "Anuj, isn't the twilight air cool and restful?"
    )

    private val NIGHT_GREETINGS = listOf(
        "The night is calm and unhurried, Anuj.",
        "Anuj, rest easy—another brand new day will come.",
        "Quiet stars and a peaceful night to you, Anuj.",
        "Let the stillness of the late hour hold you, Anuj.",
        "A gentle, quiet close to the day, Anuj.",
        "All is quiet tonight, Anuj. Let your shoulders drop."
    )
}
