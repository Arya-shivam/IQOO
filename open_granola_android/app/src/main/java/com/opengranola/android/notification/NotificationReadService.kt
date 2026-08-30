package com.opengranola.android.notification

import android.app.Notification
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import com.opengranola.android.data.NotificationEntity
import com.opengranola.android.data.CurationQueueEntity
import com.opengranola.android.data.OpenGranolaDatabase
import com.opengranola.android.ai.LocalCurationWorker
import com.opengranola.android.notifications.NotificationRedactor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/** Read-only notification context collector ported from the notifications-read app. */
class NotificationReadService : NotificationListenerService() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNotificationPosted(sbn: StatusBarNotification) = record(sbn)

    // The listener can be enabled after notifications already exist. Backfill
    // the active shade so the first assistant briefing is not empty.
    override fun onListenerConnected() {
        getActiveNotifications()?.forEach(::record)
    }

    private fun record(sbn: StatusBarNotification) {
        if (sbn.packageName == packageName || sbn.isOngoing) return
        val extras = sbn.notification.extras
        val title = NotificationRedactor.clean(extras.getCharSequence(Notification.EXTRA_TITLE))
        val body = NotificationRedactor.clean(
            extras.getCharSequence(Notification.EXTRA_BIG_TEXT)
                ?: extras.getCharSequence(Notification.EXTRA_TEXT)
        )
        if (title.isBlank() && body.isBlank()) return
        val appLabel = runCatching {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(sbn.packageName, 0)).toString()
        }.getOrDefault(sbn.packageName)
        scope.launch {
            val database = OpenGranolaDatabase.get(applicationContext)
            database.notificationDao().save(
                NotificationEntity(
                    id = "${sbn.packageName}:${sbn.key}",
                    packageName = sbn.packageName,
                    appLabel = appLabel,
                    title = title.ifBlank { appLabel },
                    body = body,
                    postedAt = sbn.postTime
                )
            )
            database.assistantDao().enqueue(
                CurationQueueEntity(
                    id = "notification:${sbn.key}",
                    source = "notification",
                    sourceId = sbn.key,
                    title = title.ifBlank { appLabel },
                    content = body,
                    occurredAt = sbn.postTime,
                    status = "pending",
                    attempts = 0,
                    lastError = "",
                    createdAt = System.currentTimeMillis()
                )
            )
            LocalCurationWorker.schedule(applicationContext)
        }
    }

    override fun onDestroy() {
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        private const val ENABLED_LISTENERS = "enabled_notification_listeners"

        fun isEnabled(context: Context): Boolean {
            val enabled = Settings.Secure.getString(
                context.contentResolver,
                ENABLED_LISTENERS
            ).orEmpty()
            return enabled.split(':')
                .mapNotNull(ComponentName::unflattenFromString)
                .any { it.packageName == context.packageName && it.className == NotificationReadService::class.java.name }
        }

        fun openSettings(context: Context) {
            context.startActivity(
                Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        }
    }
}
