package com.kotoba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun WordsScreen(
    words: List<WordEntity>,
    onSelectWord: (Int) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedSegment by remember { mutableStateOf("all") } // all, learning, mastered

    val filteredWords = words.filter { word ->
        val matchesQuery = searchQuery.isBlank() ||
                word.kanji.contains(searchQuery, ignoreCase = true) ||
                word.kana.contains(searchQuery, ignoreCase = true) ||
                word.meanTh.contains(searchQuery, ignoreCase = true) ||
                word.meanEn.contains(searchQuery, ignoreCase = true) ||
                word.lvl.contains(searchQuery, ignoreCase = true)

        val matchesSeg = when (selectedSegment) {
            "mastered" -> word.stability >= 21.0
            "learning" -> word.state != "new" && word.stability < 21.0
            else -> true
        }

        matchesQuery && matchesSeg
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp)
    ) {
        Text("単語 · N5–N4", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text("คำศัพท์", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        // Search bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ค้นหาคันจิ คานะ หรือคำแปล...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Segment Tabs
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "ทั้งหมด", "learning" to "กำลังจำ", "mastered" to "จำขึ้นใจ").forEach { (key, label) ->
                val isSelected = selectedSegment == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedSegment = key },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(99.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Words List
        Surface(
            modifier = Modifier.fillMaxWidth().weight(1f),
            shape = RoundedCornerShape(18.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(filteredWords, key = { it.id }) { word ->
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
                                Text(word.kanji, fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
                                Text(word.kana, fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = if (word.lvl == "N4") Color(0x186941C6) else Color(0x180066CC)
                                ) {
                                    Text(
                                        word.lvl,
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = if (word.lvl == "N4") AccentPurple else AccentBlue,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                            }
                            Text(
                                word.meanTh,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.padding(top = 2.dp)
                            )
                        }

                        // Status Badge
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant
                        ) {
                            Text(
                                text = if (word.state == "new") "คำใหม่" else if (word.stability >= 21) "จำแม่น" else "ทบทวน",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.secondary,
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
