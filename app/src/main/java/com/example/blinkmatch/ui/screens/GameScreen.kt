package com.example.blinkmatch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blinkmatch.data.model.GameState
import com.example.blinkmatch.ui.components.CountdownOverlay
import com.example.blinkmatch.ui.components.GameBoard
import com.example.blinkmatch.ui.components.PauseDialog
import com.example.blinkmatch.ui.theme.BrandAmber
import com.example.blinkmatch.ui.theme.BrandCyan
import com.example.blinkmatch.ui.theme.BrandEmerald
import com.example.blinkmatch.ui.theme.BrandRose
import com.example.blinkmatch.viewmodel.GameUiState

@Composable
fun GameScreen(
    uiState: GameUiState,
    reducedMotion: Boolean,
    onCellClick: (com.example.blinkmatch.data.model.MemoryPosition) -> Unit,
    onPauseClick: () -> Unit,
    onResumeClick: () -> Unit,
    onRestartClick: () -> Unit,
    onExitClick: () -> Unit,
    onGameOverFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Intercept back button to show pause dialog
    BackHandler {
        if (uiState.gameState != GameState.PAUSED && uiState.gameState != GameState.GAME_OVER) {
            onPauseClick()
        }
    }

    // Trigger navigation when game over result is ready
    LaunchedEffect(uiState.lastGameResult) {
        if (uiState.lastGameResult != null) {
            onGameOverFinished()
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Bar: Round, Score, Pause
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ROUND ${uiState.round}",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black,
                        letterSpacing = 1.sp,
                        color = MaterialTheme.colorScheme.onBackground
                    )
                    Text(
                        text = "SCORE ${uiState.score}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = BrandCyan
                    )
                }

                if (uiState.currentStreak >= 2) {
                    Card(
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = BrandAmber.copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = "STREAK ×${uiState.currentStreak}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandAmber,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        )
                    }
                }

                IconButton(
                    onClick = onPauseClick,
                    enabled = uiState.gameState != GameState.PAUSED && uiState.gameState != GameState.GAME_OVER,
                    modifier = Modifier.size(44.dp)
                ) {
                    Text(
                        text = "Ⅱ",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Status Banner / Instructions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (uiState.gameState) {
                    GameState.DISPLAYING -> {
                        val seconds = (uiState.displayTimeRemainingMs / 100.0).toInt() / 10.0
                        Text(
                            text = "MEMORIZE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = BrandAmber
                        )
                        Text(
                            text = String.format("%.1fs", seconds),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandAmber
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = (uiState.displayTimeRemainingMs.toFloat() / uiState.displayTimeTotalMs.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = BrandAmber,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    GameState.HIDING -> {
                        Text(
                            text = "GET READY...",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    GameState.RECALL -> {
                        val seconds = (uiState.recallTimeRemainingMs / 100.0).toInt() / 10.0
                        val timerColor = if (seconds <= 2.5) BrandRose else BrandCyan
                        Text(
                            text = "REPRODUCE",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            letterSpacing = 1.5.sp,
                            color = timerColor
                        )
                        Text(
                            text = String.format("%.1fs", seconds),
                            fontSize = 22.sp,
                            fontWeight = FontWeight.Black,
                            color = timerColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        val progress = (uiState.recallTimeRemainingMs.toFloat() / uiState.recallTimeTotalMs.toFloat()).coerceIn(0f, 1f)
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth(0.6f)
                                .height(5.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = timerColor,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                    GameState.SUCCESS -> {
                        Text(
                            text = uiState.earnedPointsBanner ?: "PERFECT MATCH!",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Black,
                            color = BrandEmerald,
                            textAlign = TextAlign.Center
                        )
                    }
                    GameState.GAME_OVER -> {
                        Text(
                            text = uiState.feedbackMessage ?: "GAME OVER",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = BrandRose,
                            textAlign = TextAlign.Center
                        )
                    }
                    else -> {
                        Text(
                            text = " ",
                            fontSize = 18.sp
                        )
                    }
                }

                // Selection counter during recall
                if (uiState.gameState == GameState.RECALL) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Selected: ${uiState.selectedPositions.size} / ${uiState.correctPositions.size}",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Game Board - fits without scrolling!
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                GameBoard(
                    rows = uiState.rows,
                    columns = uiState.columns,
                    gameState = uiState.gameState,
                    symbols = uiState.symbols,
                    correctPositions = uiState.correctPositions,
                    selectedPositions = uiState.selectedPositions,
                    reducedMotion = reducedMotion,
                    onCellClick = onCellClick
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }

        // Countdown Overlay
        AnimatedVisibility(
            visible = uiState.gameState == GameState.COUNTDOWN,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            CountdownOverlay(
                countdownText = uiState.countdownText,
                reducedMotion = reducedMotion
            )
        }

        // Pause Dialog
        if (uiState.gameState == GameState.PAUSED) {
            PauseDialog(
                onResume = onResumeClick,
                onRestart = onRestartClick,
                onExit = onExitClick
            )
        }
    }
}
