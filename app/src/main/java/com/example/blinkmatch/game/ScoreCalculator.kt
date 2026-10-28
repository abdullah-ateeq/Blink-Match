package com.example.blinkmatch.game

import kotlin.math.min
import kotlin.math.roundToInt

object ScoreCalculator {

    data class ScoreCalculation(
        val basePoints: Int,
        val speedBonus: Int,
        val multiplier: Double,
        val roundScore: Int,
        val perfectBonus: Int,
        val streakBonus: Int,
        val totalEarned: Int
    )

    fun calculateRoundScore(
        symbolCount: Int,
        remainingRecallTimeMs: Long,
        round: Int,
        isPerfect: Boolean,
        currentStreak: Int
    ): ScoreCalculation {
        val basePoints = symbolCount * 100

        val remainingSeconds = (remainingRecallTimeMs / 1000.0).coerceAtLeast(0.0)
        val speedBonus = (remainingSeconds * 20.0).roundToInt()

        val multiplier = 1.0 + (min(round - 1, 20) * 0.05)

        val roundScore = ((basePoints + speedBonus) * multiplier).roundToInt()

        val perfectBonus = if (isPerfect) 100 else 0
        // Every 5 perfect rounds (e.g. 5, 10, 15...): +250 streak bonus
        val streakBonus = if (isPerfect && currentStreak > 0 && currentStreak % 5 == 0) 250 else 0

        val totalEarned = roundScore + perfectBonus + streakBonus

        return ScoreCalculation(
            basePoints = basePoints,
            speedBonus = speedBonus,
            multiplier = multiplier,
            roundScore = roundScore,
            perfectBonus = perfectBonus,
            streakBonus = streakBonus,
            totalEarned = totalEarned
        )
    }

    fun calculateAccuracy(totalCorrectPositions: Int, totalRequiredPositions: Int): Double {
        if (totalRequiredPositions <= 0) return 0.0
        val acc = (totalCorrectPositions.toDouble() / totalRequiredPositions.toDouble()) * 100.0
        return (acc * 10.0).roundToInt() / 10.0 // 1 decimal place e.g. 94.2%
    }
}
