package com.example.blinkmatch.data.model

enum class AppThemeMode {
    SYSTEM,
    LIGHT,
    DARK
}

data class UserPreferences(
    val themeMode: AppThemeMode = AppThemeMode.SYSTEM,
    val soundEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true,
    val reducedMotion: Boolean = false
)

data class GameStatistics(
    val bestScore: Int = 0,
    val bestRound: Int = 0,
    val bestAccuracy: Double = 0.0,
    val bestPerfectStreak: Int = 0,
    val totalGamesPlayed: Int = 0,
    val totalPerfectRounds: Int = 0
)
