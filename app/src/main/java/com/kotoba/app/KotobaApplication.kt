package com.kotoba.app

import android.app.Application
import com.kotoba.app.audio.NativeAudioPlayer
import com.kotoba.app.data.AppPreferences
import com.kotoba.app.data.db.KotobaDatabase
import com.kotoba.app.updater.GitHubUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class KotobaApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val preferences by lazy {
        AppPreferences(this)
    }

    val database by lazy {
        KotobaDatabase.getDatabase(this, applicationScope)
    }

    val audioPlayer by lazy {
        NativeAudioPlayer(this).apply {
            setSpeechRate(preferences.speechRate.value)
        }
    }

    val updater by lazy {
        GitHubUpdater(this)
    }

    override fun onCreate() {
        super.onCreate()
        preferences.refreshDailyState()
        database.checkAndSeedData(this, applicationScope)
    }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}
