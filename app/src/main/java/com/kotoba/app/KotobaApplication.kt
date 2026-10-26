package com.kotoba.app

import android.app.Application
import com.kotoba.app.audio.NativeAudioPlayer
import com.kotoba.app.data.db.KotobaDatabase
import com.kotoba.app.updater.GitHubUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class KotobaApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    val database by lazy {
        KotobaDatabase.getDatabase(this, applicationScope)
    }

    val audioPlayer by lazy {
        NativeAudioPlayer(this)
    }

    val updater by lazy {
        GitHubUpdater(this)
    }

    override fun onTerminate() {
        super.onTerminate()
        audioPlayer.release()
    }
}
