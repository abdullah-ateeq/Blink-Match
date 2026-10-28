package com.example.blinkmatch.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.blinkmatch.ui.screens.AboutScreen
import com.example.blinkmatch.ui.screens.GameScreen
import com.example.blinkmatch.ui.screens.HomeScreen
import com.example.blinkmatch.ui.screens.ResultScreen
import com.example.blinkmatch.ui.screens.SettingsScreen
import com.example.blinkmatch.ui.screens.SplashScreen
import com.example.blinkmatch.viewmodel.BlinkMatchViewModel

@Composable
fun BlinkMatchNavHost(
    viewModel: BlinkMatchViewModel,
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    val uiState by viewModel.uiState.collectAsState()
    val preferences by viewModel.preferences.collectAsState()
    val statistics by viewModel.statistics.collectAsState()
    val currentDifficulty by viewModel.currentDifficulty.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route,
        modifier = modifier
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(
                onSplashFinished = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Splash.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                statistics = statistics,
                currentDifficulty = currentDifficulty,
                onDifficultySelected = { diff ->
                    viewModel.setDifficulty(diff)
                },
                onPlayClicked = {
                    viewModel.startNewGame(currentDifficulty)
                    navController.navigate(Screen.Game.route)
                },
                onSettingsClicked = {
                    navController.navigate(Screen.Settings.route)
                },
                onAboutClicked = {
                    navController.navigate(Screen.About.route)
                }
            )
        }

        composable(Screen.Game.route) {
            GameScreen(
                uiState = uiState,
                reducedMotion = preferences.reducedMotion,
                onCellClick = { pos ->
                    viewModel.onCellTapped(pos)
                },
                onPauseClick = {
                    viewModel.pauseGame()
                },
                onResumeClick = {
                    viewModel.resumeGame()
                },
                onRestartClick = {
                    viewModel.restartGame()
                },
                onExitClick = {
                    viewModel.exitGame()
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                },
                onGameOverFinished = {
                    navController.navigate(Screen.Result.route) {
                        popUpTo(Screen.Game.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Result.route) {
            ResultScreen(
                result = uiState.lastGameResult,
                statistics = statistics,
                reducedMotion = preferences.reducedMotion,
                onPlayAgainClicked = {
                    viewModel.startNewGame(currentDifficulty)
                    navController.navigate(Screen.Game.route) {
                        popUpTo(Screen.Result.route) { inclusive = true }
                    }
                },
                onHomeClicked = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Home.route) { inclusive = false }
                    }
                }
            )
        }

        composable(Screen.Settings.route) {
            SettingsScreen(
                preferences = preferences,
                onThemeModeChanged = { mode ->
                    viewModel.preferencesRepository.updateThemeMode(mode)
                },
                onSoundChanged = { enabled ->
                    viewModel.preferencesRepository.updateSoundEnabled(enabled)
                },
                onHapticsChanged = { enabled ->
                    viewModel.preferencesRepository.updateHapticsEnabled(enabled)
                },
                onReducedMotionChanged = { enabled ->
                    viewModel.preferencesRepository.updateReducedMotion(enabled)
                },
                onResetStatistics = {
                    viewModel.resetStatistics()
                },
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }

        composable(Screen.About.route) {
            AboutScreen(
                onBackClicked = {
                    navController.popBackStack()
                }
            )
        }
    }
}
