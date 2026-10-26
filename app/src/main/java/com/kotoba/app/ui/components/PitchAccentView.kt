package com.kotoba.app.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.ui.theme.AccentRed

/**
 * High-Legibility Tokyo Pitch Accent Diagram
 */
@Composable
fun PitchAccentView(
    morae: List<String>,
    dropMora: Int, // 0 = Heiban, 1 = Atamadaka, 2+ = Nakadaka/Odaka
    modifier: Modifier = Modifier
) {
    val items = morae + "が"
    val isHigh = items.mapIndexed { i, _ ->
        when (dropMora) {
            0 -> i > 0 // 0 [平板]: Low -> High -> High...
            1 -> i == 0 // 1 [頭高]: High -> Low -> Low...
            else -> i > 0 && i < dropMora // 2+ [中高/尾高]: Low -> High until drop
        }
    }

    val stepDp = 28.dp
    val totalWidth = (items.size - 1) * 28 + 24

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val lineColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
        val defaultDotColor = MaterialTheme.colorScheme.onSurface
        val particleDotColor = MaterialTheme.colorScheme.secondary.copy(alpha = 0.4f)

        Canvas(
            modifier = Modifier
                .width(totalWidth.dp)
                .height(30.dp)
        ) {
            val padX = 12.dp.toPx()
            val step = 28.dp.toPx()
            val yHigh = 8.dp.toPx()
            val yLow = 22.dp.toPx()

            // Draw connecting lines
            for (i in 0 until items.size - 1) {
                val x1 = padX + i * step
                val y1 = if (isHigh[i]) yHigh else yLow
                val x2 = padX + (i + 1) * step
                val y2 = if (isHigh[i + 1]) yHigh else yLow

                drawLine(
                    color = lineColor,
                    start = Offset(x1, y1),
                    end = Offset(x2, y2),
                    strokeWidth = 2.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            // Draw pitch dots
            items.forEachIndexed { i, _ ->
                val x = padX + i * step
                val y = if (isHigh[i]) yHigh else yLow
                val isParticle = i == items.size - 1
                val isDrop = dropMora > 0 && i == dropMora - 1

                val dotColor = when {
                    isDrop -> AccentRed
                    isParticle -> particleDotColor
                    else -> defaultDotColor
                }

                drawCircle(
                    color = dotColor,
                    radius = if (isDrop) 5.dp.toPx() else 4.dp.toPx(),
                    center = Offset(x, y)
                )
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // Mora labels
        Row(
            modifier = Modifier.width(totalWidth.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            items.forEachIndexed { i, mora ->
                val isDrop = dropMora > 0 && i == dropMora - 1
                val isParticle = i == items.size - 1

                Text(
                    text = mora,
                    fontSize = 12.sp,
                    fontWeight = if (isDrop) FontWeight.Bold else FontWeight.Medium,
                    color = if (isParticle) MaterialTheme.colorScheme.secondary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}
