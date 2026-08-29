package com.opengranola.android.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "meetings")
data class MeetingEntity(
    @PrimaryKey val id: String,
    val title: String,
    val startedAt: Long,
    val transcript: String = "",
    val notes: String = "",
    val recordingPath: String? = null
)
