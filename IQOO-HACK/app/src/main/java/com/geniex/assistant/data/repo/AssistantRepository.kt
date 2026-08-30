package com.geniex.assistant.data.repo

import com.geniex.assistant.data.db.AppDatabase
import com.geniex.assistant.data.db.GoalEntity
import com.geniex.assistant.data.db.MeetingEntity
import com.geniex.assistant.data.db.MemoryEntity
import com.geniex.assistant.data.db.SettingEntity
import com.geniex.assistant.data.db.TaskEntity
import com.geniex.assistant.model.GoalStatus
import com.geniex.assistant.model.MemoryType
import com.geniex.assistant.model.TaskStatus
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AssistantRepository(private val db: AppDatabase) {

    fun observeGoals(): Flow<List<GoalEntity>> = db.goalDao().observeGoals()
    fun observeTasks(): Flow<List<TaskEntity>> = db.taskDao().observeTasks()
    fun observeMemories(): Flow<List<MemoryEntity>> = db.memoryDao().observeMemories()
    fun observeMeetings(): Flow<List<MeetingEntity>> = db.meetingDao().observeMeetings()
    fun observeSettings(): Flow<List<SettingEntity>> = db.settingsDao().observeAll()

    suspend fun createGoal(goal: GoalEntity): Long = db.goalDao().insert(goal)

    suspend fun latestActiveGoalId(): Long? = db.goalDao().getLatestActiveGoalId()

    suspend fun activeGoalIdByTitle(title: String): Long? =
        db.goalDao().getActiveGoalIdByTitle(title)

    suspend fun insertGoalTasks(tasks: List<TaskEntity>) {
        db.taskDao().insertAll(tasks)
    }

    suspend fun createTask(task: TaskEntity): Long = db.taskDao().insert(task)

    suspend fun openTaskExists(goalId: Long, title: String): Boolean =
        db.taskDao().openTaskExists(goalId, title)

    suspend fun updateTask(task: TaskEntity) {
        db.taskDao().update(task)
    }

    suspend fun getOpenTasks(): List<TaskEntity> = db.taskDao().getOpenTasks()

    suspend fun markTaskDone(taskId: Long): Boolean {
        val task = db.taskDao().getTask(taskId) ?: return false
        db.taskDao().update(
            task.copy(
                status = TaskStatus.COMPLETED,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
        return true
    }

    suspend fun upsertSetting(key: String, value: String) {
        db.settingsDao().upsert(
            SettingEntity(
                key = key,
                value = value,
                updatedAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun getSetting(key: String): String? = db.settingsDao().getByKey(key)?.value

    suspend fun storeMeeting(
        title: String,
        transcript: String,
        summary: String,
        audioPath: String? = null,
        assistantReply: String = ""
    ): Long {
        return db.meetingDao().insert(
            MeetingEntity(
                title = title,
                transcript = transcript,
                summary = summary,
                audioPath = audioPath,
                assistantReply = assistantReply,
                createdAtEpochMs = System.currentTimeMillis()
            )
        )
    }

    suspend fun storeMemories(memories: List<Pair<MemoryType, String>>) {
        val now = System.currentTimeMillis()
        memories
            .map { (type, content) -> type to content.trim() }
            .filter { (_, content) -> content.isNotBlank() }
            .distinct()
            .forEach { (type, content) ->
                if (db.memoryDao().memoryExists(type.name, content)) return@forEach
                db.memoryDao().insert(
                    MemoryEntity(
                        type = type,
                        content = content,
                        importanceScore = when (type) {
                            MemoryType.DECISION -> 9
                            MemoryType.COMMITMENT -> 8
                            MemoryType.LONG_TERM -> 7
                            MemoryType.MEETING -> 6
                            MemoryType.EPISODIC -> 5
                        },
                        relatedGoalId = null,
                        relatedTaskId = null,
                        createdAtEpochMs = now
                    )
                )
            }
    }

    suspend fun completeGoalIfAllTasksDone(goalId: Long) {
        val tasks = db.taskDao().getTasksByGoal(goalId)
        if (tasks.isNotEmpty() && tasks.all { it.status == TaskStatus.COMPLETED }) {
            val goal = db.goalDao().getGoalById(goalId) ?: return
            db.goalDao().update(
                goal.copy(
                    status = GoalStatus.COMPLETED,
                    updatedAtEpochMs = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun hasAnyData(): Boolean {
        return db.goalDao().countGoals() > 0 ||
            db.taskDao().countTasks() > 0 ||
            db.meetingDao().countMeetings() > 0 ||
            db.memoryDao().countMemories() > 0
    }

    suspend fun clearAllData() {
        withContext(Dispatchers.IO) {
            db.clearAllTables()
        }
    }
}
