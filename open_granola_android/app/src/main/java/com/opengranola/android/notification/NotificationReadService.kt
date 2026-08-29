package com.opengranola.android.notification

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import org.json.JSONArray
import org.json.JSONObject

data class NotificationRecord(
    val packageName: String,
    val title: String,
    val text: String,
    val postedAt: Long,
)

class NotificationReadService : NotificationListenerService() {
    override fun onNotificationPosted(statusBarNotification: StatusBarNotification) {
        record(statusBarNotification)
    }

    override fun onListenerConnected() {
        getActiveNotifications()?.forEach { record(it) }
    }

    private fun record(statusBarNotification: StatusBarNotification) {
        if (statusBarNotification.packageName == packageName) return

        val extras = statusBarNotification.notification.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString().orEmpty()
        val text = (
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
            )?.toString().orEmpty()
        if (title.isBlank() && text.isBlank()) return

        NotificationStore(this).add(
            NotificationRecord(
                packageName = statusBarNotification.packageName,
                title = title,
                text = text,
                postedAt = statusBarNotification.postTime,
            )
        )
    }

    companion object {
        private const val ENABLED_LISTENERS = "enabled_notification_listeners"

        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                ENABLED_LISTENERS,
            ).orEmpty()
            return enabled.split(':').mapNotNull { ComponentName.unflattenFromString(it) }.any {
                it.packageName == context.packageName &&
                    it.className == NotificationReadService::class.java.name
            }
        }

        fun openSettings(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}

class NotificationStore(context: Context) {
    private val preferences = context.applicationContext.getSharedPreferences(
        "notification_context",
        Context.MODE_PRIVATE,
    )

    @Synchronized
    fun add(record: NotificationRecord) {
        val current = runCatching { JSONArray(preferences.getString(KEY, "[]")) }
            .getOrDefault(JSONArray())
        val updated = JSONArray().apply { put(record.toJson()) }
        // ponytail: retain the newest 100 records; add Room/search when notification context needs history.
        for (index in 0 until minOf(current.length(), MAX_RECORDS - 1)) {
            updated.put(current.getJSONObject(index))
        }
        preferences.edit().putString(KEY, updated.toString()).apply()
    }

    fun recent(limit: Int = MAX_RECORDS): List<NotificationRecord> {
        val current = runCatching { JSONArray(preferences.getString(KEY, "[]")) }
            .getOrDefault(JSONArray())
        return (0 until minOf(current.length(), limit.coerceAtLeast(0))).map {
            val item = current.getJSONObject(it)
            NotificationRecord(
                packageName = item.optString("packageName"),
                title = item.optString("title"),
                text = item.optString("text"),
                postedAt = item.optLong("postedAt"),
            )
        }
    }

    private fun NotificationRecord.toJson() = JSONObject().apply {
        put("packageName", packageName)
        put("title", title)
        put("text", text)
        put("postedAt", postedAt)
    }

    companion object {
        private const val KEY = "records"
        private const val MAX_RECORDS = 100
    }
}
