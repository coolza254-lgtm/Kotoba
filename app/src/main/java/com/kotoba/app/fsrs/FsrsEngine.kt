package com.kotoba.app.fsrs

import com.kotoba.app.data.model.WordEntity
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pure Kotlin Implementation of Free Spaced Repetition Scheduler (FSRS v5)
 * with short-term same-day learning/relearning steps (10m / 1h) so forgotten or difficult words
 * come back for review later the same day without pulling additional new words.
 */
class FsrsEngine(
    var desiredRetention: Double = 0.90,
    var weights: DoubleArray = DEFAULT_WEIGHTS.clone(),
    var maxInterval: Int = 365
) {
    companion object {
        val DEFAULT_WEIGHTS = doubleArrayOf(
            0.40255, 1.18385, 3.173, 15.69105,
            7.1949, 0.5345, 1.4604, 0.0046,
            1.54575, 0.1192, 1.01925, 1.9395,
            0.11, 0.29605, 2.2698, 0.2315,
            2.9898, 0.51655, 0.6621
        )
        const val DECAY = -0.5
        const val FACTOR = 19.0 / 81.0
        const val MINUTE_MS = 60_000L
        const val HOUR_MS = 3_600_000L
        const val DAY_MS = 86_400_000L
    }

    enum class Rating(val value: Int) {
        AGAIN(1),
        HARD(2),
        GOOD(3),
        EASY(4)
    }

    fun retrievability(elapsedDays: Double, stability: Double): Double {
        if (stability <= 0.0) return 0.0
        return (1.0 + FACTOR * elapsedDays / stability).pow(DECAY)
    }

    fun nextInterval(stability: Double): Int {
        if (stability <= 0.0) return 1
        val ivl = (stability / FACTOR * (desiredRetention.pow(1.0 / DECAY) - 1.0)).roundToInt()
        return min(maxInterval, max(1, ivl))
    }

    private fun initDifficulty(rating: Rating): Double {
        val g = rating.value
        val d = weights[4] - exp(weights[5] * (g - 1)) + 1
        return min(10.0, max(1.0, d))
    }

    private fun updateDifficulty(currentD: Double, rating: Rating): Double {
        val dNext = currentD - weights[6] * (rating.value - 3) * (10.0 - currentD) / 9.0
        val meanReversionD = weights[7] * initDifficulty(Rating.EASY) + (1.0 - weights[7]) * dNext
        return min(10.0, max(1.0, meanReversionD))
    }

    private fun nextStabilitySuccess(d: Double, s: Double, r: Double, rating: Rating): Double {
        val w = weights
        val hardPenalty = if (rating == Rating.HARD) w[15] else 1.0
        val easyBonus = if (rating == Rating.EASY) w[16] else 1.0
        return s * (1.0 + exp(w[8]) * (11.0 - d) * s.pow(-w[9]) * (exp(w[10] * (1.0 - r)) - 1.0) * hardPenalty * easyBonus)
    }

    private fun nextStabilityFailure(d: Double, s: Double, r: Double): Double {
        val w = weights
        val res = w[11] * d.pow(-w[12]) * ((s + 1.0).pow(w[13]) - 1.0) * exp(w[14] * (1.0 - r))
        return min(s, max(0.2, res))
    }

    fun previewIntervals(word: WordEntity): Map<Int, String> {
        val now = System.currentTimeMillis()
        return (1..4).associateWith { ratingVal ->
            val simulated = review(word.copy(), ratingVal, now)
            formatDelta(simulated.nextReview - now)
        }
    }

    private fun formatDelta(deltaMs: Long): String {
        val minutes = (deltaMs / MINUTE_MS).coerceAtLeast(1)
        if (minutes < 60) return "${minutes} นาที"
        val hours = minutes / 60
        if (deltaMs < DAY_MS) return "${hours} ชม."
        val days = (deltaMs.toDouble() / DAY_MS).roundToInt().coerceAtLeast(1)
        return "${days} วัน"
    }

    fun review(word: WordEntity, ratingValue: Int, now: Long = System.currentTimeMillis()): WordEntity {
        val rating = when (ratingValue) {
            1 -> Rating.AGAIN
            2 -> Rating.HARD
            3 -> Rating.GOOD
            else -> Rating.EASY
        }
        return review(word, rating, now)
    }

    fun review(word: WordEntity, rating: Rating, now: Long = System.currentTimeMillis()): WordEntity {
        val actualElapsedDays = if (word.lastReview > 0L) {
            max(0.0, (now - word.lastReview).toDouble() / DAY_MS)
        } else {
            0.0
        }
        if (word.firstStudiedAt == 0L) {
            word.firstStudiedAt = now
        }

        when (word.state) {
            "new" -> {
                val initialDifficulty = initDifficulty(rating)
                word.difficulty = initialDifficulty
                word.reps = 1
                word.elapsedDays = 0

                when (rating) {
                    Rating.AGAIN -> {
                        word.stability = weights[0]
                        word.state = "learning"
                        word.lapses = 0
                        word.scheduledDays = 0
                        word.nextReview = now + 10 * MINUTE_MS
                    }
                    Rating.HARD -> {
                        word.stability = weights[1]
                        word.state = "learning"
                        word.lapses = 0
                        word.scheduledDays = 0
                        word.nextReview = now + 60 * MINUTE_MS
                    }
                    Rating.GOOD -> {
                        word.stability = max(1.2, weights[1])
                        word.state = "review"
                        word.lapses = 0
                        word.scheduledDays = 1
                        word.nextReview = now + 1 * DAY_MS
                    }
                    Rating.EASY -> {
                        word.stability = max(4.0, weights[2] * 1.35)
                        word.state = "review"
                        word.lapses = 0
                        val days = max(4, min(7, nextInterval(word.stability)))
                        word.scheduledDays = days
                        word.nextReview = now + days * DAY_MS
                    }
                }
                word.lastReview = now
            }

            "learning", "relearning" -> {
                val currentS = max(0.4, word.stability)
                val currentD = if (word.difficulty > 0.0) word.difficulty else 5.0
                word.difficulty = updateDifficulty(currentD, rating)
                word.reps++
                word.elapsedDays = actualElapsedDays.roundToInt()

                when (rating) {
                    Rating.AGAIN -> {
                        word.stability = max(0.3, currentS * 0.7)
                        word.scheduledDays = 0
                        word.nextReview = now + 10 * MINUTE_MS
                    }
                    Rating.HARD -> {
                        word.stability = max(0.6, currentS)
                        word.scheduledDays = 0
                        word.nextReview = now + 60 * MINUTE_MS
                    }
                    Rating.GOOD -> {
                        val gradS = if (word.state == "relearning") max(1.2, currentS) else max(1.5, weights[1] * 1.2)
                        word.stability = gradS
                        word.state = "review"
                        val days = max(1, nextInterval(gradS))
                        word.scheduledDays = days
                        word.nextReview = now + days * DAY_MS
                    }
                    Rating.EASY -> {
                        val easyS = max(3.5, currentS * 2.2)
                        word.stability = easyS
                        word.state = "review"
                        val days = max(3, nextInterval(easyS))
                        word.scheduledDays = days
                        word.nextReview = now + days * DAY_MS
                    }
                }
                word.lastReview = now
            }

            else -> {
                // Graduated "review" card
                val currentS = max(0.5, word.stability)
                val currentD = if (word.difficulty > 0.0) word.difficulty else 5.0
                val r = retrievability(actualElapsedDays, currentS)
                word.difficulty = updateDifficulty(currentD, rating)
                word.reps++
                word.elapsedDays = actualElapsedDays.roundToInt()

                if (rating == Rating.AGAIN) {
                    word.lapses++
                    word.stability = nextStabilityFailure(currentD, currentS, r)
                    word.state = "relearning"
                    word.scheduledDays = 0
                    word.nextReview = now + 10 * MINUTE_MS
                } else {
                    val sHard = nextStabilitySuccess(currentD, currentS, r, Rating.HARD)
                    val sGood = nextStabilitySuccess(currentD, currentS, r, Rating.GOOD)
                    val sEasy = nextStabilitySuccess(currentD, currentS, r, Rating.EASY)

                    val hardDays = max(1, nextInterval(sHard))
                    val goodDays = max(hardDays + if (actualElapsedDays >= 0.5) 1 else 0, nextInterval(sGood))
                    val easyDays = max(goodDays + 1, nextInterval(sEasy))

                    when (rating) {
                        Rating.HARD -> {
                            word.stability = sHard
                            word.scheduledDays = hardDays
                            word.nextReview = now + hardDays * DAY_MS
                        }
                        Rating.GOOD -> {
                            word.stability = sGood
                            word.scheduledDays = goodDays
                            word.nextReview = now + goodDays * DAY_MS
                        }
                        Rating.EASY -> {
                            word.stability = sEasy
                            word.scheduledDays = easyDays
                            word.nextReview = now + easyDays * DAY_MS
                        }
                        else -> {}
                    }
                    word.state = "review"
                }
                word.lastReview = now
            }
        }
        return word
    }
}
