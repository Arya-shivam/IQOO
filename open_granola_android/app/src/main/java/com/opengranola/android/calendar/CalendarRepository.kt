package com.opengranola.android.calendar

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.provider.CalendarContract
import androidx.core.content.ContextCompat
import org.json.JSONObject

object CalendarRepository {
    fun isEnabled(context: Context): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.READ_CALENDAR
    ) == PackageManager.PERMISSION_GRANTED

    fun readUpcoming(context: Context): List<JSONObject> {
        if (!isEnabled(context)) return emptyList()
        val start = System.currentTimeMillis()
        val end = start + 14 * 24 * 60 * 60 * 1_000L
        val uri = CalendarContract.Instances.CONTENT_URI.buildUpon()
            .appendPath(start.toString()).appendPath(end.toString()).build()
        val fields = arrayOf(
            CalendarContract.Instances.EVENT_ID,
            CalendarContract.Instances.TITLE,
            CalendarContract.Instances.BEGIN,
            CalendarContract.Instances.END,
            CalendarContract.Instances.EVENT_LOCATION,
            CalendarContract.Instances.DESCRIPTION
        )
        val result = mutableListOf<JSONObject>()
        context.contentResolver.query(uri, fields, null, null, "${CalendarContract.Instances.BEGIN} ASC")?.use { cursor ->
            val eventId = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_ID)
            val title = cursor.getColumnIndexOrThrow(CalendarContract.Instances.TITLE)
            val begin = cursor.getColumnIndexOrThrow(CalendarContract.Instances.BEGIN)
            val finish = cursor.getColumnIndexOrThrow(CalendarContract.Instances.END)
            val location = cursor.getColumnIndexOrThrow(CalendarContract.Instances.EVENT_LOCATION)
            val description = cursor.getColumnIndexOrThrow(CalendarContract.Instances.DESCRIPTION)
            while (cursor.moveToNext()) {
                val id = cursor.getLong(eventId)
                val starts = cursor.getLong(begin)
                result += JSONObject()
                    .put("id", "calendar:$id:$starts")
                    .put("source", "Calendar")
                    .put("title", cursor.getString(title).orEmpty())
                    .put("detail", listOf(cursor.getString(location), cursor.getString(description)).filterNot { it.isNullOrBlank() }.joinToString("\n"))
                    .put("timestamp", starts)
                    .put("end", cursor.getLong(finish))
            }
        }
        return result
    }
}
