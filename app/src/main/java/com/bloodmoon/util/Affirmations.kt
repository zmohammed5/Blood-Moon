package com.bloodmoon.util

import java.time.LocalDate

/**
 * Daily affirmations and supportive messages.
 *
 * Tone: supportive, grounded, slightly humorous, goth-friendly, metal-flavored.
 * NOT personal, NOT cringe, NOT overly romantic.
 */
object Affirmations {

    private val messages = listOf(
        "The world's a pit. Good thing you mosh through it.",
        "You've survived every day so far. That's metal.",
        "Rest. Even the moon fades.",
        "Strong day or soft day — both count.",
        "Chaos comes and goes. You remain.",
        "Low energy isn't failure. It's recovery.",
        "Breathe. The void breathes back.",
        "Not every battle needs fighting today.",
        "You contain multitudes. Some darker than others.",
        "Storms pass. You're still here.",
        "Survival is its own rebellion.",
        "The night is long, but so are you.",
        "Softness is not weakness. It's strategy.",
        "Your body is a temple. Sometimes temples crumble. That's okay.",
        "Progress isn't linear. Neither are you.",
        "The moon doesn't apologize for its phases.",
        "You're allowed to take up space.",
        "Some days you conquer. Some days you endure.",
        "The darkness knows your name. You know its secrets.",
        "Forward motion, even if it's crawling.",
        "Quiet days build loud tomorrows.",
        "You've weathered worse. This will pass too.",
        "The abyss stares back. You stare harder.",
        "Pain is temporary. Your resilience isn't.",
        "Not broken. Just transforming.",
        "You're doing better than you think.",
        "Sometimes existing is enough.",
        "The universe is vast. You're part of it.",
        "Shadows don't scare you. You are the shadow.",
        "Tomorrow is unwritten. Make it yours."
    )

    /**
     * Get daily affirmation based on the date (deterministic).
     */
    fun getDailyAffirmation(date: LocalDate = LocalDate.now()): String {
        val dayOfYear = date.dayOfYear
        val index = dayOfYear % messages.size
        return messages[index]
    }

    /**
     * Get a random affirmation.
     */
    fun getRandomAffirmation(): String {
        return messages.random()
    }
}
