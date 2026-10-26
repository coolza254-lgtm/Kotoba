package com.kotoba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
fun WordsScreen(
    words: List<WordEntity>,
    onSelectWord: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf("all") } // all, N5, N4, learning, mastered

    val n5Count = remember(words) { words.count { it.lvl == "N5" } }
    val n4Count = remember(words) { words.count { it.lvl == "N4" } }
    val n3Count = remember(words) { words.count { it.lvl == "N3" } }
    val learningCount = remember(words) { words.count { it.state != "new" && it.stability < 21.0 } }
    val masteredCount = remember(words) { words.count { it.stability >= 21.0 } }

    val filteredWords = remember(words, searchQuery, selectedSegment) {
        words.filter { word ->
            val matchesQuery = searchQuery.isBlank() ||
                    word.kanji.contains(searchQuery, ignoreCase = true) ||
                    word.kana.contains(searchQuery, ignoreCase = true) ||
                    word.meanTh.contains(searchQuery, ignoreCase = true) ||
                    word.meanEn.contains(searchQuery, ignoreCase = true) ||
                    word.lvl.contains(searchQuery, ignoreCase = true)

            val matchesSeg = when (selectedSegment) {
                "N5" -> word.lvl == "N5"
                "N4" -> word.lvl == "N4"
                "N3" -> word.lvl == "N3"
                "mastered" -> word.stability >= 21.0
                "learning" -> word.state != "new" && word.stability < 21.0
                else -> true
            }

            matchesQuery && matchesSeg
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp)
    ) {
        // Clean Header Row without eyebrow clutter
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "คำศัพท์",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold
            )

            Surface(
                shape = RoundedCornerShape(99.dp),
                color = MaterialTheme.colorScheme.surfaceVariant
            ) {
                Text(
                    text = "${filteredWords.size} คำ",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ค้นหาคันจิ คานะ หรือคำแปลไทย/อังกฤษ...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segment Tabs
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                "all" to "ทั้งหมด (${words.size})",
                "N5" to "N5 ($n5Count)",
                "N4" to "N4 ($n4Count)",
                "N3" to "N3 ($n3Count)",
                "learning" to "กำลังจำ ($learningCount)",
                "mastered" to "จำแม่น ($masteredCount)"
            ).forEach { (key, label) ->
                val isSelected = selectedSegment == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSegment = key },
                    label = {
                        Text(
                            text = label,
                            fontSize = 12.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    shape = RoundedCornerShape(99.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Words List
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            if (filteredWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "ไม่พบคำศัพท์ในหมวดหมู่นี้",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(vertical = 4.dp)
                ) {
                    items(
                        items = filteredWords,
                        key = { it.id },
                        contentType = { "word_row" }
                    ) { word ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelectWord(word.id) }
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = word.kanji.ifBlank { word.kana },
                                        fontSize = 20.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    if (word.kanji.isNotBlank()) {
                                        Text(
                                            text = word.kana,
                                            fontSize = 13.sp,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                    }
                                    val lvlColor = when (word.lvl) {
                                        "N3" -> AccentAmber
                                        "N4" -> AccentPurple
                                        else -> AccentBlue
                                    }
                                    Surface(
                                        shape = RoundedCornerShape(6.dp),
                                        color = lvlColor.copy(alpha = 0.14f)
                                    ) {
                                        Text(
                                            text = word.lvl,
                                            fontSize = 11.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = lvlColor,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                }
                                Text(
                                    text = word.meanTh,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.secondary,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            }

                            // Status Badge
                            val statusText = when {
                                word.state == "new" -> "คำใหม่"
                                word.stability >= 21.0 -> "จำแม่น"
                                else -> "กำลังจำ"
                            }
                            val statusColor = when {
                                word.state == "new" -> MaterialTheme.colorScheme.secondary
                                word.stability >= 21.0 -> AccentGreen
                                else -> AccentAmber
                            }
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (word.state == "new") MaterialTheme.colorScheme.surfaceVariant else statusColor.copy(alpha = 0.12f)
                            ) {
                                Text(
                                    text = statusText,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = statusColor,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                        HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))
                    }
                }
            }
        }
    }
}
