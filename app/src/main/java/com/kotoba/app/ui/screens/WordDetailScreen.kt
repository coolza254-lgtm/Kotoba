package com.kotoba.app.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.VolumeUp
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
import com.kotoba.app.ui.components.FuriganaText
import com.kotoba.app.ui.components.PitchAccentView
import com.kotoba.app.ui.theme.AccentAmber
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun WordDetailScreen(
    word: WordEntity,
    onBack: () -> Unit,
    onPlayAudio: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp)
    ) {
        // Top Nav Bar
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
            }
            Text("คำศัพท์", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Hero Card: Furigana & Meaning
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Column {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = if (word.lvl == "N4") Color(0x186941C6) else Color(0x180066CC)
                            ) {
                                Text(
                                    word.lvl,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (word.lvl == "N4") AccentPurple else AccentBlue,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                            Surface(
                                shape = RoundedCornerShape(99.dp),
                                color = MaterialTheme.colorScheme.surfaceVariant
                            ) {
                                Text(
                                    word.pos,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 2.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // Custom Baseline-Aligned Furigana Text (No floating/collision!)
                        FuriganaText(
                            tokens = word.getRubyTokens(),
                            kanjiSize = 44.sp,
                            furiSize = 14.sp
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = word.meanTh,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = word.meanEn,
                            fontSize = 15.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    // Speaker Button
                    IconButton(
                        onClick = { onPlayAudio(word.kana) },
                        modifier = Modifier
                            .size(46.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = "Play Audio")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Pitch Accent Melody Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("ระดับเสียง (Pitch Accent)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    Text("${word.kana} · ${word.pitchPattern}", fontSize = 15.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 2.dp))
                }

                PitchAccentView(
                    morae = word.getMorae(),
                    dropMora = word.dropMora
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // Example Sentence Card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("ประโยคตัวอย่าง", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary, fontWeight = FontWeight.SemiBold)
                    IconButton(
                        onClick = { onPlayAudio(word.exampleJp) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(Icons.Default.VolumeUp, contentDescription = null, modifier = Modifier.size(18.dp))
                    }
                }
                Text(word.exampleJp, fontSize = 17.sp, fontWeight = FontWeight.Medium, modifier = Modifier.padding(top = 4.dp))
                Text(word.exampleTh, fontSize = 14.sp, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(top = 2.dp))
            }
        }

        // Notes / Warning if present
        if (!word.note.isNullOrEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = Color(0x18B54708),
                border = CardDefaults.outlinedCardBorder()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text("💡 ข้อสังเกต / คำเตือน", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = AccentAmber)
                    Text(word.note, fontSize = 13.sp, color = MaterialTheme.colorScheme.onSurface, modifier = Modifier.padding(top = 2.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // FSRS Memory Inspector
        Text("สถานะความจำในระบบ FSRS-5", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary, modifier = Modifier.padding(horizontal = 4.dp, vertical = 6.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("โอกาสจำได้", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(if (word.state == "new") "—" else "${(word.stability * 10).toInt().coerceAtMost(99)}%", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ความเสถียร (S)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(if (word.state == "new") "—" else "${word.stability.toInt()} วัน", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("ความยาก (D)", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    Text(if (word.state == "new") "—" else String.format("%.1f/10", word.difficulty), fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}
