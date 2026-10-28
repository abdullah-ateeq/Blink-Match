package com.example.blinkmatch.data.model

data class PatternResult(
    val correctCount: Int,
    val incorrectCount: Int,
    val missedCount: Int,
    val perfect: Boolean
)
