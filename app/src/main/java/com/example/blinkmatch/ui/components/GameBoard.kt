package com.example.blinkmatch.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.blinkmatch.data.model.GameState
import com.example.blinkmatch.data.model.MemoryPosition
import com.example.blinkmatch.data.model.MemorySymbol

@Composable
fun GameBoard(
    rows: Int,
    columns: Int,
    gameState: GameState,
    symbols: List<MemorySymbol>,
    correctPositions: Set<MemoryPosition>,
    selectedPositions: Set<MemoryPosition>,
    reducedMotion: Boolean,
    onCellClick: (MemoryPosition) -> Unit,
    modifier: Modifier = Modifier
) {
    // Map position to symbol for O(1) lookup
    val symbolMap = symbols.associateBy { it.position }

    BoxWithConstraints(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        val availableWidth = maxWidth
        val availableHeight = maxHeight

        // Dynamic spacing: 8dp for 3x3/4x4, 6dp for 5x5, 5dp for 6x6
        val spacing = when {
            rows >= 6 -> 5.dp
            rows >= 5 -> 6.dp
            else -> 8.dp
        }

        // Limit maximum dimension so grid is a neat square fitting within bounds
        val maxBoardDimension = minOf(availableWidth, availableHeight)
        val maxCols = maxOf(rows, columns)
        val totalSpacing = spacing * (maxCols - 1)
        val cellSize = ((maxBoardDimension - totalSpacing) / maxCols).coerceIn(44.dp, 84.dp)
        val actualBoardSize = (cellSize * maxCols) + totalSpacing

        Column(
            modifier = Modifier.size(actualBoardSize),
            verticalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            for (r in 0 until rows) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(spacing, Alignment.CenterHorizontally),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    for (c in 0 until columns) {
                        val position = MemoryPosition(r, c)
                        val symbol = symbolMap[position]
                        val isSelected = selectedPositions.contains(position)
                        val isCorrect = correctPositions.contains(position)

                        GridCellView(
                            row = r,
                            column = c,
                            gameState = gameState,
                            symbol = symbol,
                            isSelected = isSelected,
                            isCorrectPosition = isCorrect,
                            reducedMotion = reducedMotion,
                            onCellClick = { onCellClick(position) },
                            modifier = Modifier.size(cellSize)
                        )
                    }
                }
            }
        }
    }
}
