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
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
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
import com.kotoba.app.data.model.WordEntity
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun TodayScreen(
    words: List<WordEntity>,
    dueCount: Int,
    newCount: Int,
    newDoneToday: Int,
    dailyNewLimit: Int,
    streakDays: Int,
    doneToday: Int,
    learnedCount: Int,
    nextDueHint: String?,
    selectedLevelFilter: String,
    savedSession: SavedSessionState,
    themeMode: ThemeMode,
    onSelectLevelFilter: (String) -> Unit,
    onToggleTheme: () -> Unit,
    onStartSession: () -> Unit,
    onStartExtraReview: () -> Unit,
    onResumeSession: () -> Unit,
    onNavigateWords: () -> Unit
) {
    val totalQueue = if (savedSession.isActive) savedSession.remainingCount else (dueCount + newCount)
    val isDailyGoalDone = totalQueue == 0
    val ringColor = if (isDailyGoalDone) Color(0xFF16A34A) else MaterialTheme.colorScheme.primary
    val trackColor = MaterialTheme.colorScheme.outline

    val n5Total = words.count { it.lvl == "N5" }
    val n5Learned = words.count { it.lvl == "N5" && it.state != "new" }
    val n4Total = words.count { it.lvl == "N4" }
    val n4Learned = words.count { it.lvl == "N4" && it.state != "new" }
    val n3Total = words.count { it.lvl == "N3" }
    val n3Learned = words.count { it.lvl == "N3" && it.state != "new" }

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
            // Header: App Title, Streak Pill, Quick Theme Switcher
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

            // Hero Card: Ring & Daily Quota Status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Row(
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
                                val progressFraction = if (isDailyGoalDone) {
                                    1f
                                } else if (doneToday + totalQueue > 0) {
                                    (doneToday.toFloat() / (doneToday + totalQueue)).coerceIn(0.06f, 0.96f)
                                } else {
                                    0.06f
                                }
                                drawArc(
                                    color = ringColor,
                                    startAngle = -90f,
                                    sweepAngle = progressFraction * 360f,
                                    useCenter = false,
                                    style = Stroke(strokeWidth, cap = StrokeCap.Round)
                                )
                            }
                            if (isDailyGoalDone) {
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = null,
                                        tint = Color(0xFF16A34A),
                                        modifier = Modifier.size(30.dp)
                                    )
                                    Text(
                                        text = "ครบแล้ว",
                                        fontSize = 12.sp,
                                        color = Color(0xFF16A34A),
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            } else {
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
                        }

                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "เป้าหมายวันนี้",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("คำใหม่วันนี้", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                val quotaReached = newDoneToday >= dailyNewLimit
                                Text(
                                    text = if (quotaReached) "ครบ $dailyNewLimit/$dailyNewLimit คำ ✓" else "$newDoneToday / $dailyNewLimit คำ",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (quotaReached) Color(0xFF16A34A) else AccentBlue
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("รอทบทวนตอนนี้", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Text(
                                    "$dueCount ใบ",
                                    fontSize = 14.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (dueCount > 0) AccentAmber else MaterialTheme.colorScheme.onSurface
                                )
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text("ตอบไปแล้ววันนี้", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Text("$doneToday ครั้ง", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentGreen)
                            }
                        }
                    }

                    if (isDailyGoalDone && nextDueHint != null) {
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f))
                        Text(
                            text = nextDueHint,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
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

            // JLPT Level Selector for New Words (N5 / N4 / N3)
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("ระดับคำศัพท์ใหม่", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        Text(
                            "การทบทวนจะรวมทุกระดับเสมอ",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        listOf(
                            "ALL" to "ทั้งหมด",
                            "N5" to "N5",
                            "N4" to "N4",
                            "N3" to "N3"
                        ).forEach { (key, label) ->
                            val selected = selectedLevelFilter == key
                            FilterChip(
                                selected = selected,
                                onClick = { onSelectLevelFilter(key) },
                                modifier = Modifier.weight(1f),
                                label = {
                                    Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
                                        Text(
                                            text = label,
                                            fontSize = 12.sp,
                                            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium
                                        )
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            )
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        LevelMiniStat("JLPT N5", "$n5Learned/$n5Total", AccentBlue)
                        LevelMiniStat("JLPT N4", "$n4Learned/$n4Total", AccentPurple)
                        LevelMiniStat("JLPT N3", "$n3Learned/$n3Total", AccentAmber)
                    }
                }
            }

            // Vocabulary Dictionary Shortcut
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onNavigateWords() },
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
                            color = AccentBlue.copy(alpha = 0.12f),
                            modifier = Modifier.size(40.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    Icons.AutoMirrored.Filled.MenuBook,
                                    contentDescription = null,
                                    tint = AccentBlue,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                        Column {
                            Text("คลังคำศัพท์ JLPT N5 – N3", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                            Text(
                                "เริ่มเรียนแล้ว $learnedCount จาก ${words.size} คำ · พร้อมเสียงอ่านและ Pitch Accent",
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
        if (savedSession.isActive) {
            Button(
                onClick = onResumeSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(
                    "เรียนต่อจากที่ค้างไว้ (${savedSession.currentIndex + 1}/${savedSession.wordIds.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        } else if (totalQueue > 0) {
            val label = when {
                newCount > 0 && dueCount > 0 -> "เริ่มเรียนและทบทวน · $totalQueue ใบ"
                newCount > 0 -> "เริ่มเรียนคำศัพท์ใหม่ · $newCount ใบ"
                else -> "ทบทวนคำศัพท์ที่ถึงเวลา · $dueCount ใบ"
            }
            Button(
                onClick = onStartSession,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Text(label, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        } else if (learnedCount > 0) {
            OutlinedButton(
                onClick = onStartExtraReview,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(18.dp)
            ) {
                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "ทบทวนคำที่เรียนไปแล้วซ้ำ ($learnedCount คำ)",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun LevelMiniStat(label: String, value: String, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(5.dp)) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(CircleShape)
                .background(color)
        )
        Text(label, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
        Text(value, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = color)
    }
}
