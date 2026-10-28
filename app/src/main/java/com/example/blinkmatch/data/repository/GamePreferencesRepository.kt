package com.example.blinkmatch.data.repository

import android.content.Context
import android.content.SharedPreferences
import com.example.blinkmatch.data.model.AppThemeMode
import com.example.blinkmatch.data.model.Difficulty
import com.example.blinkmatch.data.model.GameStatistics
import com.example.blinkmatch.data.model.UserPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.roundToInt

class GamePreferencesRepository(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    private val _preferences = MutableStateFlow(loadPreferences())
    val preferences: StateFlow<UserPreferences> = _preferences.asStateFlow()

    private val _currentDifficulty = MutableStateFlow(loadDifficulty())
    val currentDifficulty: StateFlow<Difficulty> = _currentDifficulty.asStateFlow()

    private val _statistics = MutableStateFlow(loadStatistics())
    val statistics: StateFlow<GameStatistics> = _statistics.asStateFlow()

    fun updateThemeMode(themeMode: AppThemeMode) {
        prefs.edit().putString(KEY_THEME_MODE, themeMode.name).apply()
        _preferences.value = _preferences.value.copy(themeMode = themeMode)
    }

    fun updateSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
        _preferences.value = _preferences.value.copy(soundEnabled = enabled)
    }

    fun updateHapticsEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_HAPTICS_ENABLED, enabled).apply()
        _preferences.value = _preferences.value.copy(hapticsEnabled = enabled)
    }

    fun updateReducedMotion(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_REDUCED_MOTION, enabled).apply()
        _preferences.value = _preferences.value.copy(reducedMotion = enabled)
    }

    fun setDifficulty(difficulty: Difficulty) {
        prefs.edit().putString(KEY_DIFFICULTY, difficulty.name).apply()
        _currentDifficulty.value = difficulty
    }

    fun recordGameResult(
        score: Int,
        round: Int,
        accuracy: Double,
        perfectRoundsCount: Int,
        maxStreak: Int
    ): RecordUpdateResult {
        val current = _statistics.value

        val isNewBestScore = score > current.bestScore
        val isNewBestRound = round > current.bestRound
        val isNewBestAccuracy = accuracy > current.bestAccuracy && accuracy > 0.0

        val newBestScore = maxOf(current.bestScore, score)
        val newBestRound = maxOf(current.bestRound, round)
        val newBestAccuracy = if (current.bestAccuracy <= 0.0) accuracy else maxOf(current.bestAccuracy, accuracy)
        val newBestStreak = maxOf(current.bestPerfectStreak, maxStreak)
        val newTotalGames = current.totalGamesPlayed + 1
        val newTotalPerfect = current.totalPerfectRounds + perfectRoundsCount

        val updatedStats = GameStatistics(
            bestScore = newBestScore,
            bestRound = newBestRound,
            bestAccuracy = (newBestAccuracy * 10).roundToInt() / 10.0,
            bestPerfectStreak = newBestStreak,
            totalGamesPlayed = newTotalGames,
            totalPerfectRounds = newTotalPerfect
        )

        prefs.edit()
            .putInt(KEY_BEST_SCORE, updatedStats.bestScore)
            .putInt(KEY_BEST_ROUND, updatedStats.bestRound)
            .putFloat(KEY_BEST_ACCURACY, updatedStats.bestAccuracy.toFloat())
            .putInt(KEY_BEST_STREAK, updatedStats.bestPerfectStreak)
            .putInt(KEY_TOTAL_GAMES, updatedStats.totalGamesPlayed)
            .putInt(KEY_TOTAL_PERFECT, updatedStats.totalPerfectRounds)
            .apply()

        _statistics.value = updatedStats

        return RecordUpdateResult(
            isNewBestScore = isNewBestScore,
            isNewBestRound = isNewBestRound,
            isNewBestAccuracy = isNewBestAccuracy
        )
    }

    fun resetStatistics() {
        prefs.edit()
            .putInt(KEY_BEST_SCORE, 0)
            .putInt(KEY_BEST_ROUND, 0)
            .putFloat(KEY_BEST_ACCURACY, 0.0f)
            .putInt(KEY_BEST_STREAK, 0)
            .putInt(KEY_TOTAL_GAMES, 0)
            .putInt(KEY_TOTAL_PERFECT, 0)
            .apply()

        _statistics.value = GameStatistics(
            bestScore = 0,
            bestRound = 0,
            bestAccuracy = 0.0,
            bestPerfectStreak = 0,
            totalGamesPlayed = 0,
            totalPerfectRounds = 0
        )
    }

    private fun loadPreferences(): UserPreferences {
        val themeStr = prefs.getString(KEY_THEME_MODE, AppThemeMode.SYSTEM.name) ?: AppThemeMode.SYSTEM.name
        val themeMode = runCatching { AppThemeMode.valueOf(themeStr) }.getOrDefault(AppThemeMode.SYSTEM)
        val soundEnabled = prefs.getBoolean(KEY_SOUND_ENABLED, true)
        val hapticsEnabled = prefs.getBoolean(KEY_HAPTICS_ENABLED, true)
        val reducedMotion = prefs.getBoolean(KEY_REDUCED_MOTION, false)

        return UserPreferences(
            themeMode = themeMode,
            soundEnabled = soundEnabled,
            hapticsEnabled = hapticsEnabled,
            reducedMotion = reducedMotion
        )
    }

    private fun loadDifficulty(): Difficulty {
        val diffStr = prefs.getString(KEY_DIFFICULTY, Difficulty.EASY.name) ?: Difficulty.EASY.name
        return runCatching { Difficulty.valueOf(diffStr) }.getOrDefault(Difficulty.EASY)
    }

    private fun loadStatistics(): GameStatistics {
        return GameStatistics(
            bestScore = prefs.getInt(KEY_BEST_SCORE, 0),
            bestRound = prefs.getInt(KEY_BEST_ROUND, 0),
            bestAccuracy = prefs.getFloat(KEY_BEST_ACCURACY, 0.0f).toDouble(),
            bestPerfectStreak = prefs.getInt(KEY_BEST_STREAK, 0),
            totalGamesPlayed = prefs.getInt(KEY_TOTAL_GAMES, 0),
            totalPerfectRounds = prefs.getInt(KEY_TOTAL_PERFECT, 0)
        )
    }

    data class RecordUpdateResult(
        val isNewBestScore: Boolean,
        val isNewBestRound: Boolean,
        val isNewBestAccuracy: Boolean
    )

    companion object {
        private const val PREFS_NAME = "blink_match_prefs"

        private const val KEY_THEME_MODE = "pref_theme_mode"
        private const val KEY_SOUND_ENABLED = "pref_sound_enabled"
        private const val KEY_HAPTICS_ENABLED = "pref_haptics_enabled"
        private const val KEY_REDUCED_MOTION = "pref_reduced_motion"
        private const val KEY_DIFFICULTY = "pref_difficulty"

        private const val KEY_BEST_SCORE = "stat_best_score"
        private const val KEY_BEST_ROUND = "stat_best_round"
        private const val KEY_BEST_ACCURACY = "stat_best_accuracy"
        private const val KEY_BEST_STREAK = "stat_best_streak"
        private const val KEY_TOTAL_GAMES = "stat_total_games"
        private const val KEY_TOTAL_PERFECT = "stat_total_perfect"
    }
}
