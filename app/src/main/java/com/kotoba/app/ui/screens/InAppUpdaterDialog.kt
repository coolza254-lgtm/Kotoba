package com.kotoba.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.kotoba.app.ui.theme.AccentGreen
import com.kotoba.app.updater.GitHubRelease
import com.kotoba.app.updater.UpdateState

@Composable
fun InAppUpdaterDialog(
    updateState: UpdateState,
    onDismiss: () -> Unit,
    onConfirmDownload: (GitHubRelease) -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().padding(16.dp),
            tonalElevation = 6.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                when (updateState) {
                    is UpdateState.Checking -> {
                        CircularProgressIndicator(modifier = Modifier.size(36.dp))
                        Text(
                            text = "กำลังตรวจสอบอัปเดตจาก GitHub...",
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    is UpdateState.UpdateAvailable -> {
                        Text("🚀", fontSize = 36.sp)
                        Text(
                            text = "พบเวอร์ชันใหม่!",
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = AccentGreen.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = updateState.release.tagName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = AccentGreen,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                            )
                        }
                        if (updateState.release.body.isNotBlank()) {
                            Text(
                                text = updateState.release.body,
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.secondary,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            OutlinedButton(
                                onClick = onDismiss,
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("ภายหลัง")
                            }
                            Button(
                                onClick = { onConfirmDownload(updateState.release) },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text("อัปเดตเลย")
                            }
                        }
                    }

                    is UpdateState.Downloading -> {
                        Text("📦", fontSize = 36.sp)
                        Text(
                            text = "กำลังดาวน์โหลดไฟล์ติดตั้ง APK...",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        LinearProgressIndicator(
                            progress = updateState.progress / 100f,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp))
                        )
                        Text(
                            text = "${updateState.progress}%",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                    }

                    is UpdateState.UpToDate -> {
                        Text("✅", fontSize = 36.sp)
                        Text(
                            text = "คุณใช้เวอร์ชันล่าสุดแล้ว",
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "เวอร์ชันปัจจุบันอัปเดตตรงกับ GitHub Release ล่าสุดเรียบร้อยแล้ว",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ตกลง")
                        }
                    }

                    is UpdateState.Error -> {
                        Text("⚠️", fontSize = 36.sp)
                        Text(
                            text = "ไม่สามารถตรวจสอบอัปเดตได้",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = updateState.message,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.secondary
                        )
                        Button(
                            onClick = onDismiss,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text("ปิด")
                        }
                    }

                    else -> {}
                }
            }
        }
    }
}
