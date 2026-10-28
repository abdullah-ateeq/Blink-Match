package com.example.blinkmatch.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.blinkmatch.ui.theme.BrandAmber
import com.example.blinkmatch.ui.theme.BrandCyan

@Composable
fun CountdownOverlay(
    countdownText: String,
    reducedMotion: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.55f)),
        contentAlignment = Alignment.Center
    ) {
        AnimatedContent(
            targetState = countdownText,
            transitionSpec = {
                if (reducedMotion) {
                    fadeIn(tween(100)) togetherWith fadeOut(tween(100))
                } else {
                    (fadeIn(tween(160)) + scaleIn(initialScale = 0.6f, animationSpec = tween(160))) togetherWith
                            (fadeOut(tween(120)) + scaleOut(targetScale = 1.3f, animationSpec = tween(120)))
                }
            },
            label = "countdown"
        ) { text ->
            val isBlink = text == "BLINK!"
            Text(
                text = text,
                color = if (isBlink) BrandCyan else BrandAmber,
                fontSize = if (isBlink) 54.sp else 76.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = if (isBlink) 2.sp else 0.sp
            )
        }
    }
}
