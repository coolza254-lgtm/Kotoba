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
    words: List<WordEntity>
) {
    val totalWords = words.size
    val newCount = words.count { it.state == "new" }
    val learningCount = words.count { it.state == "learning" || it.state == "relearning" }
    val reviewCount = words.count { it.state == "review" && it.stability < 21.0 }
    val masteredCount = words.count { it.stability >= 21.0 }

    val n5Count = words.count { it.lvl == "N5" }
    val n4Count = words.count { it.lvl == "N4" }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("統計 · Analytics", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text("สถิติการเรียนรู้", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Hero Card: Retention & Stability
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("อัตราการจดจำเป้าหมาย (Retention)", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("91.2%", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                    Text("ตามอัลกอริทึม FSRS-5 ล่าสุด", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
                Surface(
                    shape = CircleShape,
                    color = AccentGreen.copy(alpha = 0.12f),
                    modifier = Modifier.size(54.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Text("🎯", fontSize = 24.sp)
                    }
                }
            }
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
                Text("สถานะความจำคำศัพท์ (Memory States)", fontSize = 14.sp, fontWeight = FontWeight.Bold)

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
                        if (newCount > 0) Box(modifier = Modifier.weight(newCount.toFloat()).fillMaxHeight().background(Color.Gray.copy(alpha = 0.3f)))
                    } else {
                        Box(modifier = Modifier.fillMaxSize().background(Color.Gray.copy(alpha = 0.3f)))
                    }
                }

                // Legend
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    StateLegendRow("จำขึ้นใจ (Mastered / S ≥ 21d)", "$masteredCount คำ", AccentGreen)
                    StateLegendRow("ทบทวนระยะยาว (Review)", "$reviewCount คำ", AccentBlue)
                    StateLegendRow("กำลังเรียนรู้ (Learning)", "$learningCount คำ", AccentAmber)
                    StateLegendRow("ยังไม่เริ่ม (New)", "$newCount คำ", Color.Gray)
                }
            }
        }

        // JLPT Level breakdown
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
                Text("คลังคำศัพท์ตามระดับ JLPT", fontSize = 14.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Gray.copy(alpha = 0.06f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("JLPT N5", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentBlue)
                            Text("$n5Count คำ", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("พื้นฐานระดับต้น", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }

                    Surface(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(16.dp),
                        color = Color.Gray.copy(alpha = 0.06f)
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text("JLPT N4", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = AccentPurple)
                            Text("$n4Count คำ", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                            Text("ระดับกลางตอนต้น", fontSize = 11.sp, color = MaterialTheme.colorScheme.secondary)
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
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
