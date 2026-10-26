package com.kotoba.app.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.model.FuriganaToken

/**
 * Baseline-Aligned Furigana Composable
 * Eliminates HTML <ruby> float/collision defects.
 * Centers furigana cleanly over corresponding kanji, while plain kana aligns along the baseline.
 */
@Composable
fun FuriganaText(
    tokens: List<FuriganaToken>,
    modifier: Modifier = Modifier,
    kanjiSize: TextUnit = 42.sp,
    furiSize: TextUnit = 14.sp,
    kanjiColor: Color = MaterialTheme.colorScheme.onSurface,
    furiColor: Color = MaterialTheme.colorScheme.secondary,
    fontWeight: FontWeight = FontWeight.Medium
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        tokens.forEach { token ->
            if (token.reading.isNullOrEmpty()) {
                // Plain kana / okurigana directly on baseline
                Text(
                    text = token.base,
                    fontSize = kanjiSize,
                    fontWeight = fontWeight,
                    color = kanjiColor,
                    lineHeight = kanjiSize
                )
            } else {
                // Kanji + Furigana cluster
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(horizontal = 1.dp)
                ) {
                    Text(
                        text = token.reading,
                        fontSize = furiSize,
                        fontWeight = FontWeight.SemiBold,
                        color = furiColor,
                        lineHeight = furiSize,
                        modifier = Modifier.padding(bottom = 3.dp)
                    )
                    Text(
                        text = token.base,
                        fontSize = kanjiSize,
                        fontWeight = fontWeight,
                        color = kanjiColor,
                        lineHeight = kanjiSize
                    )
                }
            }
        }
    }
}
