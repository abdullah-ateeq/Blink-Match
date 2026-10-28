package com.example.blinkmatch.game

import com.example.blinkmatch.data.model.BlinkRoundConfig
import com.example.blinkmatch.data.model.Difficulty
import kotlin.math.max
import kotlin.math.min

object RoundConfigProvider {

    fun getConfig(round: Int, difficulty: Difficulty): BlinkRoundConfig {
        val safeRound = max(1, round)

        val (gridSize, symbolCount, displayTimeMs, recallTimeMs) = when (difficulty) {
            Difficulty.EASY -> {
                // Easy: Start 3x3, progress to 4x4 around round 4
                val size = if (safeRound < 4) 3 else 4
                // Symbols: 2 at round 1-2, 3 at round 3-4, 4 at round 5-6, 5 at round 7-8, max 6
                val symbols = min(6, 2 + ((safeRound - 1) / 2))
                // Display time: starts at 2500ms, decreases by 80ms per round, min 800ms
                val display = max(800L, 2500L - (safeRound - 1) * 80L)
                // Recall time formula: base 4s + 1s per symbol, clamped 5s to 12s
                val baseRecall = 4000L + (symbols * 1000L)
                val recall = baseRecall.coerceIn(5000L, 12000L)
                RoundValues(size, symbols, display, recall)
            }
            Difficulty.MEDIUM -> {
                // Medium: Start 4x4, progress to 5x5 around round 5
                val size = if (safeRound < 5) 4 else 5
                // Symbols: 3 at round 1, then grows gradually up to 9
                val symbols = min(9, 3 + ((safeRound - 1) * 2 / 3))
                // Display time: starts at 2000ms, decreases by 90ms per round, min 800ms
                val display = max(800L, 2000L - (safeRound - 1) * 90L)
                // Recall time formula: base 3s + 0.8s per symbol, clamped 4s to 10s
                val baseRecall = 3000L + (symbols * 800L)
                val recall = baseRecall.coerceIn(4000L, 10000L)
                RoundValues(size, symbols, display, recall)
            }
            Difficulty.HARD -> {
                // Hard: Start 4x4 (round 1-3), 5x5 (round 4-7), 6x6 (round 8+)
                val size = when {
                    safeRound < 4 -> 4
                    safeRound < 8 -> 5
                    else -> 6
                }
                // Symbols: 4 at round 1, then grows up to 12
                val symbols = min(12, 4 + ((safeRound - 1) * 3 / 4))
                // Display time: starts at 1500ms, decreases by 80ms per round, min 800ms
                val display = max(800L, 1500L - (safeRound - 1) * 80L)
                // Recall time: base 2.5s + 0.6s per symbol, clamped 3.5s to 8s
                val baseRecall = 2500L + (symbols * 600L)
                val recall = baseRecall.coerceIn(3500L, 8000L)
                RoundValues(size, symbols, display, recall)
            }
        }

        return BlinkRoundConfig(
            round = safeRound,
            rows = gridSize,
            columns = gridSize,
            symbolCount = symbolCount,
            displayTimeMs = displayTimeMs,
            recallTimeMs = recallTimeMs
        )
    }

    private data class RoundValues(
        val size: Int,
        val symbols: Int,
        val displayTimeMs: Long,
        val recallTimeMs: Long
    )
}
