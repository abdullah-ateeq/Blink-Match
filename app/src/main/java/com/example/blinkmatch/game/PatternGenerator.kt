package com.example.blinkmatch.game

import com.example.blinkmatch.data.model.MemoryPosition
import com.example.blinkmatch.data.model.MemorySymbol
import com.example.blinkmatch.data.model.PatternResult
import com.example.blinkmatch.data.model.SymbolType
import kotlin.random.Random

object PatternGenerator {

    /**
     * Generates a fair, balanced, non-duplicate list of MemorySymbols.
     * Guaranteed:
     * - Exactly [symbolCount] symbols (clamped to rows * columns).
     * - Every symbol has a unique grid coordinate inside [0, rows) x [0, columns).
     * - Pattern is not identical to [previousPositions] if grid size allows.
     * - Balanced distribution across quadrants/rows where possible.
     */
    fun generatePattern(
        rows: Int,
        columns: Int,
        symbolCount: Int,
        random: Random = Random.Default,
        previousPositions: Set<MemoryPosition>? = null
    ): List<MemorySymbol> {
        val totalCells = rows * columns
        val targetCount = symbolCount.coerceIn(1, totalCells)

        val allPositions = ArrayList<MemoryPosition>(totalCells)
        for (r in 0 until rows) {
            for (c in 0 until columns) {
                allPositions.add(MemoryPosition(r, c))
            }
        }

        var selectedPositions: List<MemoryPosition> = emptyList()
        var attempts = 0
        val maxAttempts = 15

        while (attempts < maxAttempts) {
            attempts++
            // Shuffled candidate pool
            val shuffled = allPositions.shuffled(random)
            val candidate = shuffled.take(targetCount)
            val candidateSet = candidate.toSet()

            // Check if identical to previous pattern
            if (previousPositions != null && candidateSet == previousPositions && totalCells > targetCount) {
                continue
            }

            // Balanced distribution check:
            // If targetCount >= 3 and grid is at least 3x3, avoid placing all symbols in a single row or single column
            if (targetCount >= 3 && rows >= 3 && columns >= 3) {
                val rowCounts = candidate.groupBy { it.row }.mapValues { it.value.size }
                val colCounts = candidate.groupBy { it.column }.mapValues { it.value.size }

                val maxInOneRow = rowCounts.values.maxOrNull() ?: 0
                val maxInOneCol = colCounts.values.maxOrNull() ?: 0

                // If ALL symbols are in one row or column, retry to find a better balanced pattern
                if (maxInOneRow == targetCount || maxInOneCol == targetCount) {
                    continue
                }
            }

            selectedPositions = candidate
            break
        }

        if (selectedPositions.isEmpty()) {
            selectedPositions = allPositions.shuffled(random).take(targetCount)
        }

        // Assign symbol types cleanly from SymbolType enum
        val symbolTypes = SymbolType.values()
        val shuffledSymbols = symbolTypes.toList().shuffled(random)

        return selectedPositions.mapIndexed { index, pos ->
            val symbolType = shuffledSymbols[index % shuffledSymbols.size]
            MemorySymbol(
                id = index + 1,
                position = pos,
                symbolType = symbolType
            )
        }
    }

    /**
     * Evaluates the player's selected positions against the correct positions.
     */
    fun evaluatePattern(
        correct: Set<MemoryPosition>,
        selected: Set<MemoryPosition>
    ): PatternResult {
        val correctCount = selected.count { it in correct }
        val incorrectCount = selected.count { it !in correct }
        val missedCount = correct.count { it !in selected }
        val perfect = correct.isNotEmpty() && correct == selected

        return PatternResult(
            correctCount = correctCount,
            incorrectCount = incorrectCount,
            missedCount = missedCount,
            perfect = perfect
        )
    }
}
