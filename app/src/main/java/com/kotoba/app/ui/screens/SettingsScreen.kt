package com.kotoba.app.ui.screens

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple

@Composable
fun SettingsScreen(
    currentVersion: String = "v1.0.0",
    onCheckUpdate: () -> Unit
) {
    val context = LocalContext.current
    var targetRetention by remember { mutableFloatStateOf(0.90f) }
    var speechRate by remember { mutableFloatStateOf(1.0f) }
    var showRomaji by remember { mutableStateOf(true) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text("設定 · Settings", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.secondary)
        Text("การตั้งค่า", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // In-App GitHub Releases Updater Card
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
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("การอัปเดตแอปพลิเคชัน (In-App Updater)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("เวอร์ชันปัจจุบัน: $currentVersion", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGreen.copy(alpha = 0.12f)
                    ) {
                        Text(
                            "GitHub OTA",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = AccentGreen,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Text(
                    "ตรวจสอบไฟล์ติดตั้ง APK และอัปเดตเวอร์ชันใหม่ล่าสุดโดยตรงจาก GitHub Releases",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )

                Button(
                    onClick = onCheckUpdate,
                    modifier = Modifier.fillMaxWidth().height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ตรวจสอบเวอร์ชันใหม่", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // FSRS Spaced Repetition Settings
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
                Text("อัลกอริทึม FSRS-5 (Spaced Repetition)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("เป้าหมายการจดจำ (Desired Retention)", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("${(targetRetention * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }

                Slider(
                    value = targetRetention,
                    onValueChange = { targetRetention = it },
                    valueRange = 0.80f..0.97f,
                    steps = 16
                )

                Text(
                    "ค่ามาตรฐาน 90% ให้สมดุลระหว่างจำนวนการทบทวนต่อวันและอัตราการจดจำที่แม่นยำที่สุด",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary
                )
            }
        }

        // Audio Settings
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
                Text("เสียงอ่านภาษาญี่ปุ่น (Speech Engine)", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ความเร็วเสียงอ่าน (Speech Rate)", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("${String.format("%.1f", speechRate)}x", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = speechRate,
                    onValueChange = { speechRate = it },
                    valueRange = 0.7f..1.3f,
                    steps = 5
                )
            }
        }

        // Display Preferences
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text("การแสดงผล", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("แสดงตัวอ่านโรมาจิ (Romaji)", fontSize = 14.sp, fontWeight = FontWeight.Medium)
                        Text("ช่วยในการออกเสียงสำหรับผู้เริ่มต้น", fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Switch(
                        checked = showRomaji,
                        onCheckedChange = { showRomaji = it }
                    )
                }
            }
        }

        // About & Repository
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .clickable {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/coolza254-lgtm/Kotoba"))
                    context.startActivity(intent)
                },
            shape = RoundedCornerShape(22.dp),
            color = MaterialTheme.colorScheme.surface,
            border = CardDefaults.outlinedCardBorder()
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text("GitHub Repository", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    Text("coolza254-lgtm/Kotoba", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("Optimized for Samsung Galaxy A54 5G (Exynos 1380, 120Hz)", fontSize = 11.sp, color = AccentPurple)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
        }

        Spacer(modifier = Modifier.height(70.dp))
    }
}
