package com.opengranola.android.ai

import android.content.Context
import androidx.work.Constraints
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opengranola.android.context.ContextAssembler
import com.opengranola.android.data.DailyInsightEntity
import com.opengranola.android.data.OpenGranolaDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

class FrontierCoachWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val client = OpenRouterCoachClient(applicationContext)
        if (!client.isConfigured) return Result.success()
        return runCatching {
            val context = ContextAssembler(OpenGranolaDatabase.get(applicationContext))
                .build("daily intent-reality briefing")
            val now = System.currentTimeMillis()
            OpenGranolaDatabase.get(applicationContext).assistantDao().saveDailyInsight(
                DailyInsightEntity(todayKey(), client.generateDailyBriefing(context.text), context.snapshotId, 0, now)
            )
            Result.success()
        }.getOrElse { Result.retry() }
    }

    companion object {
        fun schedule(context: Context) {
            val request = PeriodicWorkRequestBuilder<FrontierCoachWorker>(1, TimeUnit.DAYS)
                .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .build()
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "frontier-daily-coaching",
                ExistingPeriodicWorkPolicy.UPDATE,
                request
            )
        }

        private fun todayKey() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    }
}
