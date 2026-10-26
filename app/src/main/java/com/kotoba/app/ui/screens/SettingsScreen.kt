package com.kotoba.app.ui.screens

import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.BrightnessAuto
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.kotoba.app.data.ThemeMode
import com.kotoba.app.updater.GitHubUpdater
import com.kotoba.app.updater.UpdateState
import java.util.Locale

@Composable
fun SettingsScreen(
    updater: GitHubUpdater,
    themeMode: ThemeMode,
    dailyNewLimit: Int,
    desiredRetention: Float,
    speechRate: Float,
    onChangeThemeMode: (ThemeMode) -> Unit,
    onChangeDailyNewLimit: (Int) -> Unit,
    onChangeDesiredRetention: (Float) -> Unit,
    onChangeSpeechRate: (Float) -> Unit,
    onTestAudio: () -> Unit,
    onReloadContent: () -> Unit = {}
) {
    val context = LocalContext.current
    val updateState by updater.updateState.collectAsState()

    // Check silently the first time Settings opens.
    LaunchedEffect(Unit) {
        if (updater.updateState.value is UpdateState.Idle) updater.checkForUpdate()
    }

    // When the user comes back from "Install unknown apps", continue the install automatically.
    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                val s = updater.updateState.value
                if (s is UpdateState.NeedsInstallPermission) updater.proceedToInstall(s.apk)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 18.dp)
            .padding(top = 16.dp, bottom = 32.dp)
    ) {
        Text("ตั้งค่า", fontSize = 28.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(18.dp))

        // ── App & Update ──────────────────────────────────────────
        UpdateHeroCard(updater = updater, state = updateState)

        // ── Appearance ───────────────────────────────────────────
        SectionLabel("การแสดงผล")
        SettingsCard {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("ธีม", fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                ThemeSegmented(selected = themeMode, onSelect = onChangeThemeMode)
            }
        }

        // ── Study ────────────────────────────────────────────────
        SectionLabel("การเรียน")
        SettingsCard {
            SliderRow(
                title = "คำศัพท์ใหม่ต่อวัน",
                valueText = "$dailyNewLimit คำ",
                value = dailyNewLimit.toFloat(),
                range = 5f..50f,
                steps = 8,
                onChange = { onChangeDailyNewLimit(it.toInt()) }
            )
            RowDivider()
            SliderRow(
                title = "เป้าหมายความจำ",
                subtitle = "สูงขึ้น = ทบทวนบ่อยขึ้น · แนะนำ 90%",
                valueText = "${(desiredRetention * 100).toInt()}%",
                value = desiredRetention,
                range = 0.80f..0.97f,
                steps = 16,
                onChange = onChangeDesiredRetention
            )
        }

        // ── Audio ────────────────────────────────────────────────
        SectionLabel("เสียง")
        SettingsCard {
            SliderRow(
                title = "ความเร็วเสียงอ่าน",
                valueText = String.format(Locale.US, "%.1f×", speechRate),
                value = speechRate,
                range = 0.7f..1.3f,
                steps = 5,
                onChange = onChangeSpeechRate
            )
            RowDivider()
            ActionRow(
                icon = Icons.AutoMirrored.Filled.VolumeUp,
                title = "ทดสอบเสียง",
                subtitle = "こんにちは。ことばのアプリへようこそ。",
                onClick = onTestAudio
            )
        }

        // ── Data ─────────────────────────────────────────────────
        SectionLabel("ข้อมูล")
        SettingsCard {
            ActionRow(
                icon = Icons.Default.Sync,
                title = "ซิงก์คลังคำศัพท์และไวยากรณ์",
                subtitle = "เพิ่มเนื้อหาใหม่ โดยไม่ลบความคืบหน้าเดิม",
                onClick = {
                    onReloadContent()
                    Toast.makeText(context, "ซิงก์ข้อมูลเรียบร้อย", Toast.LENGTH_SHORT).show()
                }
            )
        }

        // ── About ────────────────────────────────────────────────
        SectionLabel("เกี่ยวกับ")
        SettingsCard {
            InfoRow("เวอร์ชัน", updater.currentVersion)
            RowDivider()
            InfoRow("เนื้อหา", "JLPT N5 – N4")
            RowDivider()
            ActionRow(
                icon = Icons.AutoMirrored.Filled.OpenInNew,
                title = "ซอร์สโค้ดบน GitHub",
                subtitle = "coolza254-lgtm/Kotoba",
                onClick = {
                    context.startActivity(
                        Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/coolza254-lgtm/Kotoba/releases"))
                    )
                }
            )
        }

        Spacer(Modifier.height(20.dp))
        Text(
            "Kotoba · 言葉",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.7f)
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Update hero card – every updater state is handled inline (no popups)
// ═════════════════════════════════════════════════════════════════

@Composable
private fun UpdateHeroCard(updater: GitHubUpdater, state: UpdateState) {
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    val green = Color(0xFF16A34A)
    val red = Color(0xFFDC2626)

    SettingsCard {
        Column(Modifier.padding(18.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AppBadge()
                Spacer(Modifier.width(14.dp))
                Column(Modifier.weight(1f)) {
                    Text("Kotoba", fontSize = 19.sp, fontWeight = FontWeight.Bold)
                    Text(
                        "เวอร์ชัน ${updater.currentVersion}",
                        fontSize = 13.sp,
                        color = scheme.secondary
                    )
                }
                StatusChip(state = state, green = green, red = red)
            }

            AnimatedContent(
                targetState = state,
                contentKey = { it::class },
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "updateBody"
            ) { s ->
                Column(Modifier.fillMaxWidth().padding(top = 16.dp)) {
                    when (s) {
                        is UpdateState.Idle, is UpdateState.UpToDate, is UpdateState.Checking -> {
                            OutlinedButton(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    updater.checkForUpdate()
                                },
                                enabled = s !is UpdateState.Checking,
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                if (s is UpdateState.Checking) {
                                    CircularProgressIndicator(Modifier.size(16.dp), strokeWidth = 2.dp)
                                    Spacer(Modifier.width(10.dp))
                                    Text("กำลังตรวจสอบ…")
                                } else {
                                    Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                    Spacer(Modifier.width(8.dp))
                                    Text("ตรวจสอบอัปเดต", fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }

                        is UpdateState.UpdateAvailable -> {
                            var expanded by remember { mutableStateOf(false) }
                            val notes = s.release.body.orEmpty().trim()
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = green.copy(alpha = 0.10f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Column(Modifier.padding(14.dp)) {
                                    Text(
                                        "มีเวอร์ชันใหม่ ${s.release.tagName}",
                                        fontWeight = FontWeight.Bold,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        "ขนาด ${GitHubUpdater.formatBytes(s.release.apkAsset?.size ?: 0)} · ข้อมูลการเรียนจะไม่หาย",
                                        fontSize = 12.sp,
                                        color = scheme.secondary
                                    )
                                    if (notes.isNotEmpty()) {
                                        Row(
                                            Modifier
                                                .padding(top = 6.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { expanded = !expanded }
                                                .padding(vertical = 4.dp),
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text("มีอะไรใหม่", fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = green)
                                            Icon(
                                                if (expanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                                                null, tint = green, modifier = Modifier.size(18.dp)
                                            )
                                        }
                                        AnimatedVisibility(expanded, enter = expandVertically(), exit = shrinkVertically()) {
                                            Text(
                                                notes.replace("#", "").replace("**", ""),
                                                fontSize = 12.sp,
                                                lineHeight = 18.sp,
                                                color = scheme.onSurface.copy(alpha = 0.85f)
                                            )
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    updater.downloadAndInstall(s.release)
                                },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green, contentColor = Color.White)
                            ) {
                                Icon(Icons.Default.CloudDownload, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ดาวน์โหลดและติดตั้ง", fontWeight = FontWeight.Bold)
                            }
                        }

                        is UpdateState.Downloading -> {
                            val animated by animateFloatAsState(s.progress / 100f, label = "dl")
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                Text("กำลังดาวน์โหลด…", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
                                Text("${s.progress}%", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                            }
                            Spacer(Modifier.height(8.dp))
                            LinearProgressIndicator(
                                progress = { animated },
                                modifier = Modifier.fillMaxWidth().height(8.dp).clip(CircleShape),
                                strokeCap = StrokeCap.Round,
                                trackColor = scheme.outline.copy(alpha = 0.25f)
                            )
                            Spacer(Modifier.height(6.dp))
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${GitHubUpdater.formatBytes(s.downloadedBytes)} / ${GitHubUpdater.formatBytes(s.totalBytes)}",
                                    fontSize = 12.sp, color = scheme.secondary
                                )
                                TextButton(onClick = { updater.cancelDownload() }) { Text("ยกเลิก", fontSize = 13.sp) }
                            }
                        }

                        is UpdateState.NeedsInstallPermission -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = scheme.primary.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.Security, null, Modifier.size(20.dp), tint = scheme.primary)
                                    Spacer(Modifier.width(10.dp))
                                    Text(
                                        "ดาวน์โหลดเสร็จแล้ว ต้องอนุญาตให้ Kotoba ติดตั้งแอปก่อน (ทำครั้งเดียว) — เปิดสวิตช์ \"อนุญาตจากแหล่งนี้\" แล้วกดย้อนกลับ ระบบจะติดตั้งต่อให้อัตโนมัติ",
                                        fontSize = 12.sp, lineHeight = 18.sp
                                    )
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { updater.openInstallPermissionSettings() },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) { Text("เปิดการอนุญาต", fontWeight = FontWeight.Bold) }
                        }

                        is UpdateState.ReadyToInstall -> {
                            Text(
                                "กด \"อัปเดต\" ในหน้าต่างของระบบเพื่อติดตั้ง หากปิดไปแล้วกดปุ่มด้านล่างได้เลย",
                                fontSize = 12.sp, color = scheme.secondary, lineHeight = 18.sp
                            )
                            Spacer(Modifier.height(12.dp))
                            Button(
                                onClick = { updater.launchInstaller(s.apk) },
                                modifier = Modifier.fillMaxWidth().height(50.dp),
                                shape = RoundedCornerShape(14.dp),
                                colors = ButtonDefaults.buttonColors(containerColor = green, contentColor = Color.White)
                            ) { Text("ติดตั้งเลย", fontWeight = FontWeight.Bold) }
                        }

                        is UpdateState.Error -> {
                            Surface(
                                shape = RoundedCornerShape(14.dp),
                                color = red.copy(alpha = 0.08f),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(Modifier.padding(14.dp), verticalAlignment = Alignment.Top) {
                                    Icon(Icons.Default.ErrorOutline, null, Modifier.size(20.dp), tint = red)
                                    Spacer(Modifier.width(10.dp))
                                    Text(s.message, fontSize = 12.sp, lineHeight = 18.sp)
                                }
                            }
                            Spacer(Modifier.height(12.dp))
                            OutlinedButton(
                                onClick = { updater.checkForUpdate() },
                                modifier = Modifier.fillMaxWidth().height(46.dp),
                                shape = RoundedCornerShape(14.dp)
                            ) {
                                Icon(Icons.Default.Refresh, null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text("ลองอีกครั้ง", fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StatusChip(state: UpdateState, green: Color, red: Color) {
    val (text, color) = when (state) {
        is UpdateState.UpToDate -> "ล่าสุดแล้ว" to green
        is UpdateState.UpdateAvailable -> "มีอัปเดต" to green
        is UpdateState.Downloading -> "${state.progress}%" to MaterialTheme.colorScheme.primary
        is UpdateState.Error -> "ผิดพลาด" to red
        is UpdateState.Checking -> "ตรวจสอบ…" to MaterialTheme.colorScheme.secondary
        else -> return
    }
    Surface(shape = CircleShape, color = color.copy(alpha = 0.12f)) {
        Row(
            Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (state is UpdateState.UpToDate) {
                Icon(Icons.Default.CheckCircle, null, Modifier.size(13.dp), tint = color)
                Spacer(Modifier.width(4.dp))
            } else if (state is UpdateState.UpdateAvailable) {
                Box(Modifier.size(7.dp).clip(CircleShape).background(color))
                Spacer(Modifier.width(6.dp))
            }
            Text(text, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = color)
        }
    }
}

@Composable
private fun AppBadge() {
    Box(
        modifier = Modifier
            .size(52.dp)
            .clip(RoundedCornerShape(15.dp))
            .background(Color(0xFF111318)),
        contentAlignment = Alignment.Center
    ) {
        Text("言", fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color(0xFFFAF9F5))
        Box(
            Modifier
                .align(Alignment.TopEnd)
                .padding(7.dp)
                .size(7.dp)
                .clip(CircleShape)
                .background(Color(0xFFDC2626))
        )
    }
}

// ═════════════════════════════════════════════════════════════════
// Building blocks
// ═════════════════════════════════════════════════════════════════

@Composable
private fun SectionLabel(text: String) {
    Text(
        text,
        modifier = Modifier.padding(start = 6.dp, top = 22.dp, bottom = 8.dp),
        fontSize = 13.sp,
        fontWeight = FontWeight.SemiBold,
        color = MaterialTheme.colorScheme.secondary
    )
}

@Composable
private fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = CardDefaults.outlinedCardBorder()
    ) {
        Column(content = content)
    }
}

@Composable
private fun RowDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = 16.dp),
        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    )
}

@Composable
private fun ThemeSegmented(selected: ThemeMode, onSelect: (ThemeMode) -> Unit) {
    val haptic = LocalHapticFeedback.current
    val scheme = MaterialTheme.colorScheme
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(scheme.onSurface.copy(alpha = 0.06f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf(
            Triple(ThemeMode.SYSTEM, "อัตโนมัติ", Icons.Default.BrightnessAuto),
            Triple(ThemeMode.LIGHT, "สว่าง", Icons.Default.LightMode),
            Triple(ThemeMode.DARK, "มืด", Icons.Default.DarkMode)
        ).forEach { (mode, label, icon) ->
            val isSel = mode == selected
            val bg by animateColorAsState(if (isSel) scheme.surface else Color.Transparent, label = "segBg")
            val fg by animateColorAsState(if (isSel) scheme.onSurface else scheme.secondary, label = "segFg")
            Surface(
                modifier = Modifier
                    .weight(1f)
                    .height(56.dp),
                shape = RoundedCornerShape(11.dp),
                color = bg,
                shadowElevation = if (isSel) 2.dp else 0.dp,
                onClick = {
                    if (!isSel) {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onSelect(mode)
                    }
                }
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(icon, null, Modifier.size(18.dp), tint = fg)
                    Spacer(Modifier.height(3.dp))
                    Text(label, fontSize = 12.sp, color = fg,
                        fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium)
                }
            }
        }
    }
}

@Composable
private fun SliderRow(
    title: String,
    valueText: String,
    value: Float,
    range: ClosedFloatingPointRange<Float>,
    steps: Int,
    onChange: (Float) -> Unit,
    subtitle: String? = null
) {
    Column(Modifier.padding(start = 16.dp, end = 16.dp, top = 14.dp, bottom = 6.dp)) {
        Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Column(Modifier.weight(1f)) {
                Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
                }
            }
            Surface(shape = RoundedCornerShape(8.dp), color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)) {
                Text(
                    valueText,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
        Slider(value = value, onValueChange = onChange, valueRange = range, steps = steps)
    }
}

@Composable
private fun ActionRow(icon: ImageVector, title: String, subtitle: String?, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.06f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, null, Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurface)
        }
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(title, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            if (subtitle != null) {
                Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.secondary)
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 15.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 15.sp)
        Text(value, fontSize = 15.sp, color = MaterialTheme.colorScheme.secondary)
    }
}
