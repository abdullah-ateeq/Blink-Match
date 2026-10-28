package com.example.blinkmatch.viewmodel

import android.app.Application
import android.os.SystemClock
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.blinkmatch.audio.SoundManager
import com.example.blinkmatch.data.model.BlinkMatchResult
import com.example.blinkmatch.data.model.BlinkRoundConfig
import com.example.blinkmatch.data.model.Difficulty
import com.example.blinkmatch.data.model.GameState
import com.example.blinkmatch.data.model.MemoryCell
import com.example.blinkmatch.data.model.MemoryPosition
import com.example.blinkmatch.data.model.MemorySymbol
import com.example.blinkmatch.data.model.PatternResult
import com.example.blinkmatch.data.repository.GamePreferencesRepository
import com.example.blinkmatch.game.PatternGenerator
import com.example.blinkmatch.game.RoundConfigProvider
import com.example.blinkmatch.game.ScoreCalculator
import com.example.blinkmatch.haptics.HapticFeedbackHelper
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.random.Random

data class GameUiState(
    val gameState: GameState = GameState.READY,
    val difficulty: Difficulty = Difficulty.EASY,
    val round: Int = 1,
    val score: Int = 0,
    val rows: Int = 3,
    val columns: Int = 3,
    val symbols: List<MemorySymbol> = emptyList(),
    val correctPositions: Set<MemoryPosition> = emptySet(),
    val selectedPositions: Set<MemoryPosition> = emptySet(),
    val countdownNumber: Int = 3,
    val countdownText: String = "3",
    val displayTimeRemainingMs: Long = 0L,
    val displayTimeTotalMs: Long = 0L,
    val recallTimeRemainingMs: Long = 0L,
    val recallTimeTotalMs: Long = 0L,
    val currentStreak: Int = 0,
    val perfectRoundsCount: Int = 0,
    val totalCorrectPositionsAccumulated: Int = 0,
    val totalRequiredPositionsAccumulated: Int = 0,
    val feedbackMessage: String? = null,
    val earnedPointsBanner: String? = null,
    val lastEvaluationResult: PatternResult? = null,
    val lastGameResult: BlinkMatchResult? = null
)

class BlinkMatchViewModel(application: Application) : AndroidViewModel(application) {

    val preferencesRepository = GamePreferencesRepository(application.applicationContext)
    val soundManager = SoundManager()
    val hapticFeedbackHelper = HapticFeedbackHelper(application.applicationContext)

    private val _uiState = MutableStateFlow(GameUiState())
    val uiState: StateFlow<GameUiState> = _uiState.asStateFlow()

    private var activeTimerJob: Job? = null
    private var stateBeforePause: GameState = GameState.READY

    // Timestamps for accurate monotonic timekeeping across pauses
    private var displayTimeRemainingSnapshotMs: Long = 0L
    private var recallTimeRemainingSnapshotMs: Long = 0L

    val preferences = preferencesRepository.preferences
    val statistics = preferencesRepository.statistics
    val currentDifficulty = preferencesRepository.currentDifficulty

    fun setDifficulty(difficulty: Difficulty) {
        preferencesRepository.setDifficulty(difficulty)
        _uiState.update { it.copy(difficulty = difficulty) }
    }

    /**
     * Starts a new game session with the chosen or current difficulty.
     */
    fun startNewGame(difficulty: Difficulty? = null) {
        cancelAllTimers()
        val chosenDifficulty = difficulty ?: currentDifficulty.value
        setDifficulty(chosenDifficulty)

        _uiState.update {
            GameUiState(
                gameState = GameState.READY,
                difficulty = chosenDifficulty,
                round = 1,
                score = 0,
                currentStreak = 0,
                perfectRoundsCount = 0,
                totalCorrectPositionsAccumulated = 0,
                totalRequiredPositionsAccumulated = 0,
                feedbackMessage = null,
                lastEvaluationResult = null,
                lastGameResult = null
            )
        }

        startRound(roundNumber = 1)
    }

