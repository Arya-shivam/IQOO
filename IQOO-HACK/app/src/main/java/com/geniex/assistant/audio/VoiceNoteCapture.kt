package com.geniex.assistant.audio

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import java.util.Locale

class VoiceNoteCapture(private val context: Context) {
    private val mainHandler = Handler(Looper.getMainLooper())
    private var recognizer: SpeechRecognizer? = null
    private var listening = false
    private var latestPartialPhrase: String? = null
    private var onPhrase: ((String) -> Unit)? = null
    private var onStatus: ((String) -> Unit)? = null
    private var onEnded: (() -> Unit)? = null

    fun start(
        onPhrase: (String) -> Unit,
        onStatus: (String) -> Unit,
        onEnded: () -> Unit
    ): Boolean {
        stop()
        this.onPhrase = onPhrase
        this.onStatus = onStatus
        this.onEnded = onEnded

        if (!SpeechRecognizer.isRecognitionAvailable(context)) {
            onStatus("Speech recognition is not available on this device.")
            clearCallbacks()
            return false
        }

        listening = true
        latestPartialPhrase = null
        recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
            setRecognitionListener(listener)
        }
        listenAgain()
        return true
    }

    fun stop() {
        latestPartialPhrase?.takeIf { it.isNotBlank() }?.let { onPhrase?.invoke(it) }
        listening = false
        recognizer?.let {
            runCatching { it.stopListening() }
            runCatching { it.cancel() }
            runCatching { it.destroy() }
        }
        recognizer = null
        latestPartialPhrase = null
        clearCallbacks()
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun listenAgain() {
        if (!listening) return
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_MINIMUM_LENGTH_MILLIS, 60_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5_000L)
            putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, context.packageName)
        }
        runCatching {
            recognizer?.startListening(intent)
        }.onFailure {
            onStatus?.invoke("Voice capture is retrying. Keep speaking; I am reconnecting the listener.")
            restartListening(recreateRecognizer = true, delayMs = 700)
        }
    }

    private val listener = object : RecognitionListener {
        override fun onReadyForSpeech(params: Bundle?) = Unit
        override fun onBeginningOfSpeech() = Unit
        override fun onRmsChanged(rmsdB: Float) = Unit
        override fun onBufferReceived(buffer: ByteArray?) = Unit
        override fun onEndOfSpeech() {
            if (listening) {
                onStatus?.invoke("Processing that phrase. Keep going; I will continue listening.")
            }
        }
        override fun onPartialResults(partialResults: Bundle?) {
            latestPartialPhrase = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()
        }
        override fun onEvent(eventType: Int, params: Bundle?) = Unit

        override fun onError(error: Int) {
            if (!listening) return
            latestPartialPhrase?.takeIf { it.isNotBlank() }?.let { onPhrase?.invoke(it) }
            latestPartialPhrase = null

            when (error) {
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT -> {
                    onStatus?.invoke("Still listening. I will keep capturing the next thing you say.")
                    restartListening(recreateRecognizer = false, delayMs = 300)
                }
                SpeechRecognizer.ERROR_RECOGNIZER_BUSY,
                SpeechRecognizer.ERROR_CLIENT -> {
                    onStatus?.invoke("Voice capture is still on. I am refreshing the listener.")
                    restartListening(recreateRecognizer = true, delayMs = 700)
                }
                else -> {
                    onStatus?.invoke("Voice capture hit a temporary issue, but I am keeping it on.")
                    restartListening(recreateRecognizer = true, delayMs = 1_000)
                }
            }
        }

        override fun onResults(results: Bundle?) {
            val phrase = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
                ?.trim()

            if (!phrase.isNullOrBlank()) {
                onPhrase?.invoke(phrase)
            }
            latestPartialPhrase = null
            if (listening) {
                mainHandler.postDelayed({ listenAgain() }, 300)
            }
        }
    }

    private fun restartListening(recreateRecognizer: Boolean, delayMs: Long) {
        if (!listening) return
        mainHandler.postDelayed({
            if (!listening) return@postDelayed
            if (recreateRecognizer) {
                recognizer?.let {
                    runCatching { it.cancel() }
                    runCatching { it.destroy() }
                }
                recognizer = SpeechRecognizer.createSpeechRecognizer(context).apply {
                    setRecognitionListener(listener)
                }
            }
            listenAgain()
        }, delayMs)
    }

    private fun releaseRecognizer() {
        recognizer?.let {
            runCatching { it.cancel() }
            runCatching { it.destroy() }
        }
        recognizer = null
        latestPartialPhrase = null
        mainHandler.removeCallbacksAndMessages(null)
    }

    private fun clearCallbacks() {
        onPhrase = null
        onStatus = null
        onEnded = null
    }
}
