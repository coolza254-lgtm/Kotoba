package com.kotoba.app.updater

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import com.google.gson.Gson
import com.kotoba.app.BuildConfig
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

data class GitHubRelease(
    val tag_name: String,
    val name: String?,
    val body: String?,
    val assets: List<GitHubAsset>
) {
    val tagName: String get() = tag_name
    val apkAsset: GitHubAsset? get() = assets.firstOrNull { it.name.endsWith(".apk") }
    fun getApkDownloadUrl(): String? = apkAsset?.browser_download_url
}

data class GitHubAsset(
    val name: String,
    val size: Long,
    val browser_download_url: String
)

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpToDate(val latestTag: String) : UpdateState()
    data class UpdateAvailable(val release: GitHubRelease) : UpdateState()
    data class Downloading(val progress: Int, val downloadedBytes: Long, val totalBytes: Long) : UpdateState()
    /** APK downloaded; waiting for the user to allow "Install unknown apps" for Kotoba. */
    data class NeedsInstallPermission(val apk: File) : UpdateState()
    data class ReadyToInstall(val apk: File) : UpdateState()
    data class Error(val message: String) : UpdateState()
}

/**
 * In-app updater backed by GitHub Releases.
 *
 * Flow: check latest release -> semantic version compare -> download APK with OkHttp (real progress)
 * into the app cache -> verify signature matches installed app -> request install permission if
 * needed -> hand off to the Android package installer through FileProvider.
 */
class GitHubUpdater(private val context: Context) {

    private val client = OkHttpClient.Builder()
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()
    private val gson = Gson()
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var downloadJob: Job? = null

    private val repoOwner = "coolza254-lgtm"
    private val repoName = "Kotoba"

    /** Single source of truth: versionName from build.gradle.kts (e.g. "1.2.0"). */
    val currentVersion: String = "v" + BuildConfig.VERSION_NAME

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    private val updatesDir: File get() = File(context.cacheDir, "updates").apply { mkdirs() }

    fun resetState() {
        if (_updateState.value !is UpdateState.Downloading) {
            _updateState.value = UpdateState.Idle
        }
    }