    /**
     * Begins round sequence:
     * 1. Generates pattern according to difficulty & round
     * 2. Runs countdown: 3 -> 2 -> 1 -> BLINK!
     * 3. Transitions to DISPLAYING
     */
    private fun startRound(roundNumber: Int) {
        cancelAllTimers()

        val difficulty = _uiState.value.difficulty
        val config = RoundConfigProvider.getConfig(roundNumber, difficulty)

        val previousPositions = _uiState.value.correctPositions
        val generatedSymbols = PatternGenerator.generatePattern(
            rows = config.rows,
            columns = config.columns,
            symbolCount = config.symbolCount,
            random = Random.Default,
            previousPositions = if (roundNumber > 1) previousPositions else null
        )
        val correctPositions = generatedSymbols.map { it.position }.toSet()

        _uiState.update { current ->
            current.copy(
                gameState = GameState.COUNTDOWN,
                round = roundNumber,
                rows = config.rows,
                columns = config.columns,
                symbols = generatedSymbols,
                correctPositions = correctPositions,
                selectedPositions = emptySet(),
                countdownNumber = 3,
                countdownText = "3",
                displayTimeTotalMs = config.displayTimeMs,
                displayTimeRemainingMs = config.displayTimeMs,
                recallTimeTotalMs = config.recallTimeMs,
                recallTimeRemainingMs = config.recallTimeMs,
                feedbackMessage = null,
                earnedPointsBanner = null
            )
        }

        displayTimeRemainingSnapshotMs = config.displayTimeMs
        recallTimeRemainingSnapshotMs = config.recallTimeMs

        runCountdownSequence()
    }

    private fun runCountdownSequence() {
        activeTimerJob?.cancel()
        activeTimerJob = viewModelScope.launch {
            val soundEnabled = preferences.value.soundEnabled

            // Step 3
            _uiState.update { it.copy(gameState = GameState.COUNTDOWN, countdownNumber = 3, countdownText = "3") }
            soundManager.play(SoundManager.SoundType.COUNTDOWN_TICK, soundEnabled)
            delay(550)

            // Step 2
            _uiState.update { it.copy(countdownNumber = 2, countdownText = "2") }
            soundManager.play(SoundManager.SoundType.COUNTDOWN_TICK, soundEnabled)
            delay(550)

            // Step 1
            _uiState.update { it.copy(countdownNumber = 1, countdownText = "1") }
            soundManager.play(SoundManager.SoundType.COUNTDOWN_TICK, soundEnabled)
            delay(550)

            // BLINK!
            _uiState.update { it.copy(countdownNumber = 0, countdownText = "BLINK!") }
            soundManager.play(SoundManager.SoundType.COUNTDOWN_BLINK, soundEnabled)
            delay(450)

            // Move to DISPLAYING
            startDisplayPhase(displayTimeRemainingSnapshotMs)
        }
    }

    private fun startDisplayPhase(timeRemainingMs: Long) {
        val soundEnabled = preferences.value.soundEnabled
        soundManager.play(SoundManager.SoundType.SYMBOL_APPEAR, soundEnabled)

        _uiState.update { it.copy(gameState = GameState.DISPLAYING, displayTimeRemainingMs = timeRemainingMs) }

        activeTimerJob?.cancel()
        activeTimerJob = viewModelScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            val initialMs = timeRemainingMs

            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                val remaining = (initialMs - elapsed).coerceAtLeast(0L)
                displayTimeRemainingSnapshotMs = remaining

                _uiState.update { it.copy(displayTimeRemainingMs = remaining) }

                if (remaining <= 0L) {
                    break
                }
                delay(30)
            }

            // Quick hide transition (150ms)
            _uiState.update { it.copy(gameState = GameState.HIDING) }
            delay(150)

