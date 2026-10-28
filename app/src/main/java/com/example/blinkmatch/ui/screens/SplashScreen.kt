package com.example.blinkmatch.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.blinkmatch.data.model.SymbolType
import com.example.blinkmatch.ui.components.SymbolIcon
import com.example.blinkmatch.ui.theme.BrandCyan
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onSplashFinished: () -> Unit,
    modifier: Modifier = Modifier
) {
    var symbolsVisible by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        delay(200)
        symbolsVisible = true
        delay(650)
        symbolsVisible = false
        delay(350)
        onSplashFinished()
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "BLINK MATCH",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = 3.sp,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(28.dp))

            // 3x3 Mini Grid representing the splash
            val cellSize = 60.dp
            val shape = RoundedCornerShape(10.dp)

            Column(
                verticalArrangement = Arrangement.spacedBy(6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Row 0: [   ] [ ★ ] [   ]
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SplashCell(cellSize, shape)
                    SplashCell(cellSize, shape, SymbolType.STAR, symbolsVisible)
                    SplashCell(cellSize, shape)
                }
                // Row 1: [ ● ] [   ] [ ▲ ]
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SplashCell(cellSize, shape, SymbolType.CIRCLE, symbolsVisible)
                    SplashCell(cellSize, shape)
                    SplashCell(cellSize, shape, SymbolType.TRIANGLE, symbolsVisible)
                }
                // Row 2: [   ] [ ■ ] [   ]
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    SplashCell(cellSize, shape)
                    SplashCell(cellSize, shape, SymbolType.SQUARE, symbolsVisible)
                    SplashCell(cellSize, shape)
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            Text(
                text = "See it.",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = BrandCyan
            )

            Text(
                text = "Remember it.",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SplashCell(
    size: androidx.compose.ui.unit.Dp,
    shape: RoundedCornerShape,
    symbolType: SymbolType? = null,
    visible: Boolean = false
) {
    Box(
        modifier = Modifier
            .size(size)
            .clip(shape)
            .background(MaterialTheme.colorScheme.surface)
            .border(BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)), shape),
        contentAlignment = Alignment.Center
    ) {
        if (symbolType != null) {
            AnimatedVisibility(
                visible = visible,
                enter = fadeIn(tween(140)) + scaleIn(initialScale = 0.7f, animationSpec = tween(140)),
                exit = fadeOut(tween(140))
            ) {
                SymbolIcon(symbolType = symbolType, iconSize = 32.dp)
            }
        }
    }
}
