package com.opengranola.android.calendar

import android.Manifest
import android.content.ContentUris
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract.Instances
import android.provider.CalendarContract.Reminders
import androidx.core.content.ContextCompat
import java.util.Calendar

data class LocalCalendarEvent(
    val id: Long,
    val calendarName: String,
    val title: String,
    val startsAt: Long,
    val endsAt: Long,
    val location: String,
    val allDay: Boolean,
    val reminderMinutes: Int?
)

data class CalendarSnapshot(
    val events: List<LocalCalendarEvent> = emptyList(),
    val hasPermission: Boolean = false
)

/**
 * Reads Android's Calendar Provider. This includes device-local calendars and
 * Google calendars already synchronized by the user's Google Calendar app.
 * No Google credentials or calendar contents leave the phone.
 */
class CalendarRepository(private val context: Context) {
    fun hasPermission(): Boolean = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED

    fun upcoming(days: Int = 7): CalendarSnapshot {
        if (!hasPermission()) return CalendarSnapshot(hasPermission = false)
        val begin = System.currentTimeMillis()
        val end = Calendar.getInstance().apply { add(Calendar.DAY_OF_YEAR, days) }.timeInMillis
        val uri = Instances.CONTENT_URI.buildUpon().also {
            ContentUris.appendId(it, begin)
            ContentUris.appendId(it, end)
        }.build()
        val projection = arrayOf(
            Instances.EVENT_ID,
            Instances.CALENDAR_DISPLAY_NAME,
            Instances.TITLE,
            Instances.BEGIN,
            Instances.END,
            Instances.EVENT_LOCATION,
            Instances.ALL_DAY
        )
        val events = mutableListOf<LocalCalendarEvent>()
        context.contentResolver.query(uri, projection, null, null, "${Instances.BEGIN} ASC")?.use { cursor ->
            val idIndex = cursor.getColumnIndexOrThrow(Instances.EVENT_ID)
            val calendarIndex = cursor.getColumnIndexOrThrow(Instances.CALENDAR_DISPLAY_NAME)
            val titleIndex = cursor.getColumnIndexOrThrow(Instances.TITLE)
            val beginIndex = cursor.getColumnIndexOrThrow(Instances.BEGIN)
            val endIndex = cursor.getColumnIndexOrThrow(Instances.END)
            val locationIndex = cursor.getColumnIndexOrThrow(Instances.EVENT_LOCATION)
            val allDayIndex = cursor.getColumnIndexOrThrow(Instances.ALL_DAY)
            while (cursor.moveToNext() && events.size < 30) {
                val eventId = cursor.getLong(idIndex)
                val title = cursor.getString(titleIndex).orEmpty().trim()
                if (title.isBlank()) continue
                events += LocalCalendarEvent(
                    id = eventId,
                    calendarName = cursor.getString(calendarIndex).orEmpty(),
                    title = title,
                    startsAt = cursor.getLong(beginIndex),
                    endsAt = cursor.getLong(endIndex),
                    location = cursor.getString(locationIndex).orEmpty(),
                    allDay = cursor.getInt(allDayIndex) == 1,
                    reminderMinutes = reminderMinutes(eventId)
                )
            }
        }
        return CalendarSnapshot(events = events.distinctBy { "${it.id}:${it.startsAt}" }, hasPermission = true)
    }

    private fun reminderMinutes(eventId: Long): Int? {
        return context.contentResolver.query(
            Reminders.CONTENT_URI,
            arrayOf(Reminders.MINUTES),
            "${Reminders.EVENT_ID} = ?",
            arrayOf(eventId.toString()),
            "${Reminders.MINUTES} ASC"
        )?.use { cursor ->
            if (cursor.moveToFirst()) cursor.getInt(0).takeIf { it >= 0 } else null
        }
    }
}
