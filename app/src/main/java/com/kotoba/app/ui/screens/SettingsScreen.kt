package com.kotoba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kotoba.app.data.ThemeMode
import com.kotoba.app.ui.theme.AccentBlue
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.ui.theme.AccentPurple
import java.util.Locale

@Composable
fun SettingsScreen(
    currentVersion: String = "v1.1.0",
    themeMode: ThemeMode,
    dailyNewLimit: Int,
    desiredRetention: Float,
    speechRate: Float,
    onChangeThemeMode: (ThemeMode) -> Unit,
    onChangeDailyNewLimit: (Int) -> Unit,
    onChangeDesiredRetention: (Float) -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    onTestAudio: () -> Unit,
    onCheckUpdate: () -> Unit,
    onReloadContent: () -> Unit = {}
) {
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Clean Header without clutter eyebrow text
        Text("ตั้งค่า", fontSize = 28.sp, fontWeight = FontWeight.Bold)

        // Theme Mode Card (Light / Dark AMOLED / System)
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
                Text("โหมดสีหน้าจอ (Theme)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "เลือกโหมดสว่างหรือโหมดมืดดำสนิท (True Black) เพื่อถนอมสายตาและประหยัดแบตเตอรี่จอ Super AMOLED",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ThemeMode.entries.forEach { mode ->
                        val isSelected = themeMode == mode
                        OutlinedButton(
                            onClick = { onChangeThemeMode(mode) },
                            modifier = Modifier
                                .weight(1f)
                                .height(44.dp),
                            shape = RoundedCornerShape(12.dp),
                            contentPadding = PaddingValues(horizontal = 6.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                containerColor = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface,
                                contentColor = if (isSelected) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            ),
                            border = BorderStroke(
                                width = 1.dp,
                                color = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                            )
                        ) {
                            Text(
                                text = when (mode) {
                                    ThemeMode.SYSTEM -> "ตามระบบ"
                                    ThemeMode.LIGHT -> "สว่าง"
                                    ThemeMode.DARK -> "มืด"
                                },
                                fontSize = 13.sp,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }

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
                        Text("อัปเดตแอปพลิเคชัน (In-App Updater)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                        Text("เวอร์ชันปัจจุบัน: $currentVersion", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    }
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = AccentGreen.copy(alpha = 0.14f)
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("ตรวจสอบเวอร์ชันใหม่", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }

        // Daily Study & FSRS Spaced Repetition Settings
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
                Text("การทบทวนและอัลกอริทึม FSRS-5", fontSize = 15.sp, fontWeight = FontWeight.Bold)

                // Daily New Words Limit
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("จำนวนคำศัพท์ใหม่ต่อวัน", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("$dailyNewLimit คำ/วัน", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentPurple)
                }

                Slider(
                    value = dailyNewLimit.toFloat(),
                    onValueChange = { onChangeDailyNewLimit(it.toInt()) },
                    valueRange = 5f..50f,
                    steps = 8
                )

                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f))

                // Desired Retention
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("เป้าหมายอัตราการจดจำ (Retention)", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("${(desiredRetention * 100).toInt()}%", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = AccentBlue)
                }

                Slider(
                    value = desiredRetention,
                    onValueChange = { onChangeDesiredRetention(it) },
                    valueRange = 0.80f..0.97f,
                    steps = 16
                )

                Text(
                    "ค่ามาตรฐาน 90% ให้สมดุลระหว่างจำนวนการทบทวนต่อวันและอัตราการจำระยะยาว",
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
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("เสียงอ่านภาษาญี่ปุ่น (TTS)", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                    TextButton(
                        onClick = onTestAudio,
                        contentPadding = PaddingValues(horizontal = 8.dp, vertical = 0.dp)
                    ) {
                        Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("ทดสอบเสียง", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text("ความเร็วเสียงอ่าน", fontSize = 13.sp, color = MaterialTheme.colorScheme.secondary)
                    Text("${String.format(Locale.US, "%.2f", speechRate)}x", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }

                Slider(
                    value = speechRate,
                    onValueChange = { onChangeSpeechRate(it) },
                    valueRange = 0.7f..1.3f,
                    steps = 5
                )
            }
        }

        // Content Library Management
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
                Text("คลังข้อมูลคำศัพท์และไวยากรณ์", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                Text(
                    "ซิงก์ข้อมูลคำศัพท์และบทเรียนไวยากรณ์ล่าสุดโดยไม่ลบประวัติความจำที่เรียนไปแล้ว",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.secondary,
                    lineHeight = 18.sp
                )
                OutlinedButton(
                    onClick = {
                        onReloadContent()
                        Toast.makeText(context, "ซิงก์คลังคำศัพท์และไวยากรณ์เรียบร้อยแล้ว", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Text("ซิงก์คลังข้อมูล (Sync Content)", fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
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
                    Text("Samsung Galaxy A54 5G · 120Hz Super AMOLED", fontSize = 11.sp, color = AccentPurple)
                }
                Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = null, tint = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}
