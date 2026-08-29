package com.opengranola.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    @Query("SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, createdAt DESC")
    fun observeMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM plans ORDER BY updatedAt DESC")
    fun observePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plan_tasks ORDER BY priority ASC, position ASC")
    fun observePlanTasks(): Flow<List<PlanTaskEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, lastUsedAt DESC LIMIT :limit")
    suspend fun relevantMemories(limit: Int): List<MemoryEntity>

    @Query("SELECT * FROM plans WHERE status != 'done' ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun activePlans(limit: Int): List<PlanEntity>

    @Query("SELECT * FROM plan_tasks WHERE planId IN (:planIds) AND status != 'done' ORDER BY priority ASC, position ASC")
    suspend fun activeTasks(planIds: List<String>): List<PlanTaskEntity>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recentMessages(sessionId: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM context_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentEvents(limit: Int): List<ContextEventEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: PlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasks(tasks: List<PlanTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEvent(event: ContextEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: ContextSnapshotEntity)

    @Query("UPDATE memories SET archived = 1 WHERE id = :id")
    suspend fun archiveMemory(id: String)

    @Query("UPDATE plan_tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("DELETE FROM plan_tasks WHERE planId = :planId")
    suspend fun deleteTasksForPlan(planId: String)

    @Query("DELETE FROM plans WHERE id = :planId")
    suspend fun deletePlanRecord(planId: String)

    @Transaction
    suspend fun deletePlan(planId: String) {
        deleteTasksForPlan(planId)
        deletePlanRecord(planId)
    }

    @Query("DELETE FROM context_events WHERE id = :id")
    suspend fun deleteEvent(id: String)
}
