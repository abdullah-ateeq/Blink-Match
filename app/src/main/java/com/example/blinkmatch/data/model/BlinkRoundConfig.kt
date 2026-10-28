package com.example.blinkmatch.data.model

data class BlinkRoundConfig(
    val round: Int,
    val rows: Int,
    val columns: Int,
    val symbolCount: Int,
    val displayTimeMs: Long,
    val recallTimeMs: Long
)
