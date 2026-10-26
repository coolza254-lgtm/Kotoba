package com.kotoba.app.audio

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class NativeAudioPlayer(private val context: Context) : TextToSpeech.OnInitListener {

    private var tts: TextToSpeech? = null
    private var isTtsReady = false

    init {
        tts = TextToSpeech(context, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts?.setLanguage(Locale.JAPANESE)
            isTtsReady = !(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
            tts?.setSpeechRate(0.92f)
        }
    }

    /**
     * Plays Japanese text using native TTS or cached native human opus
     */
    fun speak(text: String) {
        if (isTtsReady) {
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "kotoba_speech_${System.currentTimeMillis()}")
        }
    }

    fun release() {
        tts?.stop()
        tts?.shutdown()
    }
}
