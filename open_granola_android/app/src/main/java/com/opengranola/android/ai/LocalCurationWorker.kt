package com.opengranola.android.ai

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import com.opengranola.android.data.ActionEntity
import com.opengranola.android.data.GraphEdgeEntity
import com.opengranola.android.data.GraphNodeEntity
import com.opengranola.android.data.OpenGranolaDatabase
import java.util.UUID
import java.util.concurrent.TimeUnit

class LocalCurationWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {
    override suspend fun doWork(): Result {
        val database = OpenGranolaDatabase.get(applicationContext)
        val dao = database.assistantDao()
        val provider = GenieXLocalLlmProvider(applicationContext)
        val repository = GenieXModelRepository(applicationContext)
        val selected = repository.selected()
        val paths = selected?.let { runCatching { repository.paths(it) }.getOrNull() }
        if (paths != null) {
            val runtime = paths.runtime_id.ifBlank { selected.runtimeId }
            provider.useManagedModel(
                ManagedModel(
                    paths.model_name,
                    paths.model_path,
                    paths.tokenizer_path,
                    runtime,
                    if (runtime.equals("qairt", ignoreCase = true)) "npu" else repository.selectedComputeUnit()
                )
            )
        }
        val goals = dao.activeGoals(16)
        if (paths == null && goals.isNotEmpty()) return Result.success()
        dao.pendingCuration(20).forEach { item ->
            try {
                val curated = if (goals.isEmpty()) {
                    CuratedAction("event", item.title.take(240), item.content.take(1800), emptyList(), .4f, emptyList())
                } else {
                    provider.curate(CurationInput(item.source, item.title, item.content, item.occurredAt), goals)
                }
                val actionId = "action:${item.id}"
                val action = ActionEntity(
                    actionId,
                    item.source,
                    item.sourceId,
                    curated.type,
                    curated.title,
                    curated.summary,
                    curated.tags.joinToString(","),
                    curated.importance,
                    when {
                        curated.type == "noise" -> "ignored"
                        curated.goalIds.isEmpty() -> "unlinked"
                        else -> "linked"
                    },
                    item.occurredAt,
                    item.createdAt
                )
                val edges = curated.goalIds.map { goalId ->
                    GraphEdgeEntity(UUID.randomUUID().toString(), "action", actionId, "goal", goalId, "contributes_to", .75f, curated.summary.take(300), System.currentTimeMillis())
                }
                val node = GraphNodeEntity(actionId, "action", curated.title, curated.summary, curated.tags.joinToString(","), action.linkStatus, item.sourceId, item.createdAt, System.currentTimeMillis())
                dao.saveCurationResult(item, action, node, edges)
            } catch (error: Throwable) {
                dao.updateCuration(item.id, "failed", item.attempts + 1, error.message.orEmpty().take(500))
            }
        }
        return Result.success()
    }

    companion object {
        fun schedule(context: Context) {
            WorkManager.getInstance(context).enqueueUniquePeriodicWork(
                "local-context-curation",
                ExistingPeriodicWorkPolicy.UPDATE,
                PeriodicWorkRequestBuilder<LocalCurationWorker>(15, TimeUnit.MINUTES).build()
            )
        }
    }
}
