package com.kotoba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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
    onRateWord: (WordEntity, Int) -> Unit,
    onPlayAudio: (String) -> Unit,
    onFinishSession: () -> Unit
) {
    var currentIndex by remember { mutableIntStateOf(0) }
    var isRevealed by remember { mutableStateOf(false) }

    if (words.isEmpty() || currentIndex >= words.size) {
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
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Text("🎉", fontSize = 48.sp)
                    Text("ยอดเยี่ยมมาก!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "คุณทบทวนคำศัพท์ชุดนี้ครบเรียบร้อยแล้ว",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = onFinishSession,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Text("กลับหน้าหลัก", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
        return
    }

    val currentWord = words[currentIndex]
    val fsrsEngine = remember { FsrsEngine() }
    val previewIntervals: Map<Int, String> = remember(currentWord) {
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
                IconButton(onClick = onFinishSession) {
                    Icon(Icons.Default.Close, contentDescription = "Exit Session")
                }
                Text(
                    text = "${currentIndex + 1} / ${words.size}",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary
                )
                Surface(
                    shape = RoundedCornerShape(6.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Text(
                        text = currentWord.lvl,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = AccentPurple,
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
                trackColor = Color.Gray.copy(alpha = 0.2f)
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
                        fontSize = 44.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = currentWord.pos,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                    Spacer(modifier = Modifier.height(30.dp))
                    Text(
                        text = "แตะที่การ์ดเพื่อดูเฉลย",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.6f)
                    )
                } else {
                    // Back side: Full revealed info
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(14.dp)
                    ) {
                        FuriganaText(
                            tokens = currentWord.getRubyTokens(),
                            kanjiSize = 38.sp,
                            furiSize = 13.sp
                        )

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(
                                text = currentWord.pitchPattern,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            IconButton(
                                onClick = { onPlayAudio(currentWord.kana) },
                                modifier = Modifier.size(32.dp)
                            ) {
                                Icon(
                                    Icons.Default.VolumeUp,
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
                            color = Color.Gray.copy(alpha = 0.15f)
                        )

                        // Meaning Thai & English
                        Text(
                            text = currentWord.meanTh,
                            fontSize = 20.sp,
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
                                color = Color.Gray.copy(alpha = 0.06f)
                            ) {
                                Column(
                                    modifier = Modifier.padding(12.dp),
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
                    .padding(bottom = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // 1 = Again
                Button(
                    onClick = {
                        onRateWord(currentWord, 1)
                        isRevealed = false
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentRed.copy(alpha = 0.12f), contentColor = AccentRed)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ลืม", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[1] ?: "10m", fontSize = 10.sp)
                    }
                }

                // 2 = Hard
                Button(
                    onClick = {
                        onRateWord(currentWord, 2)
                        isRevealed = false
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentAmber.copy(alpha = 0.12f), contentColor = AccentAmber)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("ยาก", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[2] ?: "1d", fontSize = 10.sp)
                    }
                }

                // 3 = Good
                Button(
                    onClick = {
                        onRateWord(currentWord, 3)
                        isRevealed = false
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentGreen.copy(alpha = 0.15f), contentColor = AccentGreen)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("จำได้", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                        Text(previewIntervals[3] ?: "3d", fontSize = 10.sp)
                    }
                }

                // 4 = Easy
                Button(
                    onClick = {
                        onRateWord(currentWord, 4)
                        isRevealed = false
                        currentIndex++
                    },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue.copy(alpha = 0.15f), contentColor = AccentBlue)
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
                    isRevealed = true
                    onPlayAudio(currentWord.kana)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("ดูเฉลย (Show Answer)", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}
