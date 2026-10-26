package com.kotoba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.model.GrammarEntity
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple
import com.kotoba.app.ui.theme.AccentRed

@Composable
fun GrammarDetailScreen(
    grammar: GrammarEntity,
    onBack: () -> Unit,
    onPlayAudio: (String) -> Unit,
    onToggleMastered: (Boolean) -> Unit
) {
    var selectedQuizChoice by remember { mutableStateOf<Int?>(null) }
    var quizSubmitted by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Top Nav
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("ไวยากรณ์", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
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
                            "base" -> Color(0x140F9D58)
                            "N5" -> Color(0x141A73E8)
                            else -> Color(0x146941C6)
                        }
                    ) {
                        Text(
                            text = when (grammar.lvl) {
                                "base" -> "พื้นฐาน"
                                else -> grammar.lvl
                            },
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = when (grammar.lvl) {
                                "base" -> AccentGreen
                                "N5" -> Color(0xFF1A73E8)
                                else -> AccentPurple,
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
                    fontSize = 28.sp,
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
                        text = "โครงสร้างไวยากรณ์ (接続 · Setsuzoku)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    formulas.forEach { item ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .background(Color.Gray.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                                .padding(horizontal = 12.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                            ) {
                                Text(
                                    text = item.tag,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                )
                            }
                            Text(
                                text = item.rule,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
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
                color = Color(0x0C1A73E8),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = "จุดสังเกตและข้อควรระวัง (Nuance)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF1A73E8)
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
                                .background(Color.Gray.copy(alpha = 0.05f), RoundedCornerShape(12.dp))
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
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        Icons.Default.VolumeUp,
                                        contentDescription = "Play Audio",
                                        tint = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.size(18.dp)
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

        // Mini Quiz (Cloze Practice)
        val choices = grammar.getQuizChoices()
        if (!grammar.quizQuestion.isNullOrBlank() && choices.isNotEmpty()) {
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
                        text = "แบบฝึกหัดทบทวน (Mini Quiz)",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )

                    Text(
                        text = grammar.quizQuestion,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    choices.forEachIndexed { index, choice ->
                        val isSelected = selectedQuizChoice == index
                        val isCorrect = index == grammar.quizAnswer

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
                            else -> Color.Gray.copy(alpha = 0.3f)
                        }

                        OutlinedButton(
                            onClick = {
                                if (!quizSubmitted) {
                                    selectedQuizChoice = index
                                    quizSubmitted = true
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(containerColor = btnBg),
                            border = androidx.compose.foundation.BorderStroke(1.dp, borderColor)
                        ) {
                            Text(
                                text = "${index + 1}. $choice",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || (quizSubmitted && isCorrect)) FontWeight.Bold else FontWeight.Normal,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }

                    if (quizSubmitted) {
                        val wasCorrect = selectedQuizChoice == grammar.quizAnswer
                        Text(
                            text = if (wasCorrect) "ถูกต้อง! 🎉" else "ยังไม่ถูกต้อง คำตอบคือตัวเลือกที่ ${grammar.quizAnswer + 1}",
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

        Spacer(modifier = Modifier.height(40.dp))
    }
}
