package com.opengranola.android

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.opengranola.android.data.MeetingEntity
import com.opengranola.android.data.OpenGranolaDatabase
import com.opengranola.android.model.Meeting
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MeetingViewModel(application: Application) : AndroidViewModel(application) {
    private val dao = OpenGranolaDatabase.get(application).meetingDao()
    val meetings: StateFlow<List<Meeting>> = dao.observeAll().map { rows -> rows.map { it.toMeeting() } }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun save(meeting: Meeting) = viewModelScope.launch {
        dao.save(MeetingEntity(meeting.id, meeting.title, meeting.startedAt, meeting.transcript, meeting.notes))
    }

    private fun MeetingEntity.toMeeting() = Meeting(id, title, startedAt, transcript, notes)
}
