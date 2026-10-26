package com.kotoba.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.model.GrammarEntity
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple
import com.kotoba.app.ui.theme.AccentRed

private data class ShuffledQuizOption(
    val text: String,
    val isCorrect: Boolean
)

@Composable
fun GrammarQuizScreen(
    grammarPool: List<GrammarEntity>,
    onPlayAudio: (String) -> Unit,
    onMarkMastered: (GrammarEntity, Boolean) -> Unit,
    onOpenGrammarDetail: (String) -> Unit,
    onExitQuiz: () -> Unit
) {
    val haptic = LocalHapticFeedback.current
    var quizRound by remember { mutableIntStateOf(0) }

    val questions = remember(grammarPool, quizRound) {
        grammarPool
            .filter { !it.quizQuestion.isNullOrBlank() && it.getQuizChoices().isNotEmpty() }
            .shuffled()
            .take(10)
    }

    var currentIndex by remember(quizRound) { mutableIntStateOf(0) }
    var selectedOptionIndex by remember(currentIndex, quizRound) { mutableStateOf<Int?>(null) }
    var correctCount by remember(quizRound) { mutableIntStateOf(0) }
    val missedGrammar = remember(quizRound) { mutableStateListOf<GrammarEntity>() }
    var showExitConfirm by remember { mutableStateOf(false) }

    BackHandler {
        if (currentIndex > 0 && currentIndex < questions.size) {
            showExitConfirm = true
        } else {
            onExitQuiz()
        }
    }

    if (showExitConfirm) {
        AlertDialog(
            onDismissRequest = { showExitConfirm = false },
            title = { Text("ออกจากแบบทดสอบไวยากรณ์?", fontWeight = FontWeight.Bold) },
            text = { Text("คุณทำไปแล้ว $currentIndex จาก ${questions.size} ข้อ ต้องการออกจากแบบทดสอบตอนนี้หรือไม่?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showExitConfirm = false
                        onExitQuiz()
                    }
                ) {
                    Text("ออก", color = AccentRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = { showExitConfirm = false }) {
                    Text("ทำต่อ")
                }
            }
        )
    }

    // Quiz Completed Summary Screen
    if (questions.isEmpty() || currentIndex >= questions.size) {
        val total = questions.size.coerceAtLeast(1)
        val percentage = (correctCount * 100) / total

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(26.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Surface(
                        shape = CircleShape,
                        color = if (percentage >= 80) AccentGreen.copy(alpha = 0.14f) else AccentBlue.copy(alpha = 0.14f),
                        modifier = Modifier.size(72.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "$percentage%",
                                fontSize = 22.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (percentage >= 80) AccentGreen else AccentBlue
                            )
                        }
                    }

                    Text(
                        text = when {
                            percentage == 100 -> "สมบูรณ์แบบ! แม่นยำทุกข้อ"
                            percentage >= 80 -> "ยอดเยี่ยมมาก! เข้าใจไวยากรณ์เป็นอย่างดี"
                            percentage >= 60 -> "ทำได้ดี! ทบทวนจุดที่พลาดอีกนิด"
                            else -> "มาทบทวนโครงสร้างหลักอีกครั้งกันเถอะ"
                        },
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Text(
                        text = "ตอบถูก $correctCount จาก ${questions.size} ข้อ",
                        fontSize = 15.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        OutlinedButton(
                            onClick = onExitQuiz,
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("กลับหน้าไวยากรณ์", fontWeight = FontWeight.SemiBold)
                        }
                        Button(
                            onClick = { quizRound++ },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("ทำชุดใหม่", fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            if (missedGrammar.isNotEmpty()) {
                Text(
                    text = "หัวข้อที่ควรทบทวนเพิ่มเติม (${missedGrammar.size} หัวข้อ)",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 4.dp)
                )

                missedGrammar.forEach { item ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onOpenGrammarDetail(item.id) },
                        shape = RoundedCornerShape(16.dp),
                        color = MaterialTheme.colorScheme.surface,
                        border = CardDefaults.outlinedCardBorder()
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(
                                modifier = Modifier.weight(1f),
                                verticalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Text(item.title, fontSize = 16.sp, fontWeight = FontWeight.Bold)
                                Text(item.meanTh, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                            }
                            Icon(
                                Icons.AutoMirrored.Filled.ArrowForward,
                                contentDescription = "Review",
                                tint = MaterialTheme.colorScheme.secondary
                            )
                        }
                    }
                }
            }
        }
        return
    }

    val currentGrammar = questions[currentIndex]
    val shuffledOptions = remember(currentGrammar, quizRound) {
        val rawChoices = currentGrammar.getQuizChoices()
        rawChoices.mapIndexed { idx, text ->
            ShuffledQuizOption(
                text = text,
                isCorrect = (idx == currentGrammar.quizAnswer)
            )
        }.shuffled()
    }

    val isAnswered = selectedOptionIndex != null

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Header & Progress
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = {
                        if (currentIndex > 0) showExitConfirm = true else onExitQuiz()
                    }
                ) {
                    Icon(Icons.Default.Close, contentDescription = "Close Quiz")
                }

                Text(
                    text = "ข้อที่ ${currentIndex + 1} / ${questions.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )

                Surface(
                    shape = RoundedCornerShape(99.dp),
                    color = AccentGreen.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = "ถูก $correctCount",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentGreen,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / questions.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Scrollable Question + Choices + Deep Explanation
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Question Card
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(6.dp),
                            color = when (currentGrammar.lvl) {
                                "base" -> AccentGreen.copy(alpha = 0.14f)
                                "N5" -> AccentBlue.copy(alpha = 0.14f)
                                else -> AccentPurple.copy(alpha = 0.14f)
                            }
                        ) {
                            Text(
                                text = if (currentGrammar.lvl == "base") "พื้นฐาน" else currentGrammar.lvl,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = when (currentGrammar.lvl) {
                                    "base" -> AccentGreen
                                    "N5" -> AccentBlue
                                    else -> AccentPurple
                                },
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                            )
                        }
                        Text(
                            text = currentGrammar.groupName,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    Text(
                        text = currentGrammar.quizQuestion ?: "",
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Bold,
                        lineHeight = 28.sp,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            // Answer Choices
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                shuffledOptions.forEachIndexed { idx, option ->
                    val isSelected = selectedOptionIndex == idx

                    val containerColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.surface
                        option.isCorrect -> AccentGreen.copy(alpha = 0.14f)
                        isSelected && !option.isCorrect -> AccentRed.copy(alpha = 0.14f)
                        else -> MaterialTheme.colorScheme.surface
                    }

                    val borderColor = when {
                        !isAnswered -> MaterialTheme.colorScheme.outline
                        option.isCorrect -> AccentGreen
                        isSelected && !option.isCorrect -> AccentRed
                        else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !isAnswered) {
                                selectedOptionIndex = idx
                                if (option.isCorrect) {
                                    correctCount++
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onMarkMastered(currentGrammar, true)
                                } else {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (!missedGrammar.contains(currentGrammar)) {
                                        missedGrammar.add(currentGrammar)
                                    }
                                }
                                // Play example sentence audio on answer
                                currentGrammar.getExamples().firstOrNull()?.let { ex ->
                                    onPlayAudio(ex.jp)
                                }
                            },
                        shape = RoundedCornerShape(16.dp),
                        color = containerColor,
                        border = BorderStroke(if (isAnswered && (option.isCorrect || isSelected)) 1.8.dp else 1.dp, borderColor)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 15.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${idx + 1}.  ${option.text}",
                                fontSize = 16.sp,
                                fontWeight = if (isAnswered && (option.isCorrect || isSelected)) FontWeight.Bold else FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.weight(1f)
                            )

                            if (isAnswered) {
                                if (option.isCorrect) {
                                    Icon(
                                        Icons.Default.Check,
                                        contentDescription = "Correct",
                                        tint = AccentGreen,
                                        modifier = Modifier.size(20.dp)
                                    )
                                } else if (isSelected) {
                                    Icon(
                                        Icons.Default.Close,
                                        contentDescription = "Wrong",
                                        tint = AccentRed,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Explanation Card after answering
            AnimatedVisibility(
                visible = isAnswered,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
            ) {
                val chosenOption = selectedOptionIndex?.let { shuffledOptions.getOrNull(it) }
                val wasCorrect = chosenOption?.isCorrect == true

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
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (wasCorrect) "✓ ถูกต้องแม่นยำ!" else "✕ คำอธิบายหลักไวยากรณ์",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (wasCorrect) AccentGreen else AccentRed
                            )

                            TextButton(
                                onClick = { onOpenGrammarDetail(currentGrammar.id) },
                                contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                            ) {
                                Text("ดูบทเรียนเต็ม →", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }

                        Text(
                            text = "${currentGrammar.title} — ${currentGrammar.meanTh}",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        // First Formula rule
                        currentGrammar.getFormulas().firstOrNull()?.let { rule ->
                            Surface(
                                shape = RoundedCornerShape(10.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "โครงสร้าง (${rule.tag}): ${rule.rule}",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    modifier = Modifier.padding(10.dp)
                                )
                            }
                        }

                        Text(
                            text = currentGrammar.nuanceTh,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 20.sp
                        )

                        currentGrammar.getExamples().firstOrNull()?.let { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(
                                        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                                        RoundedCornerShape(10.dp)
                                    )
                                    .padding(horizontal = 12.dp, vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.jp, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                                    Text(ex.th, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                                }
                                IconButton(
                                    onClick = { onPlayAudio(ex.jp) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.AutoMirrored.Filled.VolumeUp,
                                        contentDescription = "Listen",
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Next Button
        if (isAnswered) {
            Button(
                onClick = { currentIndex++ },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = if (currentIndex + 1 < questions.size) "ข้อต่อไป (${currentIndex + 2}/${questions.size})" else "ดูสรุปผลคะแนน",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null)
            }
        }
    }
}