            // Enter RECALL phase
            startRecallPhase(recallTimeRemainingSnapshotMs)
        }
    }

    private fun startRecallPhase(timeRemainingMs: Long) {
        _uiState.update { it.copy(gameState = GameState.RECALL, recallTimeRemainingMs = timeRemainingMs) }

        activeTimerJob?.cancel()
        activeTimerJob = viewModelScope.launch {
            val startTime = SystemClock.elapsedRealtime()
            val initialMs = timeRemainingMs

            while (isActive) {
                val elapsed = SystemClock.elapsedRealtime() - startTime
                val remaining = (initialMs - elapsed).coerceAtLeast(0L)
                recallTimeRemainingSnapshotMs = remaining

                _uiState.update { it.copy(recallTimeRemainingMs = remaining) }

                if (remaining <= 0L) {
                    onRecallTimeout()
                    break
                }
                delay(50)
            }
        }
    }

    /**
     * Player taps a cell during RECALL phase.
     */
    fun onCellTapped(position: MemoryPosition) {
        val state = _uiState.value
        if (state.gameState != GameState.RECALL) return

        val currentSelected = state.selectedPositions.toMutableSet()
        val soundEnabled = preferences.value.soundEnabled
        val hapticsEnabled = preferences.value.hapticsEnabled

        if (currentSelected.contains(position)) {
            currentSelected.remove(position)
            soundManager.play(SoundManager.SoundType.CELL_DESELECT, soundEnabled)
            hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.LIGHT_TICK, hapticsEnabled)
            _uiState.update { it.copy(selectedPositions = currentSelected) }
        } else {
            // Cannot select more than the target symbol count
            val targetCount = state.correctPositions.size
            if (currentSelected.size < targetCount) {
                currentSelected.add(position)
                soundManager.play(SoundManager.SoundType.CELL_TAP, soundEnabled)
                hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.LIGHT_TICK, hapticsEnabled)
                _uiState.update { it.copy(selectedPositions = currentSelected) }

                // Auto-submit when selected count equals required count
                if (currentSelected.size == targetCount) {
                    evaluateSubmission(currentSelected)
                }
            }
        }
    }

    private fun evaluateSubmission(selected: Set<MemoryPosition>) {
        cancelAllTimers()

        val state = _uiState.value
        val correct = state.correctPositions
        val result = PatternGenerator.evaluatePattern(correct = correct, selected = selected)

        val soundEnabled = preferences.value.soundEnabled
        val hapticsEnabled = preferences.value.hapticsEnabled

        val newTotalCorrect = state.totalCorrectPositionsAccumulated + result.correctCount
        val newTotalRequired = state.totalRequiredPositionsAccumulated + correct.size

        if (result.perfect) {
            // Success
            val newStreak = state.currentStreak + 1
            val newPerfectCount = state.perfectRoundsCount + 1

            val scoreDetails = ScoreCalculator.calculateRoundScore(
                symbolCount = correct.size,
                remainingRecallTimeMs = recallTimeRemainingSnapshotMs,
                round = state.round,
                isPerfect = true,
                currentStreak = newStreak
            )
            val newScore = state.score + scoreDetails.totalEarned

            val bannerText = if (scoreDetails.streakBonus > 0) {
                "MEMORY STREAK! +${scoreDetails.totalEarned}"
            } else {
                "PERFECT MATCH! +${scoreDetails.totalEarned}"
            }

            if (scoreDetails.streakBonus > 0) {
                soundManager.play(SoundManager.SoundType.PERFECT_STREAK, soundEnabled)
                hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.STREAK, hapticsEnabled)
            } else {
                soundManager.play(SoundManager.SoundType.CORRECT, soundEnabled)
                hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.SUCCESS, hapticsEnabled)
            }

            _uiState.update {
                it.copy(
                    gameState = GameState.SUCCESS,
                    score = newScore,
                    currentStreak = newStreak,
                    perfectRoundsCount = newPerfectCount,
                    totalCorrectPositionsAccumulated = newTotalCorrect,
                    totalRequiredPositionsAccumulated = newTotalRequired,
                    feedbackMessage = "PERFECT MEMORY!",
                    earnedPointsBanner = bannerText,
                    lastEvaluationResult = result
                )
            }

            // Brief celebration flash, then next round
            viewModelScope.launch {
                delay(650)
                startRound(roundNumber = state.round + 1)
            }
        } else {
            // Wrong Answer -> Game Over
            soundManager.play(SoundManager.SoundType.WRONG, soundEnabled)
            hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.ERROR, hapticsEnabled)

            _uiState.update {
                it.copy(
                    gameState = GameState.GAME_OVER,
                    currentStreak = 0,
                    totalCorrectPositionsAccumulated = newTotalCorrect,
                    totalRequiredPositionsAccumulated = newTotalRequired,
                    feedbackMessage = "WRONG PATTERN\nYou remembered: ${result.correctCount} / ${correct.size}",
                    lastEvaluationResult = result
                )
            }

            // Reveal mismatch briefly, then finish
            viewModelScope.launch {
                delay(1300)
                finishGameSession(timedOut = false)
            }
        }
    }

    private fun onRecallTimeout() {
        val state = _uiState.value
        val result = PatternGenerator.evaluatePattern(
            correct = state.correctPositions,
            selected = state.selectedPositions
        )

        val soundEnabled = preferences.value.soundEnabled
        val hapticsEnabled = preferences.value.hapticsEnabled
        soundManager.play(SoundManager.SoundType.GAME_OVER, soundEnabled)
        hapticFeedbackHelper.performHaptic(HapticFeedbackHelper.HapticType.ERROR, hapticsEnabled)

        val newTotalCorrect = state.totalCorrectPositionsAccumulated + result.correctCount
        val newTotalRequired = state.totalRequiredPositionsAccumulated + state.correctPositions.size

        _uiState.update {
            it.copy(
                gameState = GameState.GAME_OVER,
                currentStreak = 0,
                totalCorrectPositionsAccumulated = newTotalCorrect,
                totalRequiredPositionsAccumulated = newTotalRequired,
                feedbackMessage = "TIME'S UP\nYou remembered: ${result.correctCount} / ${state.correctPositions.size}",
                lastEvaluationResult = result
            )
        }

        viewModelScope.launch {
            delay(1200)
            finishGameSession(timedOut = true)
        }
    }

    private fun finishGameSession(timedOut: Boolean) {
        cancelAllTimers()
        val state = _uiState.value

        val finalAccuracy = ScoreCalculator.calculateAccuracy(
            totalCorrectPositions = state.totalCorrectPositionsAccumulated,
            totalRequiredPositions = state.totalRequiredPositionsAccumulated
        )

        val updateResult = preferencesRepository.recordGameResult(
            score = state.score,
            round = state.round,
            accuracy = finalAccuracy,
            perfectRoundsCount = state.perfectRoundsCount,
            maxStreak = state.currentStreak
        )

        val gameResult = BlinkMatchResult(
            difficulty = state.difficulty,
            round = state.round,
            score = state.score,
            correctPositions = state.totalCorrectPositionsAccumulated,
            totalPositions = state.totalRequiredPositionsAccumulated,
            accuracy = finalAccuracy,
            perfectRounds = state.perfectRoundsCount,
            perfectStreak = state.currentStreak,
            timedOut = timedOut,
            isNewBestScore = updateResult.isNewBestScore,
            isNewBestRound = updateResult.isNewBestRound,
            isNewBestAccuracy = updateResult.isNewBestAccuracy
        )

        _uiState.update { it.copy(lastGameResult = gameResult) }
    }

    /**
     * Pauses the game.
     * Stops active timers and animations, preserves pattern and selections.
     */
    fun pauseGame() {
        val current = _uiState.value.gameState
        if (current == GameState.PAUSED || current == GameState.GAME_OVER || current == GameState.READY) return

        stateBeforePause = current
        cancelAllTimers()
        _uiState.update { it.copy(gameState = GameState.PAUSED) }
    }

    /**
     * Resumes the game right where it was paused.
     */
    fun resumeGame() {
        if (_uiState.value.gameState != GameState.PAUSED) return

        when (stateBeforePause) {
            GameState.COUNTDOWN -> {
                runCountdownSequence()
            }
            GameState.DISPLAYING -> {
                startDisplayPhase(displayTimeRemainingSnapshotMs)
            }
            GameState.HIDING -> {
                startRecallPhase(recallTimeRemainingSnapshotMs)
            }
            GameState.RECALL -> {
                startRecallPhase(recallTimeRemainingSnapshotMs)
            }
            else -> {
                startRecallPhase(recallTimeRemainingSnapshotMs)
            }
        }
    }

    /**
     * Restarts game session immediately with current difficulty.
     */
    fun restartGame() {
        startNewGame(_uiState.value.difficulty)
    }

    /**
     * Exits game to Home.
     */
    fun exitGame() {
        cancelAllTimers()
        _uiState.update { it.copy(gameState = GameState.READY) }
    }

    fun resetStatistics() {
        preferencesRepository.resetStatistics()
    }

    private fun cancelAllTimers() {
        activeTimerJob?.cancel()
        activeTimerJob = null
    }

    override fun onCleared() {
        super.onCleared()
        cancelAllTimers()
    }
}
