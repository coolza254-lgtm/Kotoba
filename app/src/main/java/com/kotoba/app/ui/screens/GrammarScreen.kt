package com.kotoba.app.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Search
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

@Composable
fun GrammarScreen(
    grammarList: List<GrammarEntity>,
    onSelectGrammar: (String) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedLevel by remember { mutableStateOf("all") } // all, base, N5, N4

    val filteredList = grammarList.filter { item ->
        val matchesQuery = searchQuery.isBlank() ||
                item.title.contains(searchQuery, ignoreCase = true) ||
                item.meanTh.contains(searchQuery, ignoreCase = true) ||
                item.meanEn.contains(searchQuery, ignoreCase = true) ||
                item.groupName.contains(searchQuery, ignoreCase = true)

        val matchesLevel = when (selectedLevel) {
            "base" -> item.lvl == "base"
            "N5" -> item.lvl == "N5"
            "N4" -> item.lvl == "N4"
            else -> true
        }

        matchesQuery && matchesLevel
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 18.dp)
            .padding(top = 18.dp)
    ) {
        Text("文法 · N5 & N4", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text("ไวยากรณ์ญี่ปุ่น", fontSize = 28.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 12.dp))

        // Search Bar
        OutlinedTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            placeholder = { Text("ค้นหาไวยากรณ์ โครงสร้าง หรือความหมาย...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            shape = RoundedCornerShape(14.dp),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Filter chips: All, Base, N5, N4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("all" to "ทั้งหมด", "base" to "พื้นฐาน", "N5" to "JLPT N5", "N4" to "JLPT N4").forEach { (key, label) ->
                val isSelected = selectedLevel == key
                FilterChip(
                    selected = isSelected,
                    onClick = { selectedLevel = key },
                    label = { Text(label, fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal) },
                    shape = RoundedCornerShape(99.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // List
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(bottom = 80.dp)
        ) {
            items(filteredList, key = { it.id }) { grammar ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onSelectGrammar(grammar.id) },
                    shape = RoundedCornerShape(16.dp),
                    color = MaterialTheme.colorScheme.surface,
                    border = CardDefaults.outlinedCardBorder()
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
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

                            if (grammar.isMastered) {
                                Icon(
                                    Icons.Default.CheckCircle,
                                    contentDescription = "Mastered",
                                    tint = AccentGreen,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }

                        Text(
                            text = grammar.title,
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        Text(
                            text = grammar.meanTh,
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.secondary,
                            lineHeight = 20.sp
                        )
                    }
                }
            }
        }
    }
}
