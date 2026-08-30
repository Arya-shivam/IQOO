package com.geniex.assistant.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

class MeetingRecorder(private val context: Context) {
    private var recorder: MediaRecorder? = null
    private var currentPath: String? = null

    fun start(): String {
        release()

        val outputDirectory = File(context.filesDir, "meetings").apply { mkdirs() }
        val outputFile = File(outputDirectory, "meeting-${System.currentTimeMillis()}.m4a")
        val mediaRecorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mediaRecorder.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioSamplingRate(44_100)
            setAudioEncodingBitRate(128_000)
            setOutputFile(outputFile.absolutePath)
            prepare()
            start()
        }

        recorder = mediaRecorder
        currentPath = outputFile.absolutePath
        return outputFile.absolutePath
    }

    fun stop(): String? {
        val path = currentPath
        recorder?.let { mediaRecorder ->
            runCatching { mediaRecorder.stop() }
            runCatching { mediaRecorder.reset() }
            runCatching { mediaRecorder.release() }
        }
        recorder = null
        currentPath = null
        return path
    }

    fun release() {
        recorder?.let { mediaRecorder ->
            runCatching { mediaRecorder.stop() }
            runCatching { mediaRecorder.reset() }
            runCatching { mediaRecorder.release() }
        }
        recorder = null
        currentPath = null
    }
}
