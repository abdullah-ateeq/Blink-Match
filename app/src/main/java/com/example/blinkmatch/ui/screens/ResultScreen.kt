package com.example.blinkmatch.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blinkmatch.data.model.BlinkMatchResult
import com.example.blinkmatch.data.model.GameStatistics
import com.example.blinkmatch.ui.theme.BrandAmber
import com.example.blinkmatch.ui.theme.BrandCyan
import com.example.blinkmatch.ui.theme.BrandEmerald
import com.example.blinkmatch.ui.theme.BrandIndigo
import com.example.blinkmatch.ui.theme.BrandRose

@Composable
fun ResultScreen(
    result: BlinkMatchResult?,
    statistics: GameStatistics,
    reducedMotion: Boolean,
    onPlayAgainClicked: () -> Unit,
    onHomeClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler {
        onHomeClicked()
    }

    val scrollState = rememberScrollState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "BLINK MATCH",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "GAME OVER",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 2.sp,
                color = BrandRose
            )

            // New Record Badge
            val hasNewRecord = result?.isNewBestScore == true || result?.isNewBestRound == true || result?.isNewBestAccuracy == true
            if (hasNewRecord) {
                Spacer(modifier = Modifier.height(12.dp))
                AnimatedVisibility(
                    visible = true,
                    enter = if (reducedMotion) fadeIn() else fadeIn(tween(300)) + scaleIn(initialScale = 0.8f)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .background(BrandAmber.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 6.dp)
                    ) {
                        Text(
                            text = when {
                                result?.isNewBestScore == true -> "★ NEW BEST SCORE! ★"
                                result?.isNewBestRound == true -> "★ NEW BEST ROUND! ★"
                                else -> "★ NEW BEST ACCURACY! ★"
                            },
                            fontSize = 13.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = BrandAmber
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Round & Score Highlight Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "FINAL SCORE",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "${result?.score ?: 0}",
                        fontSize = 42.sp,
                        fontWeight = FontWeight.Black,
                        color = BrandCyan
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        ResultStatItem(label = "ROUND", value = "${result?.round ?: 1}", color = BrandAmber)
                        ResultStatItem(label = "ACCURACY", value = "${result?.accuracy ?: 0.0}%", color = BrandEmerald)
                        ResultStatItem(label = "PERFECT", value = "${result?.perfectRounds ?: 0}", color = BrandIndigo)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Result Comparison Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp)
                ) {
                    Text(
                        text = "YOUR RESULT COMPARISON",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.2.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    ComparisonRow(
                        label = "Remembered",
                        value = "${result?.correctPositions ?: 0} / ${result?.totalPositions ?: 0}"
                    )
                    ComparisonRow(
                        label = "Best Round",
                        value = "${statistics.bestRound}"
                    )
                    ComparisonRow(
                        label = "Best Score",
                        value = "${statistics.bestScore}"
                    )
                    ComparisonRow(
                        label = "Best Accuracy",
                        value = "${statistics.bestAccuracy}%"
                    )
                    ComparisonRow(
                        label = "Reason",
                        value = if (result?.timedOut == true) "Time expired" else "Wrong pattern"
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // Action Buttons: Play Again & Home
            Button(
                onClick = onPlayAgainClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BrandCyan)
            ) {
                Text(
                    text = "PLAY AGAIN",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp,
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedButton(
                onClick = onHomeClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    text = "HOME",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun ResultStatItem(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
            color = color
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ComparisonRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 5.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = label,
            fontSize = 13.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
