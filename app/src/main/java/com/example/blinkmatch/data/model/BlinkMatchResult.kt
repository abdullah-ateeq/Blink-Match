package com.example.blinkmatch.data.model

data class BlinkMatchResult(
    val difficulty: Difficulty,
    val round: Int,
    val score: Int,
    val correctPositions: Int,
    val totalPositions: Int,
    val accuracy: Double,
    val perfectRounds: Int,
    val perfectStreak: Int,
    val timedOut: Boolean,
    val isNewBestScore: Boolean = false,
    val isNewBestRound: Boolean = false,
    val isNewBestAccuracy: Boolean = false
)
