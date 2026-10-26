package com.kotoba.app.data

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

enum class ThemeMode(val key: String, val labelTh: String) {
    SYSTEM("system", "ตามระบบ"),
    LIGHT("light", "โหมดสว่าง"),
    DARK("dark", "โหมดมืด AMOLED");

    companion object {
        fun fromKey(key: String): ThemeMode = entries.find { it.key == key } ?: SYSTEM
    }
}

data class SavedSessionState(
    val wordIds: List<Int> = emptyList(),
    val currentIndex: Int = 0,
    val dayKey: String = "",
    val isExtraReview: Boolean = false
) {
    val isActive: Boolean
        get() = wordIds.isNotEmpty() && currentIndex in 0 until wordIds.size
    val remainingCount: Int
        get() = (wordIds.size - currentIndex).coerceAtLeast(0)
}

class AppPreferences(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences("kotoba_prefs", Context.MODE_PRIVATE)

    private val _themeMode = MutableStateFlow(
        ThemeMode.fromKey(prefs.getString(KEY_THEME_MODE, ThemeMode.SYSTEM.key) ?: ThemeMode.SYSTEM.key)
    )
    val themeMode: StateFlow<ThemeMode> = _themeMode.asStateFlow()

    private val _doneToday = MutableStateFlow(0)
    val doneToday: StateFlow<Int> = _doneToday.asStateFlow()

    private val _newDoneToday = MutableStateFlow(0)
    val newDoneToday: StateFlow<Int> = _newDoneToday.asStateFlow()

    private val _streakDays = MutableStateFlow(prefs.getInt(KEY_STREAK_DAYS, 1))
    val streakDays: StateFlow<Int> = _streakDays.asStateFlow()

    private val _dailyNewLimit = MutableStateFlow(prefs.getInt(KEY_DAILY_NEW_LIMIT, 15))
    val dailyNewLimit: StateFlow<Int> = _dailyNewLimit.asStateFlow()

    private val _desiredRetention = MutableStateFlow(prefs.getFloat(KEY_DESIRED_RETENTION, 0.90f))
    val desiredRetention: StateFlow<Float> = _desiredRetention.asStateFlow()

    private val _speechRate = MutableStateFlow(prefs.getFloat(KEY_SPEECH_RATE, 0.92f))
    val speechRate: StateFlow<Float> = _speechRate.asStateFlow()

    private val _newWordLevelFilter = MutableStateFlow(prefs.getString(KEY_NEW_WORD_LEVEL, "ALL") ?: "ALL")
    val newWordLevelFilter: StateFlow<String> = _newWordLevelFilter.asStateFlow()

    private val _savedSession = MutableStateFlow(loadSavedSession())
    val savedSession: StateFlow<SavedSessionState> = _savedSession.asStateFlow()

    init {
        refreshDailyState()
    }

    private fun todayKey(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(Date())
    }

    private fun yesterdayKey(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun startOfTodayMillis(): Long {
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    fun refreshDailyState() {
        val today = todayKey()
        val lastDay = prefs.getString(KEY_LAST_DAY, "") ?: ""
        if (lastDay != today) {
            // New calendar day: reset both doneToday and newDoneToday
            prefs.edit()
                .putString(KEY_LAST_DAY, today)
                .putInt(KEY_DONE_TODAY, 0)
                .putInt(KEY_NEW_DONE_TODAY, 0)
                .apply()
            _doneToday.value = 0
            _newDoneToday.value = 0

            // Clear stale session from previous day
            val session = _savedSession.value
            if (session.dayKey.isNotEmpty() && session.dayKey != today) {
                clearSavedSession()
            }
        } else {
            _doneToday.value = prefs.getInt(KEY_DONE_TODAY, 0)
            _newDoneToday.value = prefs.getInt(KEY_NEW_DONE_TODAY, 0)
        }
        _streakDays.value = prefs.getInt(KEY_STREAK_DAYS, 1)
    }

    /**
     * Ensures newDoneToday reflects words already introduced today in SQLite
     * (e.g., if the user studied new words earlier today before updating the app).
     */
    fun ensureNewDoneTodayAtLeast(countFromDb: Int) {
        refreshDailyState()
        if (countFromDb > _newDoneToday.value) {
            prefs.edit().putInt(KEY_NEW_DONE_TODAY, countFromDb).apply()
            _newDoneToday.value = countFromDb
        }
        if (countFromDb > _doneToday.value) {
            prefs.edit().putInt(KEY_DONE_TODAY, countFromDb).apply()
            _doneToday.value = countFromDb
        }
    }

    fun setThemeMode(mode: ThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, mode.key).apply()
        _themeMode.value = mode
    }

    fun cycleThemeMode() {
        val next = when (_themeMode.value) {
            ThemeMode.SYSTEM -> ThemeMode.LIGHT
            ThemeMode.LIGHT -> ThemeMode.DARK
            ThemeMode.DARK -> ThemeMode.SYSTEM
        }
        setThemeMode(next)
    }

    fun setDailyNewLimit(limit: Int) {
        val clamped = limit.coerceIn(5, 50)
        prefs.edit().putInt(KEY_DAILY_NEW_LIMIT, clamped).apply()
        _dailyNewLimit.value = clamped
    }

    fun setDesiredRetention(retention: Float) {
        val clamped = retention.coerceIn(0.80f, 0.97f)
        prefs.edit().putFloat(KEY_DESIRED_RETENTION, clamped).apply()
        _desiredRetention.value = clamped
    }

    fun setSpeechRate(rate: Float) {
        val clamped = rate.coerceIn(0.7f, 1.3f)
        prefs.edit().putFloat(KEY_SPEECH_RATE, clamped).apply()
        _speechRate.value = clamped
    }

    fun setNewWordLevelFilter(level: String) {
        val normalized = if (level in listOf("ALL", "N5", "N4", "N3")) level else "ALL"
        prefs.edit().putString(KEY_NEW_WORD_LEVEL, normalized).apply()
        _newWordLevelFilter.value = normalized
    }

    /**
     * Records one card reviewed today. If [isNewWord] is true, also counts toward the daily new word quota.
     */
    fun recordCardReviewed(isNewWord: Boolean = false) {
        refreshDailyState()
        val today = todayKey()
        val lastStudyDate = prefs.getString(KEY_LAST_STUDY_DATE, "") ?: ""
        var currentStreak = prefs.getInt(KEY_STREAK_DAYS, 1)

        if (lastStudyDate != today) {
            currentStreak = if (lastStudyDate == yesterdayKey()) {
                currentStreak + 1
            } else {
                1
            }
            prefs.edit()
                .putString(KEY_LAST_STUDY_DATE, today)
                .putInt(KEY_STREAK_DAYS, currentStreak)
                .apply()
            _streakDays.value = currentStreak
        }

        val updatedDone = _doneToday.value + 1
        val updatedNewDone = if (isNewWord) _newDoneToday.value + 1 else _newDoneToday.value

        prefs.edit()
            .putString(KEY_LAST_DAY, today)
            .putInt(KEY_DONE_TODAY, updatedDone)
            .putInt(KEY_NEW_DONE_TODAY, updatedNewDone)
            .apply()

        _doneToday.value = updatedDone
        _newDoneToday.value = updatedNewDone
    }

    /**
     * Saves the active flashcard session queue and index so the user can exit and resume seamlessly.
     */
    fun saveSessionProgress(wordIds: List<Int>, currentIndex: Int, isExtraReview: Boolean = false) {
        val today = todayKey()
        if (wordIds.isEmpty() || currentIndex >= wordIds.size) {
            clearSavedSession()
            return
        }
        val serializedIds = wordIds.joinToString(",")
        prefs.edit()
            .putString(KEY_SESSION_IDS, serializedIds)
            .putInt(KEY_SESSION_INDEX, currentIndex)
            .putString(KEY_SESSION_DAY, today)
            .putBoolean(KEY_SESSION_EXTRA, isExtraReview)
            .apply()
        _savedSession.value = SavedSessionState(
            wordIds = wordIds,
            currentIndex = currentIndex,
            dayKey = today,
            isExtraReview = isExtraReview
        )
    }

    fun clearSavedSession() {
        prefs.edit()
            .remove(KEY_SESSION_IDS)
            .remove(KEY_SESSION_INDEX)
            .remove(KEY_SESSION_DAY)
            .remove(KEY_SESSION_EXTRA)
            .apply()
        _savedSession.value = SavedSessionState()
    }

    private fun loadSavedSession(): SavedSessionState {
        val today = todayKey()
        val dayKey = prefs.getString(KEY_SESSION_DAY, "") ?: ""
        if (dayKey != today) return SavedSessionState()

        val rawIds = prefs.getString(KEY_SESSION_IDS, "") ?: ""
        if (rawIds.isBlank()) return SavedSessionState()

        val ids = rawIds.split(",").mapNotNull { it.trim().toIntOrNull() }
        val index = prefs.getInt(KEY_SESSION_INDEX, 0)
        val isExtra = prefs.getBoolean(KEY_SESSION_EXTRA, false)
        if (ids.isEmpty() || index >= ids.size) return SavedSessionState()

        return SavedSessionState(
            wordIds = ids,
            currentIndex = index,
            dayKey = dayKey,
            isExtraReview = isExtra
        )
    }

    companion object {
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LAST_DAY = "last_day_key"
        private const val KEY_DONE_TODAY = "done_today_count"
        private const val KEY_NEW_DONE_TODAY = "new_done_today_count"
        private const val KEY_STREAK_DAYS = "streak_days"
        private const val KEY_LAST_STUDY_DATE = "last_study_date"
        private const val KEY_DAILY_NEW_LIMIT = "daily_new_limit"
        private const val KEY_DESIRED_RETENTION = "desired_retention"
        private const val KEY_SPEECH_RATE = "speech_rate"
        private const val KEY_NEW_WORD_LEVEL = "new_word_level_filter"
        private const val KEY_SESSION_IDS = "session_word_ids"
        private const val KEY_SESSION_INDEX = "session_current_index"
        private const val KEY_SESSION_DAY = "session_day_key"
        private const val KEY_SESSION_EXTRA = "session_is_extra"
    }
}
