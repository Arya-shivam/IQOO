package com.opengranola.android.recording

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer

/**
 * Uses the device's speech service with offline preference for live partial results.
 * The service may fall back to its installed recognizer if no offline model exists.
 */
class LiveTranscriber(
    context: Context,
    private val onTranscript: (String) -> Unit,
    private val onState: (String) -> Unit
) {
    private val recognizer = when {
        SpeechRecognizer.isOnDeviceRecognitionAvailable(context) ->
            SpeechRecognizer.createOnDeviceSpeechRecognizer(context)
        SpeechRecognizer.isRecognitionAvailable(context) ->
            SpeechRecognizer.createSpeechRecognizer(context)
        else -> null
    }
    private var running = false
    private var committedText = ""
    private var partialText = ""

    init {
        recognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) { onState("Listening") }
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() {
                if (running) recognizer.startListening(request())
            }
            override fun onError(error: Int) {
                if (running) recognizer.startListening(request()) else onState("Stopped")
            }
            override fun onResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { text ->
                        committedText = listOf(committedText, text).filter(String::isNotBlank).joinToString(" ")
                        partialText = ""
                        onTranscript(committedText)
                    }
                if (running) recognizer.startListening(request())
            }
            override fun onPartialResults(results: Bundle?) {
                results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }
                    ?.let { text ->
                        partialText = text
                        onTranscript(listOf(committedText, partialText).filter(String::isNotBlank).joinToString(" "))
                    }
            }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
        })
    }

    fun start() {
        if (recognizer == null) {
            onState("Speech recognition unavailable on this device")
            return
        }
        running = true
        committedText = ""
        partialText = ""
        recognizer.startListening(request())
    }

    fun stop() {
        running = false
        recognizer?.stopListening()
        recognizer?.cancel()
        onState("Stopped")
    }

    fun release() {
        running = false
        recognizer?.destroy()
    }

    private fun request() = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
        putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
        putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
    }
}