    fun checkForUpdate() {
        val s = _updateState.value
        if (s is UpdateState.Checking || s is UpdateState.Downloading) return
        _updateState.value = UpdateState.Checking
        scope.launch {
            _updateState.value = try {
                val request = Request.Builder()
                    .url("https://api.github.com/repos/$repoOwner/$repoName/releases/latest")
                    .header("Accept", "application/vnd.github+json")
                    .header("User-Agent", "Kotoba-Android")
                    .build()
                client.newCall(request).execute().use { response ->
                    when {
                        response.code == 403 || response.code == 429 ->
                            UpdateState.Error("GitHub จำกัดจำนวนการตรวจสอบชั่วคราว ลองใหม่อีกครั้งในภายหลัง")
                        !response.isSuccessful ->
                            UpdateState.Error("เชื่อมต่อ GitHub ไม่สำเร็จ (HTTP ${response.code})")
                        else -> {
                            val release = gson.fromJson(response.body!!.string(), GitHubRelease::class.java)
                            if (release.apkAsset != null && isNewer(release.tagName, currentVersion)) {
                                UpdateState.UpdateAvailable(release)
                            } else {
                                UpdateState.UpToDate(release.tagName)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                UpdateState.Error("ไม่มีการเชื่อมต่ออินเทอร์เน็ต หรือเซิร์ฟเวอร์ไม่ตอบสนอง")
            }
        }
    }

    fun downloadAndInstall(release: GitHubRelease) {
        val asset = release.apkAsset ?: run {
            _updateState.value = UpdateState.Error("ไม่พบไฟล์ APK ในเวอร์ชันนี้")
            return
        }
        if (_updateState.value is UpdateState.Downloading) return

        downloadJob = scope.launch {
            val target = File(updatesDir, "Kotoba-${release.tagName}.apk")
            val partial = File(updatesDir, target.name + ".part")
            try {
                updatesDir.listFiles()?.forEach { it.delete() }
                _updateState.value = UpdateState.Downloading(0, 0, asset.size)

                val request = Request.Builder()
                    .url(asset.browser_download_url)
                    .header("User-Agent", "Kotoba-Android")
                    .build()

                client.newCall(request).execute().use { response ->
                    if (!response.isSuccessful) error("ดาวน์โหลดไม่สำเร็จ (HTTP ${response.code})")
                    val body = response.body ?: error("ไฟล์ว่างเปล่า")
                    val total = body.contentLength().takeIf { it > 0 } ?: asset.size
                    body.byteStream().use { input ->
                        partial.outputStream().use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var downloaded = 0L
                            var lastProgress = -1
                            while (isActive) {
                                val read = input.read(buffer)
                                if (read == -1) break
                                output.write(buffer, 0, read)
                                downloaded += read
                                val progress = ((downloaded * 100) / total).toInt().coerceIn(0, 100)
                                if (progress != lastProgress) {
                                    lastProgress = progress
                                    _updateState.value = UpdateState.Downloading(progress, downloaded, total)
                                }
                            }
                        }
                    }
                }
                if (!isActive) {
                    partial.delete()
                    return@launch
                }
                if (!partial.renameTo(target)) error("บันทึกไฟล์ไม่สำเร็จ")

                // Guard: Android silently refuses updates signed with a different key.
                if (!signatureMatchesInstalled(target)) {
                    target.delete()
                    _updateState.value = UpdateState.Error(
                        "ลายเซ็นของไฟล์ใหม่ไม่ตรงกับแอปที่ติดตั้งอยู่ กรุณาถอนการติดตั้ง Kotoba เวอร์ชันเก่าหนึ่งครั้ง แล้วติดตั้งเวอร์ชันใหม่จาก GitHub"
                    )
                    return@launch
                }

                proceedToInstall(target)
            } catch (e: Exception) {
                partial.delete()
                _updateState.value = UpdateState.Error(e.message ?: "ดาวน์โหลดไม่สำเร็จ")
            }
        }
    }

    fun cancelDownload() {
        downloadJob?.cancel()
        downloadJob = null
        _updateState.value = UpdateState.Idle
    }

    /** Called again after the user returns from the "Install unknown apps" settings page. */
    fun proceedToInstall(apk: File) {
        if (!apk.exists()) {
            _updateState.value = UpdateState.Error("ไม่พบไฟล์ที่ดาวน์โหลด กรุณาลองใหม่")
            return
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O &&
            !context.packageManager.canRequestPackageInstalls()
        ) {
            _updateState.value = UpdateState.NeedsInstallPermission(apk)
            return
        }
        _updateState.value = UpdateState.ReadyToInstall(apk)
        launchInstaller(apk)
    }

    fun openInstallPermissionSettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
            Uri.parse("package:${context.packageName}")
        ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    fun launchInstaller(apk: File) {
        try {
            val apkUri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", apk)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error("เปิดตัวติดตั้งไม่สำเร็จ: ${e.message}")
        }
    }

    // ---- helpers ----

    private fun signatureMatchesInstalled(apk: File): Boolean {
        return try {
            val pm = context.packageManager
            val installed = signingDigests(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNING_CERTIFICATES)
                else
                    @Suppress("DEPRECATION") pm.getPackageInfo(context.packageName, PackageManager.GET_SIGNATURES)
            )
            val archive = signingDigests(
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P)
                    pm.getPackageArchiveInfo(apk.absolutePath, PackageManager.GET_SIGNING_CERTIFICATES)
                else
                    @Suppress("DEPRECATION") pm.getPackageArchiveInfo(apk.absolutePath, PackageManager.GET_SIGNATURES)
            )
            // If we can't read the archive, let the system installer decide.
            archive.isEmpty() || installed.isEmpty() || installed.intersect(archive).isNotEmpty()
        } catch (e: Exception) {
            true
        }
    }

    private fun signingDigests(info: PackageInfo?): Set<String> {
        if (info == null) return emptySet()
        val sigs = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info.signingInfo?.let {
                if (it.hasMultipleSigners()) it.apkContentsSigners else it.signingCertificateHistory
            }
        } else {
            @Suppress("DEPRECATION") info.signatures
        } ?: return emptySet()
        val md = MessageDigest.getInstance("SHA-256")
        return sigs.map { sig -> md.digest(sig.toByteArray()).joinToString("") { "%02x".format(it) } }.toSet()
    }

    companion object {
        /** Semantic compare: "v1.10.0" > "v1.9.2". Non-numeric parts are ignored. */
        fun isNewer(remote: String, current: String): Boolean {
            fun parse(v: String) = v.trim().removePrefix("v").removePrefix("V")
                .split('.', '-').mapNotNull { it.toIntOrNull() }
            val r = parse(remote)
            val c = parse(current)
            for (i in 0 until maxOf(r.size, c.size)) {
                val a = r.getOrElse(i) { 0 }
                val b = c.getOrElse(i) { 0 }
                if (a != b) return a > b
            }
            return false
        }

        fun formatBytes(bytes: Long): String = when {
            bytes >= 1024 * 1024 -> String.format(java.util.Locale.US, "%.1f MB", bytes / 1048576.0)
            bytes >= 1024 -> String.format(java.util.Locale.US, "%.0f KB", bytes / 1024.0)
            else -> "$bytes B"
        }
    }
}
