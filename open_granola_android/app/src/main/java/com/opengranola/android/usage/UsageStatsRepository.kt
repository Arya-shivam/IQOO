package com.opengranola.android.usage

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.provider.Settings
import java.util.Calendar

data class AppUsage(val label: String, val minutes: Int, val share: Float)
data class UsageSnapshot(val hasPermission: Boolean = false, val totalMinutes: Int = 0, val pickups: Int = 0, val apps: List<AppUsage> = emptyList())

class UsageStatsRepository(private val context: Context) {
    fun settingsIntent() = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun today(): UsageSnapshot {
        if (!hasPermission()) return UsageSnapshot()
        val start = Calendar.getInstance().apply { set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0); set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
        val stats = context.getSystemService(UsageStatsManager::class.java).queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, System.currentTimeMillis())
            .filter { it.totalTimeInForeground > 0 && it.packageName != context.packageName }
        val total = stats.sumOf { it.totalTimeInForeground }.toInt()
        val top = stats.sortedByDescending { it.totalTimeInForeground }.take(5).map {
            val label = runCatching { context.packageManager.getApplicationLabel(context.packageManager.getApplicationInfo(it.packageName, 0)).toString() }.getOrDefault(it.packageName)
            AppUsage(label, (it.totalTimeInForeground / 60_000L).toInt(), if (total == 0) 0f else it.totalTimeInForeground.toFloat() / total)
        }
        return UsageSnapshot(true, total / 60_000, stats.sumOf { it.appLaunchCount }, top)
    }

    private fun hasPermission(): Boolean = context.getSystemService(AppOpsManager::class.java)
        .checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, android.os.Process.myUid(), context.packageName) == AppOpsManager.MODE_ALLOWED
}
