package com.example.blinkmatch.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.repeatable
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blinkmatch.data.model.GameState
import com.example.blinkmatch.data.model.MemorySymbol
import com.example.blinkmatch.ui.theme.BrandCyan
import com.example.blinkmatch.ui.theme.BrandEmerald
import com.example.blinkmatch.ui.theme.BrandRose
import kotlin.math.roundToInt

@Composable
fun GridCellView(
    row: Int,
    column: Int,
    gameState: GameState,
    symbol: MemorySymbol?,
    isSelected: Boolean,
    isCorrectPosition: Boolean,
    reducedMotion: Boolean,
    onCellClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isDisplaying = gameState == GameState.DISPLAYING
    val isRecall = gameState == GameState.RECALL
    val isSuccess = gameState == GameState.SUCCESS
    val isGameOver = gameState == GameState.GAME_OVER

    val shakeOffset = remember { Animatable(0f) }

    // Wrong answer shake animation for incorrectly selected cell or during game over
    LaunchedEffect(isGameOver) {
        if (isGameOver && isSelected && !isCorrectPosition && !reducedMotion) {
            shakeOffset.animateTo(
                targetValue = 0f,
                animationSpec = repeatable(
                    iterations = 4,
                    animation = tween(durationMillis = 60, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                initialVelocity = 12f
            )
        }
    }

    val selectionScale by animateFloatAsState(
        targetValue = if (isSelected && !reducedMotion) 0.96f else 1.0f,
        animationSpec = tween(durationMillis = 140, easing = FastOutSlowInEasing),
        label = "cellScale"
    )

    // Cell background & border styling based on state
    val cellShape = RoundedCornerShape(12.dp)

    val (bgColor, borderColor, borderWidth) = when {
        isSuccess && isSelected -> {
            Triple(
                BrandEmerald.copy(alpha = 0.28f),
                BrandEmerald,
                2.5.dp
            )
        }
        isGameOver && isSelected && !isCorrectPosition -> {
            // Player picked wrong cell
            Triple(
                BrandRose.copy(alpha = 0.32f),
                BrandRose,
                2.5.dp
            )
        }
        isGameOver && isCorrectPosition -> {
            // Correct position revealed after game over
            Triple(
                BrandEmerald.copy(alpha = 0.24f),
                BrandEmerald,
                2.5.dp
            )
        }
        isSelected -> {
            Triple(
                BrandCyan.copy(alpha = 0.22f),
                BrandCyan,
                2.dp
            )
        }
        else -> {
            Triple(
                MaterialTheme.colorScheme.surface,
                MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                1.5.dp
            )
        }
    }

    val cellDescription = "Row ${row + 1}, Column ${column + 1}" + when {
        isSelected -> ", Selected"
        isDisplaying && symbol != null -> ", Symbol ${symbol.symbolType.name}"
        else -> ""
    }

    Box(
        modifier = modifier
            .offset { IntOffset(shakeOffset.value.roundToInt(), 0) }
            .scale(selectionScale)
            .clip(cellShape)
            .background(bgColor)
            .border(BorderStroke(borderWidth, borderColor), cellShape)
            .clickable(
                enabled = isRecall,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onCellClick
            )
            .semantics { contentDescription = cellDescription }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        // Displaying phase: show symbol
        AnimatedVisibility(
            visible = isDisplaying && symbol != null,
            enter = if (reducedMotion) fadeIn() else fadeIn(tween(180)) + scaleIn(initialScale = 0.88f, animationSpec = tween(180)),
            exit = if (reducedMotion) fadeOut() else fadeOut(tween(140)) + scaleOut(targetScale = 0.88f, animationSpec = tween(140))
        ) {
            if (symbol != null) {
                SymbolIcon(
                    symbolType = symbol.symbolType,
                    modifier = Modifier.fillMaxSize(0.68f)
                )
            }
        }

        // Recall phase: Show checkmark if selected
        if (isRecall && isSelected) {
            Text(
                text = "✓",
                color = BrandCyan,
                fontSize = 28.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Success phase: Flash green checkmark
        if (isSuccess && isSelected) {
            Text(
                text = "✓",
                color = BrandEmerald,
                fontSize = 30.sp,
                fontWeight = FontWeight.Black
            )
        }

        // Game Over reveal phase:
        if (isGameOver) {
            when {
                isSelected && !isCorrectPosition -> {
                    // Wrong selection mark
                    Text(
                        text = "✕",
                        color = BrandRose,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
                isCorrectPosition -> {
                    // Correct position revealed
                    Text(
                        text = "✓",
                        color = BrandEmerald,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}
