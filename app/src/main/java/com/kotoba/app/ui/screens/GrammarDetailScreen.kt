package com.kotoba.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.model.GrammarEntity
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple
import com.kotoba.app.ui.theme.AccentRed

private data class LessonQuizChoice(
    val text: String,
    val isCorrect: Boolean
)

@Composable
fun GrammarDetailScreen(
    grammar: GrammarEntity,
    hasPrevious: Boolean = false,
    hasNext: Boolean = false,
    onPrevious: () -> Unit = {},
    onNext: () -> Unit = {},
    onBack: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onToggleMastered: (Boolean) -> Unit
) {
    BackHandler { onBack() }

    var quizAttempt by remember(grammar.id) { mutableIntStateOf(0) }
    var selectedQuizChoice by remember(grammar.id, quizAttempt) { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember(grammar.id, quizAttempt) { mutableStateOf(false) }

    val shuffledChoices = remember(grammar.id, quizAttempt) {
        val raw = grammar.getQuizChoices()
        raw.mapIndexed { idx, choice ->
            LessonQuizChoice(choice, idx == grammar.quizAnswer)
        }.shuffled()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top Nav
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                }
                Text("รายละเอียดไวยากรณ์", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }

            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onPrevious, enabled = hasPrevious) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Previous Lesson")
                }
                IconButton(onClick = onNext, enabled = hasNext) {
                    Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Next Lesson")
                }
            }
        }

        // Hero Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = when (grammar.lvl) {
                            "base" -> AccentGreen.copy(alpha = 0.14f)
                            "N5" -> AccentBlue.copy(alpha = 0.14f)
                            else -> AccentPurple.copy(alpha = 0.14f)
                        }
                    ) {
                        Text(
                            text = if (grammar.lvl == "base") "พื้นฐาน" else grammar.lvl,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (grammar.lvl) {
                                "base" -> AccentGreen
                                "N5" -> AccentBlue
                                else -> AccentPurple
                            },
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                        )
                    }

                    Text(
                        text = grammar.groupName,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Medium
                    )
                }

                Text(
                    text = grammar.title,
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = grammar.meanTh,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                if (grammar.meanEn.isNotBlank()) {
                    Text(
                        text = grammar.meanEn,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }

        // Formula / Setsuzoku (接続)
        val formulas = grammar.getFormulas()
        if (formulas.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "โครงสร้างการเชื่อม (接続)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    formulas.forEach { item ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(12.dp))
                                .padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(
                                text = item.tag,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentBlue
                            )
                            Text(
                                text = item.rule,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                lineHeight = 20.sp
                            )
                        }
                    }
                }
            }
        }

        // Nuance / Note (ニュアンス)
        if (grammar.nuanceTh.isNotBlank()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = AccentBlue.copy(alpha = 0.08f),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "💡 จุดสังเกตและข้อควรระวัง (Nuance)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentBlue
                    )
                    Text(
                        text = grammar.nuanceTh,
                        fontSize = 14.sp,
                        lineHeight = 22.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }

        // Example Sentences (例文)
        val examples = grammar.getExamples()
        if (examples.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "ประโยคตัวอย่าง (例文)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    examples.forEach { ex ->
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = ex.jp,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = { onPlayAudio(ex.jp) },
                                    modifier = Modifier.size(34.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Play Audio",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                            Text(
                                text = ex.th,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }

        // Mini Quiz (Cloze Practice with Shuffled Options)
        if (!grammar.quizQuestion.isNullOrBlank() && shuffledChoices.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "แบบฝึกหัดตรวจสอบความเข้าใจ",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        if (quizSubmitted) {
                            TextButton(
                                onClick = { quizAttempt++ },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(15.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("สุ่มใหม่", fontSize = 12.sp)
                            }
                        }
                    }

                    Text(
                        text = grammar.quizQuestion,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    shuffledChoices.forEachIndexed { index, option ->
                        val isSelected = selectedQuizChoice == index
                        val isCorrect = option.isCorrect

                        val btnBg = when {
                            !quizSubmitted && isSelected -> MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            quizSubmitted && isCorrect -> AccentGreen.copy(alpha = 0.15f)
                            quizSubmitted && isSelected && !isCorrect -> AccentRed.copy(alpha = 0.15f)
                            else -> Color.Transparent
                        }

                        val borderColor = when {
                            !quizSubmitted && isSelected -> MaterialTheme.colorScheme.primary
                            quizSubmitted && isCorrect -> AccentGreen
                            quizSubmitted && isSelected && !isCorrect -> AccentRed
                            else -> MaterialTheme.colorScheme.outline
                        }

                        OutlinedButton(
                            onClick = {
                                if (!quizSubmitted) {
                                    selectedQuizChoice = index
                                    quizSubmitted = true
                                    if (option.isCorrect && !grammar.isMastered) {
                                        onToggleMastered(true)
                                    }
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = btnBg),
                            border = BorderStroke(1.dp, borderColor)
                        ) {
                            Text(
                                text = "${index + 1}.  ${option.text}",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || (quizSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    if (quizSubmitted) {
                        val chosen = selectedQuizChoice?.let { shuffledChoices.getOrNull(it) }
                        val wasCorrect = chosen?.isCorrect == true
                        val rightText = shuffledChoices.firstOrNull { it.isCorrect }?.text ?: ""
                        Text(
                            text = if (wasCorrect) "✓ ถูกต้อง! บันทึกสถานะเข้าใจแล้วเรียบร้อย" else "✕ ยังไม่ถูกต้อง คำตอบที่ถูกคือ: $rightText",
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Bold,
                            color = if (wasCorrect) AccentGreen else AccentRed
                        )
                    }
                }
            }
        }

        // Mastered Toggle Button
        Button(
            onClick = { onToggleMastered(!grammar.isMastered) },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = if (grammar.isMastered) AccentGreen else MaterialTheme.colorScheme.primary
            )
        ) {
            Icon(Icons.Default.Check, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = if (grammar.isMastered) "เรียนรู้แล้ว (แตะเพื่อยกเลิก)" else "ทำเครื่องหมายว่าเข้าใจแล้ว",
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
