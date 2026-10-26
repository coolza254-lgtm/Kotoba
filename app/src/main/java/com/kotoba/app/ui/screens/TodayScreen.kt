package com.kotoba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.SavedSessionState
import com.kotoba.app.data.ThemeMode
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun TodayScreen(
    dueCount: Int,
    newCount: Int,
    streakDays: Int,
    doneToday: Int,
    totalVocabCount: Int,
    totalGrammarCount: Int,
    masteredGrammarCount: Int,
    savedSession: SavedSessionState,
    themeMode: ThemeMode,
    onToggleTheme: () -> Unit,
    onStartSession: () -> Unit,
    onResumeSession: () -> Unit,
    onNavigateGrammar: () -> Unit,
    onStartGrammarQuiz: () -> Unit
) {
    val totalQueue = if (savedSession.isActive) savedSession.remainingCount else (dueCount + newCount)
    val ringColor = MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Clean Header: App Title, Streak Pill, and Quick Theme Switcher
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Kotoba",
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(AccentAmber)
                            )
                            Text(
                                text = "$streakDays วันต่อเนื่อง",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Quick Theme Mode Button
                    Surface(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .clickable { onToggleTheme() },
                        shape = CircleShape,
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(
                                imageVector = when (themeMode) {
                                    ThemeMode.LIGHT -> Icons.Default.LightMode
                                    ThemeMode.DARK -> Icons.Default.DarkMode
                                    ThemeMode.SYSTEM -> Icons.Default.BrightnessAuto
                                },
                                contentDescription = "Toggle Theme",
                                modifier = Modifier.size(18.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            // Hero Card: Ring & Daily Target Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Box(
                        modifier = Modifier.size(112.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                            val strokeWidth = 10.dp.toPx()
                            drawCircle(
                                color = trackColor,
                                style = Stroke(strokeWidth)
                            )
                            val progressFraction = if (doneToday + totalQueue > 0) {
                                (doneToday.toFloat() / (doneToday + totalQueue)).coerceIn(0.05f, 1f)
                            } else {
                                1f
                            }
                            drawArc(
                                color = ringColor,
                                startAngle = -90f,
                                sweepAngle = progressFraction * 360f,
                                useCenter = false,
                                style = Stroke(strokeWidth, cap = StrokeCap.Round)
                            )
                        }
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "$totalQueue",
                                fontSize = 32.sp,
                                fontWeight = FontWeight.Bold,
                                lineHeight = 32.sp
                            )
                            Text(
                                text = "ใบที่เหลือ",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }

                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            text = "เป้าหมายการทบทวนวันนี้",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("เรียนเสร็จวันนี้", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("$doneToday ใบ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("รอทบทวน", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("$dueCount ใบ", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("คำศัพท์ใหม่", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            Text("+$newCount ใบ", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                        }
                    }
                }
            }

            // Saved Session Banner if user paused mid-session
            if (savedSession.isActive) {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onResumeSession() },
                    shape = RoundedCornerShape(18.dp),
                    color = AccentBlue.copy(alpha = 0.1f),
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "มีรอบการท่องศัพท์ที่ค้างไว้",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                            Text(
                                text = "ทำไปแล้ว ${savedSession.currentIndex} จาก ${savedSession.wordIds.size} ใบ · แตะเพื่อเรียนต่อทันที",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = "Resume",
                            tint = AccentBlue
                        )
                    }
                }
            }

            // Quick Shortcut 1: Grammar Curriculum
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateGrammar() },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("บทเรียนไวยากรณ์ (พื้นฐาน – N4)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "โครงสร้างการผันรูป คำช่วย และจุดสังเกตความต่าง",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentPurple.copy(alpha = 0.14f)
                    ) {
                        Text(
                            "$masteredGrammarCount/$totalGrammarCount บท",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentPurple,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp)
                        )
                    }
                }
            }

            // Quick Shortcut 2: Grammar Comprehension Quiz
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onStartGrammarQuiz() },
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = AccentGreen.copy(alpha = 0.14f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.Default.Quiz,
                                    contentDescription = null,
                                    tint = AccentGreen,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text("แบบทดสอบความเข้าใจไวยากรณ์", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "ฝึกทำโจทย์เติมคำพร้อมคำอธิบายเหตุผลรายข้อ",
                                fontSize = 12.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                    Icon(
                        Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Bottom Thumb-Zone CTA
        Button(
            onClick = {
                if (savedSession.isActive) onResumeSession() else onStartSession()
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
        ) {
            val buttonLabel = if (savedSession.isActive) {
                "เรียนต่อจากที่ค้างไว้ (${savedSession.currentIndex + 1}/${savedSession.wordIds.size})"
            } else if (totalQueue > 0) {
                "เริ่มท่องศัพท์วันนี้ · $totalQueue ใบ"
            } else {
                "ทบทวนคำศัพท์เพิ่มเติม · $totalVocabCount คำ"
            }
            Text(buttonLabel, fontSize = 16.sp, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
        }
    }
}
