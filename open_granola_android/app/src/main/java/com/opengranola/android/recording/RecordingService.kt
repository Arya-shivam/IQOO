package com.opengranola.android.recording

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.media.MediaRecorder
import java.io.File
import androidx.core.app.NotificationCompat
import com.opengranola.android.R

class RecordingService : Service() {
    private var recorder: MediaRecorder? = null
    private var outputFile: File? = null
    override fun onCreate() {
        super.onCreate()
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(
            NotificationChannel(CHANNEL_ID, "Meeting recording", NotificationManager.IMPORTANCE_LOW)
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopRecording()
            stopSelf()
            return START_NOT_STICKY
        }
        startForeground(NOTIFICATION_ID, notification())
        startRecording()
        return START_NOT_STICKY
    }

    private fun startRecording() {
        if (recorder != null) return
        val directory = File(filesDir, "recordings").apply { mkdirs() }
        outputFile = File(directory, "meeting-${System.currentTimeMillis()}.m4a")
        recorder = MediaRecorder(this).apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(128_000)
            setAudioSamplingRate(44_100)
            setOutputFile(outputFile!!.absolutePath)
            prepare()
            start()
        }
    }

    private fun stopRecording() {
        recorder?.runCatching { stop() }
        recorder?.release()
        recorder = null
    }

    override fun onDestroy() {
        stopRecording()
        super.onDestroy()
    }

    private fun notification(): Notification = NotificationCompat.Builder(this, CHANNEL_ID)
        .setContentTitle("pa is recording")
        .setContentText("Recording is stored locally on this device")
        .setSmallIcon(android.R.drawable.ic_btn_speak_now)
        .setOngoing(true)
        .build()

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        const val ACTION_STOP = "com.opengranola.android.action.STOP_RECORDING"
        private const val CHANNEL_ID = "meeting_recording"
        private const val NOTIFICATION_ID = 1001
    }
}
