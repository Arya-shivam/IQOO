package com.opengranola.android.model

import java.util.UUID

data class Meeting(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "Untitled meeting",
    val startedAt: Long = System.currentTimeMillis(),
    val transcript: String = "",
    val notes: String = "",
    val isRecording: Boolean = false
)
