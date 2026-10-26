package com.kotoba.app.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Close
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
import com.kotoba.app.data.model.WordEntity
import com.kotoba.app.fsrs.FsrsEngine
import com.kotoba.app.ui.components.FuriganaText
import com.kotoba.app.ui.components.PitchAccentView
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple
import com.kotoba.app.ui.theme.AccentRed

@Composable
fun FlashcardSessionScreen(
    words: List<WordEntity>,
    initialIndex: Int = 0,
    desiredRetention: Double = 0.90,
    onRateWord: (WordEntity, Int, Int) -> Unit,
    onPlayAudio: (String) -> Unit,
    onFinishSession: (Boolean) -> Unit // true = completed all, false = paused mid-session
) {
    val haptic = LocalHapticFeedback.current
    var currentIndex by remember { mutableIntStateOf(initialIndex.coerceIn(0, words.size.coerceAtLeast(1))) }
    var isRevealed by remember { mutableStateOf(false) }
    var showPauseDialog by remember { mutableStateOf(false) }

    val isSessionComplete = words.isEmpty() || currentIndex >= words.size

    BackHandler {
        if (!isSessionComplete) {
            showPauseDialog = true
        } else {
            onFinishSession(true)
        }
    }

    if (showPauseDialog) {
        AlertDialog(
            onDismissRequest = { showPauseDialog = false },
            title = { Text("พักการท่องศัพท์ชั่วคราว?", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    "บันทึกความคืบหน้าไว้ที่ใบที่ ${currentIndex + 1} จาก ${words.size} เรียบร้อยแล้ว คุณสามารถกลับมาเรียนต่อจากจุดเดิมได้ทุกเมื่อ"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showPauseDialog = false
                        onFinishSession(false)
                    }
                ) {
                    Text("บันทึกและออก", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                Button(onClick = { showPauseDialog = false }) {
                    Text("เรียนต่อ")
                }
            }
        )
    }

    if (isSessionComplete) {
        // Completion Card
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = MaterialTheme.colorScheme.surface,
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(
                    modifier = Modifier.padding(28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Text("ทบทวนครบชุดเรียบร้อย!", fontSize = 22.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "คุณเรียนคำศัพท์ครบทั้ง ${words.size} ใบในรอบนี้แล้ว ระบบ FSRS-5 ได้คำนวณวันทบทวนถัดไปให้เรียบร้อย",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onFinishSession(true) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("กลับหน้าหลัก", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
        return
    }

    val currentWord = words[currentIndex]
    val fsrsEngine = remember(desiredRetention) { FsrsEngine(desiredRetention = desiredRetention) }
    val previewIntervals: Map<Int, String> = remember(currentWord, fsrsEngine) {
        fsrsEngine.previewIntervals(currentWord)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(18.dp),
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // Top Bar: Progress and Close
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { showPauseDialog = true }) {
                    Icon(Icons.Default.Close, contentDescription = "Pause Session")
                }
                Text(
                    text = "ใบที่ ${currentIndex + 1} / ${words.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = if (currentWord.lvl == "N4") AccentPurple.copy(alpha = 0.14f) else AccentBlue.copy(alpha = 0.14f)
                ) {
                    Text(
                        text = currentWord.lvl,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (currentWord.lvl == "N4") AccentPurple else AccentBlue,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }
            }

            LinearProgressIndicator(
                progress = (currentIndex + 1).toFloat() / words.size,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = MaterialTheme.colorScheme.primary,
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }

        // Center: Interactive Flashcard
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(vertical = 14.dp)
                .clickable {
                    if (!isRevealed) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isRevealed = true
                        onPlayAudio(currentWord.kana)
                    }
                },
            shape = RoundedCornerShape(26.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (!isRevealed) {
                    // Front side: Word prompt
                    Text(
                        text = currentWord.kanji.ifBlank { currentWord.kana },
                        fontSize = 46.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        shape = RoundedCornerShape(99.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant
                    ) {
                        Text(
                            text = currentWord.pos,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(32.dp))
                    Text(
                        text = "แตะที่การ์ดเพื่อดูคำอ่านและคำแปล",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.65f)
                    )
                } else {
                    // Back side: Full revealed info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        FuriganaText(
                            tokens = currentWord.getRubyTokens(),
                            kanjiSize = 40.sp,
                            furiSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "${currentWord.kana} · ${currentWord.pitchPattern}",
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = { onPlayAudio(currentWord.kana) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Audio",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        // Pitch accent waveform
                        PitchAccentView(
                            morae = currentWord.getMorae(),
                            dropMora = currentWord.dropMora
                        )

                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 4.dp),
                            color = MaterialTheme.colorScheme.outline
                        )

                        // Meaning Thai & English
                        Text(
                            text = currentWord.meanTh,
                            fontSize = 21.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        if (currentWord.meanEn.isNotBlank()) {
                            Text(
                                text = currentWord.meanEn,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                        }

                        // Example Sentence
                        if (currentWord.exampleJp.isNotBlank()) {
                            Surface(
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(14.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                            ) {
                                Row(
                                    modifier = Modifier.padding(12.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = currentWord.exampleJp,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.SemiBold
                                        )
                                        Text(
                                            text = currentWord.exampleTh,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    IconButton(
                                        onClick = { onPlayAudio(currentWord.exampleJp) },
                                        modifier = Modifier.size(30.dp)
                                    ) {
                                        Icon(
                                            Icons.AutoMirrored.Filled.VolumeUp,
                                            contentDescription = "Play Sentence",
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // Bottom Rating Bar (FSRS: Again, Hard, Good, Easy)
        if (isRevealed) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val submitRating: (Int) -> Unit = { rating ->
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    val nextIdx = currentIndex + 1
                    onRateWord(currentWord, rating, nextIdx)
                    isRevealed = false
                    currentIndex = nextIdx
                }

                // 1 = Again
                Button(
                    onClick = { submitRating(1) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentRed.copy(alpha = 0.14f),
                        contentColor = AccentRed
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ลืม", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[1] ?: "10m", fontSize = 10.sp)
                    }
                }

                // 2 = Hard
                Button(
                    onClick = { submitRating(2) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentAmber.copy(alpha = 0.14f),
                        contentColor = AccentAmber
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ยาก", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[2] ?: "1d", fontSize = 10.sp)
                    }
                }

                // 3 = Good
                Button(
                    onClick = { submitRating(3) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentGreen.copy(alpha = 0.15f),
                        contentColor = AccentGreen
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("จำได้", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[3] ?: "3d", fontSize = 10.sp)
                    }
                }

                // 4 = Easy
                Button(
                    onClick = { submitRating(4) },
                    modifier = Modifier
                        .weight(1f)
                        .height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 4.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AccentBlue.copy(alpha = 0.15f),
                        contentColor = AccentBlue
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ง่าย", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[4] ?: "7d", fontSize = 10.sp)
                    }
                }
            }
        } else {
            Button(
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                    isRevealed = true
                    onPlayAudio(currentWord.kana)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ดูเฉลย (Show Answer)", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}
