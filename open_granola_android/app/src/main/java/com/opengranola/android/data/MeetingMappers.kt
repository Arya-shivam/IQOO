package com.opengranola.android.data

import com.opengranola.android.model.Meeting

fun MeetingEntity.toModel(): Meeting = Meeting(
    id = id,
    title = title,
    startedAt = startedAt,
    transcript = transcript,
    notes = notes,
    isRecording = false
)

fun Meeting.toEntity(): MeetingEntity = MeetingEntity(
    id = id,
    title = title,
    startedAt = startedAt,
    transcript = transcript,
    notes = notes
)
