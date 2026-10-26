package com.kotoba.app.fsrs

import com.kotoba.app.data.model.WordEntity
import kotlin.math.exp
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.roundToInt

/**
 * Pure Kotlin Implementation of Free Spaced Repetition Scheduler (FSRS v5)
 * Reference: https://github.com/open-spaced-repetition/fsrs4anki/wiki/The-Algorithm
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

    private fun nextStabilitySuccess(d: Double, s: Double, r: Double, rating: Rating): Double {
        val w = weights
        val hardPenalty = if (rating == Rating.HARD) w[15] else 1.0
        val easyBonus = if (rating == Rating.EASY) w[16] else 1.0
        return s * (1.0 + exp(w[8]) * (11.0 - d) * s.pow(-w[9]) * (exp(w[10] * (1.0 - r)) - 1.0) * hardPenalty * easyBonus)
    }

    private fun nextStabilityFailure(d: Double, s: Double, r: Double): Double {
        val w = weights
        val res = w[11] * d.pow(-w[12]) * ((s + 1.0).pow(w[13]) - 1.0) * exp(w[14] * (1.0 - r))
        return min(s, max(0.1, res))
    }

    fun previewIntervals(word: WordEntity): Map<Int, String> {
        val s = max(0.1, word.stability)
        return mapOf(
            1 to "10m",
            2 to "${max(1, (nextInterval(s) * 0.5).roundToInt())}d",
            3 to "${nextInterval(s)}d",
            4 to "${max(4, (nextInterval(s) * 1.5).roundToInt())}d"
        )
    }

    fun review(word: WordEntity, ratingValue: Int): WordEntity {
        val rating = when (ratingValue) {
            1 -> Rating.AGAIN
            2 -> Rating.HARD
            3 -> Rating.GOOD
            else -> Rating.EASY
        }
        return review(word, rating)
    }

    fun review(word: WordEntity, rating: Rating): WordEntity {
        val now = System.currentTimeMillis()
        if (word.state == "new") {
            val initialStability = weights[rating.value - 1]
            val initialDifficulty = initDifficulty(rating)
            word.stability = initialStability
            word.difficulty = initialDifficulty
            word.state = "review"
            word.reps = 1
            word.lapses = 0
            word.elapsedDays = 0
            word.scheduledDays = nextInterval(initialStability)
            word.lastReview = now
            word.nextReview = now + word.scheduledDays * 86400000L
        } else {
            val r = retrievability(word.elapsedDays.toDouble(), word.stability)
            val newStability = if (rating == Rating.AGAIN) {
                word.lapses++
                nextStabilityFailure(word.difficulty, word.stability, r)
            } else {
                nextStabilitySuccess(word.difficulty, word.stability, r, rating)
            }

            val dNext = word.difficulty - weights[6] * (rating.value - 3) * (10.0 - word.difficulty) / 9.0
            val meanReversionD = weights[7] * initDifficulty(Rating.EASY) + (1.0 - weights[7]) * dNext
            val newDifficulty = min(10.0, max(1.0, meanReversionD))

            word.stability = newStability
            word.difficulty = newDifficulty
            word.reps++
            word.elapsedDays = 0
            word.scheduledDays = if (rating == Rating.AGAIN) 1 else nextInterval(newStability)
            word.lastReview = now
            word.nextReview = now + word.scheduledDays * 86400000L
        }
        return word
    }
}
