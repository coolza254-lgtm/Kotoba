package com.kotoba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.model.WordEntity
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun StatsScreen(
    words: List<WordEntity>,
    doneToday: Int = 0,
    newDoneToday: Int = 0,
    streakDays: Int = 1
) {
    val totalWords = words.size
    val studiedTotal = words.count { it.state != "new" }
    val newCount = words.count { it.state == "new" }
    val learningCount = words.count { it.state != "new" && it.stability < 7.0 }
    val reviewCount = words.count { it.state != "new" && it.stability in 7.0..20.99 }
    val masteredCount = words.count { it.stability >= 21.0 }

    val n5Words = words.filter { it.lvl == "N5" }
    val n4Words = words.filter { it.lvl == "N4" }
    val n3Words = words.filter { it.lvl == "N3" }
    val n5Studied = n5Words.count { it.state != "new" }
    val n4Studied = n4Words.count { it.state != "new" }
    val n3Studied = n3Words.count { it.state != "new" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        Text(
            text = "สถิติ",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold
        )

        // Top KPI Row: New Today, Streak, Total Learned
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "คำใหม่วันนี้",
                value = "$newDoneToday คำ",
                accent = AccentBlue
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "ต่อเนื่อง",
                value = "$streakDays วัน",
                accent = AccentAmber
            )
            KpiStatCard(
                modifier = Modifier.weight(1f),
                title = "เริ่มเรียนแล้ว",
                value = "$studiedTotal/$totalWords",
                accent = AccentGreen
            )
        }

        // Memory State Distribution
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("สถานะความจำคำศัพท์", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "ตอบวันนี้ $doneToday ครั้ง",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Multi-segment progress bar
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                        .clip(RoundedCornerShape(6.dp))
                ) {
                    if (totalWords > 0) {
                        if (masteredCount > 0) Box(modifier = Modifier.weight(masteredCount.toFloat()).fillMaxHeight().background(AccentGreen))
                        if (reviewCount > 0) Box(modifier = Modifier.weight(reviewCount.toFloat()).fillMaxHeight().background(AccentBlue))
                        if (learningCount > 0) Box(modifier = Modifier.weight(learningCount.toFloat()).fillMaxHeight().background(AccentAmber))
                        if (newCount > 0) Box(modifier = Modifier.weight(newCount.toFloat()).fillMaxHeight().background(Color.Gray.copy(alpha = 0.25f)))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.25f)))
                    }
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StateLegendRow("จำขึ้นใจ (ความเสถียร ≥ 21 วัน)", "$masteredCount คำ", AccentGreen)
                    StateLegendRow("ทบทวนระยะกลาง (7–20 วัน)", "$reviewCount คำ", AccentBlue)
                    StateLegendRow("กำลังเรียนรู้ (< 7 วัน)", "$learningCount คำ", AccentAmber)
                    StateLegendRow("คำใหม่ที่ยังไม่เริ่ม", "$newCount คำ", Color.Gray)
                }
            }
        }

        // JLPT Level Progress (N5, N4, N3)
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("ความคืบหน้าตามระดับ JLPT", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                LevelProgressRow(
                    levelTitle = "JLPT N5 (พื้นฐานระดับต้น)",
                    studied = n5Studied,
                    total = n5Words.size,
                    color = AccentBlue
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                LevelProgressRow(
                    levelTitle = "JLPT N4 (ระดับกลางตอนต้น)",
                    studied = n4Studied,
                    total = n4Words.size,
                    color = AccentPurple
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                LevelProgressRow(
                    levelTitle = "JLPT N3 (ระดับกลาง)",
                    studied = n3Studied,
                    total = n3Words.size,
                    color = AccentAmber
                )
            }
        }
    }
}

@Composable
private fun KpiStatCard(
    modifier: Modifier = Modifier,
    title: String,
    value: String,
    accent: Color
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(18.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(title, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.Medium)
            Text(value, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = accent)
        }
    }
}

@Composable
private fun LevelProgressRow(
    levelTitle: String,
    studied: Int,
    total: Int,
    color: Color
) {
    val safeTotal = total.coerceAtLeast(1)
    val ratio = (studied.toFloat() / safeTotal).coerceIn(0f, 1f)

    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(levelTitle, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
            Text(
                "$studied / $total (${(ratio * 100).toInt()}%)",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        LinearProgressIndicator(
            progress = { ratio },
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(RoundedCornerShape(4.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
    }
}

@Composable
private fun StateLegendRow(label: String, count: String, color: Color) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(label, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface)
        }
        Text(count, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
    }
}
