package com.kotoba.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class NativeAudioPlayer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false
    private var currentSpeechRate = 0.92f

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            isTtsReady = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            tts?.setSpeechRate(currentSpeechRate)
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeechRate = rate.coerceIn(0.6f, 1.5f)
        if (isTtsReady) {
            tts?.setSpeechRate(currentSpeechRate)
        }
    }

    /**
     * Plays Japanese text using native TTS
     */
    fun speak(text: String) {
        if (text.isBlank()) return
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kotoba_speech_${System.currentTimeMillis()}")
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
