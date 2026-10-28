package com.example.blinkmatch.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.blinkmatch.data.model.SymbolType
import com.example.blinkmatch.ui.theme.SymbolCircleColor
import com.example.blinkmatch.ui.theme.SymbolDiamondColor
import com.example.blinkmatch.ui.theme.SymbolHeartColor
import com.example.blinkmatch.ui.theme.SymbolPlusColor
import com.example.blinkmatch.ui.theme.SymbolSquareColor
import com.example.blinkmatch.ui.theme.SymbolStarColor
import com.example.blinkmatch.ui.theme.SymbolSunColor
import com.example.blinkmatch.ui.theme.SymbolTriangleColor
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

fun getSymbolDefaultColor(type: SymbolType): Color {
    return when (type) {
        SymbolType.STAR -> SymbolStarColor
        SymbolType.CIRCLE -> SymbolCircleColor
        SymbolType.TRIANGLE -> SymbolTriangleColor
        SymbolType.SQUARE -> SymbolSquareColor
        SymbolType.DIAMOND -> SymbolDiamondColor
        SymbolType.PLUS -> SymbolPlusColor
        SymbolType.HEART -> SymbolHeartColor
        SymbolType.SUN -> SymbolSunColor
    }
}

@Composable
fun SymbolIcon(
    symbolType: SymbolType,
    modifier: Modifier = Modifier,
    tint: Color = getSymbolDefaultColor(symbolType),
    iconSize: Dp = 36.dp
) {
    Box(
        modifier = modifier.size(iconSize),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f
            val radius = minOf(w, h) * 0.42f

            when (symbolType) {
                SymbolType.STAR -> drawStar(cx, cy, radius, tint)
                SymbolType.CIRCLE -> drawCircleSymbol(cx, cy, radius, tint)
                SymbolType.TRIANGLE -> drawTriangle(cx, cy, radius, tint)
                SymbolType.SQUARE -> drawSquareSymbol(cx, cy, radius, tint)
                SymbolType.DIAMOND -> drawDiamond(cx, cy, radius, tint)
                SymbolType.PLUS -> drawPlus(cx, cy, radius, tint)
                SymbolType.HEART -> drawHeart(cx, cy, radius, tint)
                SymbolType.SUN -> drawSun(cx, cy, radius, tint)
            }
        }
    }
}

private fun DrawScope.drawStar(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val innerRadius = radius * 0.44f
    val points = 5
    var angle = -PI / 2.0
    val step = PI / points

    for (i in 0 until points * 2) {
        val r = if (i % 2 == 0) radius else innerRadius
        val x = (cx + cos(angle) * r).toFloat()
        val y = (cy + sin(angle) * r).toFloat()
        if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
        angle += step
    }
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawCircleSymbol(cx: Float, cy: Float, radius: Float, color: Color) {
    drawCircle(color = color, radius = radius * 0.95f, center = Offset(cx, cy), style = Fill)
}

private fun DrawScope.drawTriangle(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val topY = cy - radius
    val bottomY = cy + radius * 0.85f
    val halfWidth = radius * 1.05f

    path.moveTo(cx, topY)
    path.lineTo(cx + halfWidth, bottomY)
    path.lineTo(cx - halfWidth, bottomY)
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawSquareSymbol(cx: Float, cy: Float, radius: Float, color: Color) {
    val halfSide = radius * 0.82f
    val cornerRadius = radius * 0.18f
    drawRoundRect(
        color = color,
        topLeft = Offset(cx - halfSide, cy - halfSide),
        size = Size(halfSide * 2f, halfSide * 2f),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius, cornerRadius),
        style = Fill
    )
}

private fun DrawScope.drawDiamond(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    path.moveTo(cx, cy - radius * 1.05f)
    path.lineTo(cx + radius * 0.85f, cy)
    path.lineTo(cx, cy + radius * 1.05f)
    path.lineTo(cx - radius * 0.85f, cy)
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawPlus(cx: Float, cy: Float, radius: Float, color: Color) {
    val strokeWidth = radius * 0.52f
    val armLength = radius * 0.95f
    val halfStroke = strokeWidth / 2f

    val path = Path()
    // Horizontal bar
    path.addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            Rect(cx - armLength, cy - halfStroke, cx + armLength, cy + halfStroke),
            androidx.compose.ui.geometry.CornerRadius(halfStroke * 0.4f, halfStroke * 0.4f)
        )
    )
    // Vertical bar
    path.addRoundRect(
        androidx.compose.ui.geometry.RoundRect(
            Rect(cx - halfStroke, cy - armLength, cx + halfStroke, cy + armLength),
            androidx.compose.ui.geometry.CornerRadius(halfStroke * 0.4f, halfStroke * 0.4f)
        )
    )
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawHeart(cx: Float, cy: Float, radius: Float, color: Color) {
    val path = Path()
    val width = radius * 1.9f
    val height = radius * 1.9f
    val left = cx - width / 2f
    val top = cy - height / 2f

    path.moveTo(cx, top + height * 0.3f)
    // Left curve
    path.cubicTo(
        left, top,
        left, top + height * 0.6f,
        cx, top + height * 0.95f
    )
    // Right curve
    path.cubicTo(
        left + width, top + height * 0.6f,
        left + width, top,
        cx, top + height * 0.3f
    )
    path.close()
    drawPath(path, color = color, style = Fill)
}

private fun DrawScope.drawSun(cx: Float, cy: Float, radius: Float, color: Color) {
    val centerRadius = radius * 0.48f
    drawCircle(color = color, radius = centerRadius, center = Offset(cx, cy), style = Fill)

    val numRays = 8
    val rayInner = centerRadius * 1.25f
    val rayOuter = radius * 1.05f
    val rayWidth = radius * 0.18f

    for (i in 0 until numRays) {
        val angle = i * (2.0 * PI / numRays)
        val x1 = (cx + cos(angle) * rayInner).toFloat()
        val y1 = (cy + sin(angle) * rayInner).toFloat()
        val x2 = (cx + cos(angle) * rayOuter).toFloat()
        val y2 = (cy + sin(angle) * rayOuter).toFloat()
        drawLine(
            color = color,
            start = Offset(x1, y1),
            end = Offset(x2, y2),
            strokeWidth = rayWidth,
            cap = StrokeCap.Round
        )
    }
}
