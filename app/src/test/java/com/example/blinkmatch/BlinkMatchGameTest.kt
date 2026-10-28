package com.example.blinkmatch

import com.example.blinkmatch.data.model.Difficulty
import com.example.blinkmatch.data.model.MemoryPosition
import com.example.blinkmatch.game.PatternGenerator
import com.example.blinkmatch.game.RoundConfigProvider
import com.example.blinkmatch.game.ScoreCalculator
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.random.Random

class BlinkMatchGameTest {

    @Test
    fun roundTest_3x3_2symbols_has2UniquePositions() {
        val pattern = PatternGenerator.generatePattern(
            rows = 3,
            columns = 3,
            symbolCount = 2,
            random = Random(42)
        )

        assertEquals(2, pattern.size)
        val positions = pattern.map { it.position }.toSet()
        assertEquals(2, positions.size)

        for (pos in positions) {
            assertTrue(pos.row in 0 until 3)
            assertTrue(pos.column in 0 until 3)
        }
    }

    @Test
    fun patternTest_100Generations_allValidAndUnique() {
        val random = Random(12345)
        for (i in 1..100) {
            val rows = random.nextInt(3, 7) // 3 to 6
            val cols = rows
            val maxSymbols = minOf(12, rows * cols)
            val symbolCount = random.nextInt(2, maxSymbols + 1)

            val pattern = PatternGenerator.generatePattern(
                rows = rows,
                columns = cols,
                symbolCount = symbolCount,
                random = random
            )

            // 1. Correct count
            assertEquals("Run $i symbol count mismatch", symbolCount, pattern.size)

            // 2. No duplicate positions
            val uniquePositions = pattern.map { it.position }.toSet()
            assertEquals("Run $i duplicate positions detected", symbolCount, uniquePositions.size)

            // 3. Every position is inside the grid
            for (sym in pattern) {
                assertTrue("Row out of bounds: ${sym.position.row}", sym.position.row in 0 until rows)
                assertTrue("Column out of bounds: ${sym.position.column}", sym.position.column in 0 until cols)
            }
        }
    }

    @Test
    fun evaluationTest_exactMatch_isPerfect() {
        val correct = setOf(
            MemoryPosition(0, 1),
            MemoryPosition(1, 2),
            MemoryPosition(2, 0)
        )
        val selected = setOf(
            MemoryPosition(0, 1),
            MemoryPosition(1, 2),
            MemoryPosition(2, 0)
        )

        val result = PatternGenerator.evaluatePattern(correct, selected)

        assertTrue(result.perfect)
        assertEquals(3, result.correctCount)
        assertEquals(0, result.incorrectCount)
        assertEquals(0, result.missedCount)
    }

    @Test
    fun partialTest_someMissing_notPerfect() {
        val correct = setOf(
            MemoryPosition(0, 0), // A
            MemoryPosition(0, 1), // B
            MemoryPosition(0, 2)  // C
        )
        val selected = setOf(
            MemoryPosition(0, 0), // A
            MemoryPosition(0, 1)  // B
        )

        val result = PatternGenerator.evaluatePattern(correct, selected)

        assertFalse(result.perfect)
        assertEquals(2, result.correctCount)
        assertEquals(1, result.missedCount)
        assertEquals(0, result.incorrectCount)
    }

    @Test
    fun wrongTest_incorrectSelection_recordsMismatch() {
        val correct = setOf(
            MemoryPosition(0, 0), // A
            MemoryPosition(0, 1), // B
            MemoryPosition(0, 2)  // C
        )
        val selected = setOf(
            MemoryPosition(0, 0), // A
            MemoryPosition(0, 1), // B
            MemoryPosition(1, 1)  // D (wrong)
        )

        val result = PatternGenerator.evaluatePattern(correct, selected)

        assertFalse(result.perfect)
        assertEquals(2, result.correctCount)
        assertEquals(1, result.incorrectCount)
        assertEquals(1, result.missedCount)
    }

    @Test
    fun scoreFormulaTest_deterministicCalculation() {
        // Example from prompt:
        // Symbols: 5, Time remaining: 4 seconds (4000ms), Round: 5
        // Base: 500, Speed: 80, Multiplier: 1.20, Final: 696
        val calc = ScoreCalculator.calculateRoundScore(
            symbolCount = 5,
            remainingRecallTimeMs = 4000L,
            round = 5,
            isPerfect = false,
            currentStreak = 0
        )

        assertEquals(500, calc.basePoints)
        assertEquals(80, calc.speedBonus)
        assertEquals(1.20, calc.multiplier, 0.001)
        assertEquals(696, calc.roundScore)
        assertEquals(0, calc.perfectBonus)
        assertEquals(0, calc.streakBonus)
        assertEquals(696, calc.totalEarned)
    }

    @Test
    fun scoreFormulaTest_perfectRoundAndStreakBonus() {
        val calc = ScoreCalculator.calculateRoundScore(
            symbolCount = 4,
            remainingRecallTimeMs = 2000L,
            round = 5,
            isPerfect = true,
            currentStreak = 5 // Multiple of 5
        )

        assertEquals(400, calc.basePoints)
        assertEquals(40, calc.speedBonus)
        assertEquals(1.20, calc.multiplier, 0.001)
        // (440) * 1.20 = 528
        assertEquals(528, calc.roundScore)
        assertEquals(100, calc.perfectBonus)
        assertEquals(250, calc.streakBonus)
        assertEquals(528 + 100 + 250, calc.totalEarned)
    }

    @Test
    fun accuracyTest_correctPercentageCalculation() {
        val acc = ScoreCalculator.calculateAccuracy(
            totalCorrectPositions = 18,
            totalRequiredPositions = 20
        )
        assertEquals(90.0, acc, 0.01)

        val acc2 = ScoreCalculator.calculateAccuracy(
            totalCorrectPositions = 7,
            totalRequiredPositions = 8
        )
        assertEquals(87.5, acc2, 0.01)
    }

    @Test
    fun roundConfigTest_easyMediumHardBounds() {
        // Easy Round 1: 3x3, 2 symbols, 2500ms display, >= 5000ms recall
        val easyR1 = RoundConfigProvider.getConfig(1, Difficulty.EASY)
        assertEquals(3, easyR1.rows)
        assertEquals(3, easyR1.columns)
        assertEquals(2, easyR1.symbolCount)
        assertEquals(2500L, easyR1.displayTimeMs)
        assertTrue(easyR1.recallTimeMs >= 5000L)

        // Hard Round 10: 6x6, up to 12 symbols, min 800ms display
        val hardR10 = RoundConfigProvider.getConfig(10, Difficulty.HARD)
        assertEquals(6, hardR10.rows)
        assertEquals(6, hardR10.columns)
        assertTrue(hardR10.symbolCount in 4..12)
        assertTrue(hardR10.displayTimeMs >= 800L)
    }

    @Test
    fun patternGenerator_avoidsImmediateDuplicatePattern() {
        val rows = 3
        val cols = 3
        val symbols = 3
        val r = Random(999)

        val first = PatternGenerator.generatePattern(rows, cols, symbols, r)
        val firstPositions = first.map { it.position }.toSet()

        val second = PatternGenerator.generatePattern(rows, cols, symbols, r, previousPositions = firstPositions)
        val secondPositions = second.map { it.position }.toSet()

        assertNotEquals(firstPositions, secondPositions)
    }
}
