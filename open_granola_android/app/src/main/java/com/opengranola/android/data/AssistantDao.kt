package com.opengranola.android.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface AssistantDao {
    @Query("SELECT * FROM goals WHERE status = 'active' ORDER BY updatedAt DESC")
    fun observeGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM actions ORDER BY occurredAt DESC")
    fun observeActions(): Flow<List<ActionEntity>>

    @Query("SELECT * FROM graph_edges ORDER BY createdAt DESC")
    fun observeEdges(): Flow<List<GraphEdgeEntity>>

    @Query("SELECT * FROM graph_nodes ORDER BY updatedAt DESC")
    fun observeGraphNodes(): Flow<List<GraphNodeEntity>>

    @Query("SELECT * FROM graph_nodes ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun graphNodes(limit: Int): List<GraphNodeEntity>

    @Query("SELECT * FROM graph_edges ORDER BY createdAt DESC LIMIT :limit")
    suspend fun graphEdges(limit: Int): List<GraphEdgeEntity>

    @Query("SELECT COUNT(*) FROM graph_nodes WHERE id LIKE 'demo:%'")
    fun observeDemoNodeCount(): Flow<Int>

    @Query("SELECT * FROM goals WHERE status = 'active' ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun activeGoals(limit: Int): List<GoalEntity>

    @Query("SELECT * FROM actions ORDER BY occurredAt DESC LIMIT :limit")
    suspend fun recentActions(limit: Int): List<ActionEntity>

    @Query("SELECT a.* FROM actions a INNER JOIN graph_edges e ON e.fromType = 'action' AND e.fromId = a.id WHERE e.toType = 'goal' AND e.toId = :goalId AND a.occurredAt >= :since ORDER BY a.occurredAt DESC")
    suspend fun actionsForGoal(goalId: String, since: Long): List<ActionEntity>

    @Query("SELECT * FROM curation_queue WHERE status = 'pending' ORDER BY createdAt ASC LIMIT :limit")
    suspend fun pendingCuration(limit: Int): List<CurationQueueEntity>

    @Query("SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, createdAt DESC")
    fun observeMemories(): Flow<List<MemoryEntity>>

    @Query("SELECT * FROM plans ORDER BY updatedAt DESC")
    fun observePlans(): Flow<List<PlanEntity>>

    @Query("SELECT * FROM plan_tasks ORDER BY priority ASC, position ASC")
    fun observePlanTasks(): Flow<List<PlanTaskEntity>>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt ASC")
    fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM commitments ORDER BY CASE status WHEN 'open' THEN 0 ELSE 1 END, updatedAt DESC")
    fun observeCommitments(): Flow<List<CommitmentEntity>>

    @Query("SELECT * FROM daily_insights ORDER BY date DESC")
    fun observeDailyInsights(): Flow<List<DailyInsightEntity>>

    @Query("SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, lastUsedAt DESC LIMIT :limit")
    suspend fun relevantMemories(limit: Int): List<MemoryEntity>

    @Query("SELECT * FROM plans WHERE status != 'done' ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun activePlans(limit: Int): List<PlanEntity>

    @Query("SELECT * FROM plan_tasks WHERE planId IN (:planIds) AND status != 'done' ORDER BY priority ASC, position ASC")
    suspend fun activeTasks(planIds: List<String>): List<PlanTaskEntity>

    @Query("SELECT * FROM plan_tasks WHERE status = 'done' AND completedAt > 0 ORDER BY completedAt DESC LIMIT :limit")
    suspend fun recentCompletedTasks(limit: Int): List<PlanTaskEntity>

    @Query("SELECT * FROM chat_messages WHERE sessionId = :sessionId ORDER BY createdAt DESC LIMIT :limit")
    suspend fun recentMessages(sessionId: String, limit: Int): List<ChatMessageEntity>

    @Query("SELECT * FROM context_events ORDER BY timestamp DESC LIMIT :limit")
    suspend fun recentEvents(limit: Int): List<ContextEventEntity>

    @Query("SELECT * FROM commitments WHERE status = 'open' ORDER BY updatedAt DESC LIMIT :limit")
    suspend fun activeCommitments(limit: Int): List<CommitmentEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemory(memory: MemoryEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMemories(memories: List<MemoryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlan(plan: PlanEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun savePlans(plans: List<PlanEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveTasks(tasks: List<PlanTaskEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSession(session: ChatSessionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveMessage(message: ChatMessageEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEvent(event: ContextEventEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveEvents(events: List<ContextEventEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveSnapshot(snapshot: ContextSnapshotEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveCommitments(commitments: List<CommitmentEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveDailyInsight(insight: DailyInsightEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoal(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGoals(goals: List<GoalEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveAction(action: ActionEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveActions(actions: List<ActionEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveGraphNodes(nodes: List<GraphNodeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun saveEdges(edges: List<GraphEdgeEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun enqueue(item: CurationQueueEntity)

    @Query("UPDATE curation_queue SET status = :status, attempts = :attempts, lastError = :error WHERE id = :id")
    suspend fun updateCuration(id: String, status: String, attempts: Int, error: String)

    @Transaction
    suspend fun saveCurationResult(item: CurationQueueEntity, action: ActionEntity, node: GraphNodeEntity, edges: List<GraphEdgeEntity>) {
        saveAction(action)
        saveGraphNodes(listOf(node))
        saveEdges(edges)
        updateCuration(item.id, "complete", item.attempts + 1, "")
    }

    @Query("DELETE FROM memories WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoMemories()

    @Query("DELETE FROM plans WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoPlans()

    @Query("DELETE FROM plan_tasks WHERE id LIKE 'demo:%' OR planId LIKE 'demo:%'")
    suspend fun deleteDemoTasks()

    @Query("DELETE FROM context_events WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoEvents()

    @Query("DELETE FROM commitments WHERE id LIKE 'demo:%' OR meetingId LIKE 'demo:%'")
    suspend fun deleteDemoCommitments()

    @Query("DELETE FROM goals WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoGoals()

    @Query("DELETE FROM actions WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoActions()

    @Query("DELETE FROM graph_nodes WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoGraphNodes()

    @Query("DELETE FROM graph_edges WHERE id LIKE 'demo:%' OR fromId LIKE 'demo:%' OR toId LIKE 'demo:%'")
    suspend fun deleteDemoEdges()

    @Query("DELETE FROM curation_queue WHERE id LIKE 'demo:%'")
    suspend fun deleteDemoCuration()

    @Transaction
    suspend fun deleteDemoData() {
        deleteDemoMemories()
        deleteDemoTasks()
        deleteDemoPlans()
        deleteDemoEvents()
        deleteDemoCommitments()
        deleteDemoGoals()
        deleteDemoActions()
        deleteDemoEdges()
        deleteDemoGraphNodes()
        deleteDemoCuration()
    }

    @Query("UPDATE memories SET archived = 1 WHERE id = :id")
    suspend fun archiveMemory(id: String)

    @Query("UPDATE plan_tasks SET status = :status WHERE id = :id")
    suspend fun updateTaskStatus(id: String, status: String)

    @Query("UPDATE plan_tasks SET status = 'in_progress', startedAt = :startedAt, completedAt = 0, completionNote = '', completionCredibility = '' WHERE id = :id")
    suspend fun startTask(id: String, startedAt: Long)

    @Query("UPDATE plan_tasks SET estimatedMinutes = :minutes WHERE id = :id")
    suspend fun updateTaskEstimate(id: String, minutes: Int)

    @Query("UPDATE plan_tasks SET status = 'done', completedAt = :completedAt, completionNote = :note, completionCredibility = :credibility WHERE id = :id")
    suspend fun completeTask(id: String, completedAt: Long, note: String, credibility: String)

    @Query("UPDATE plan_tasks SET status = :status, completedAt = 0, completionNote = '', completionCredibility = '' WHERE id = :id")
    suspend fun reopenTask(id: String, status: String)

    @Query("UPDATE graph_nodes SET status = :status, updatedAt = :updatedAt WHERE id = :id OR sourceId = :id")
    suspend fun updateGraphNodeStatus(id: String, status: String, updatedAt: Long)

    @Transaction
    suspend fun updateTaskAndGraphStatus(id: String, status: String) {
        updateTaskStatus(id, status)
        updateGraphNodeStatus(id, status, System.currentTimeMillis())
    }

    @Transaction
    suspend fun startTaskAndGraph(id: String, startedAt: Long) {
        startTask(id, startedAt)
        updateGraphNodeStatus(id, "in_progress", startedAt)
    }

    @Transaction
    suspend fun completeTaskAndGraph(id: String, completedAt: Long, note: String, credibility: String) {
        completeTask(id, completedAt, note, credibility)
        updateGraphNodeStatus(id, "done", completedAt)
    }

    @Transaction
    suspend fun reopenTaskAndGraph(id: String, status: String) {
        reopenTask(id, status)
        updateGraphNodeStatus(id, status, System.currentTimeMillis())
    }

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

    @Query("UPDATE commitments SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateCommitmentStatus(id: String, status: String, updatedAt: Long)

    @Query("DELETE FROM commitments WHERE id = :id")
    suspend fun deleteCommitment(id: String)

    @Query("DELETE FROM commitments WHERE meetingId = :meetingId")
    suspend fun deleteCommitmentsForMeeting(meetingId: String)

    @Transaction
    suspend fun replaceMeetingCommitments(meetingId: String, commitments: List<CommitmentEntity>) {
        deleteCommitmentsForMeeting(meetingId)
        saveCommitments(commitments)
    }

    @Query("UPDATE daily_insights SET feedback = :feedback WHERE date = :date")
    suspend fun updateInsightFeedback(date: String, feedback: Int)

    @Query("DELETE FROM context_events WHERE source = 'calendar'")
    suspend fun clearCalendarEvents()

    @Transaction
    suspend fun replaceCalendarEvents(events: List<ContextEventEntity>) {
        clearCalendarEvents()
        events.forEach { saveEvent(it) }
    }
}
