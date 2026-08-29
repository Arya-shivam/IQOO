package com.opengranola.android.usage

import android.app.AppOpsManager
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.provider.Settings
import java.util.Calendar

data class AppUsage(
    val packageName: String,
    val label: String,
    val minutes: Int,
    val share: Float
)

data class UsageSnapshot(
    val totalMinutes: Int = 0,
    val pickups: Int = 0,
    val apps: List<AppUsage> = emptyList(),
    val hasPermission: Boolean = false
)

class UsageStatsRepository(private val context: Context) {
    fun hasPermission(): Boolean {
        val appOps = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager
        val mode = appOps.unsafeCheckOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            android.os.Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    fun settingsIntent(): Intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)

    fun today(): UsageSnapshot {
        if (!hasPermission()) return UsageSnapshot(hasPermission = false)
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val end = System.currentTimeMillis()
        val manager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val stats = manager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, calendar.timeInMillis, end)
            .orEmpty()
            .filter { it.totalTimeInForeground > 30_000 && it.packageName != context.packageName }
        val total = stats.sumOf { it.totalTimeInForeground }.coerceAtLeast(1L)
        val apps = stats.sortedByDescending { it.totalTimeInForeground }.take(6).map { stat ->
            AppUsage(
                packageName = stat.packageName,
                label = appLabel(stat.packageName),
                minutes = (stat.totalTimeInForeground / 60_000L).toInt(),
                share = stat.totalTimeInForeground.toFloat() / total
            )
        }
        return UsageSnapshot(
            totalMinutes = (total / 60_000L).toInt(),
            // UsageStats does not expose reliable unlock counts on every OEM; app launches are a useful proxy.
            pickups = stats.sumOf { it.lastTimeUsed.takeIf { time -> time >= calendar.timeInMillis }?.let { 1L } ?: 0L }.toInt(),
            apps = apps,
            hasPermission = true
        )
    }

    private fun appLabel(packageName: String): String = runCatching {
        val info = context.packageManager.getApplicationInfo(packageName, 0)
        context.packageManager.getApplicationLabel(info).toString()
    }.getOrElse { packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() } }
}
