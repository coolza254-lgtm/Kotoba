package com.kotoba.app.updater

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Build
import android.os.Environment
import androidx.core.content.FileProvider
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File

data class GitHubRelease(
    val tag_name: String,
    val name: String,
    val body: String,
    val assets: List<GitHubAsset>
) {
    val tagName: String get() = tag_name
    fun getApkDownloadUrl(): String? = assets.firstOrNull { it.name.endsWith(".apk") }?.browser_download_url
}

data class GitHubAsset(
    val name: String,
    val size: Long,
    val browser_download_url: String
)

sealed class UpdateState {
    data object Idle : UpdateState()
    data object Checking : UpdateState()
    data class UpdateAvailable(val release: GitHubRelease) : UpdateState()
    data class Downloading(val progress: Int) : UpdateState()
    data object UpToDate : UpdateState()
    data class Error(val message: String) : UpdateState()
}

class GitHubUpdater(private val context: Context) {

    private val client = OkHttpClient()
    private val gson = Gson()
    private val repoOwner = "coolza254-lgtm"
    private val repoName = "Kotoba"
    private val currentVersion = "v1.0.1"

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.Idle)
    val updateState: StateFlow<UpdateState> = _updateState.asStateFlow()

    fun resetState() {
        _updateState.value = UpdateState.Idle
    }

    fun checkForUpdate(scope: CoroutineScope = CoroutineScope(Dispatchers.IO)) {
        _updateState.value = UpdateState.Checking
        scope.launch {
            try {
                val release = fetchLatestRelease()
                if (release != null) {
                    if (release.tagName != currentVersion && release.getApkDownloadUrl() != null) {
                        _updateState.value = UpdateState.UpdateAvailable(release)
                    } else {
                        _updateState.value = UpdateState.UpToDate
                    }
                } else {
                    _updateState.value = UpdateState.Error("ไม่สามารถเชื่อมต่อ GitHub Releases ได้")
                }
            } catch (e: Exception) {
                _updateState.value = UpdateState.Error(e.message ?: "เกิดข้อผิดพลาดในการตรวจสอบ")
            }
        }
    }

    private suspend fun fetchLatestRelease(): GitHubRelease? = withContext(Dispatchers.IO) {
        try {
            val url = "https://api.github.com/repos/$repoOwner/$repoName/releases/latest"
            val request = Request.Builder()
                .url(url)
                .header("Accept", "application/vnd.github.v3+json")
                .build()

            val response = client.newCall(request).execute()
            if (response.isSuccessful) {
                val json = response.body?.string() ?: return@withContext null
                gson.fromJson(json, GitHubRelease::class.java)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * Downloads APK from GitHub Releases and triggers Android Package Installer
     */
    fun downloadAndInstallApk(apkUrl: String, fileName: String = "Kotoba-Update.apk") {
        try {
            _updateState.value = UpdateState.Downloading(10)
            val uri = Uri.parse(apkUrl)
            val destinationFile = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            if (destinationFile.exists()) destinationFile.delete()

            val request = DownloadManager.Request(uri)
                .setTitle("ดาวน์โหลดตัวอัปเดต Kotoba")
                .setDescription("กำลังดาวน์โหลดเวอร์ชันล่าสุดจาก GitHub Releases...")
                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                .setDestinationUri(Uri.fromFile(destinationFile))
                .setMimeType("application/vnd.android.package-archive")

            val downloadManager = context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager
            val downloadId = downloadManager.enqueue(request)

            val onComplete = object : BroadcastReceiver() {
                override fun onReceive(ctxt: Context, intent: Intent) {
                    val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                    if (id == downloadId) {
                        context.unregisterReceiver(this)
                        _updateState.value = UpdateState.Idle
                        installApk(destinationFile)
                    }
                }
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE),
                    Context.RECEIVER_EXPORTED
                )
            } else {
                context.registerReceiver(
                    onComplete,
                    IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
                )
            }
        } catch (e: Exception) {
            _updateState.value = UpdateState.Error(e.message ?: "ไม่สามารถเริ่มการดาวน์โหลดได้")
            e.printStackTrace()
        }
    }

    private fun installApk(file: File) {
        val apkUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val installIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(apkUri, "application/vnd.android.package-archive")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(installIntent)
    }
}
