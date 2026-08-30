package com.opengranola.android.`data`

import androidx.room.EntityInsertAdapter
import androidx.room.RoomDatabase
import androidx.room.coroutines.createFlow
import androidx.room.util.appendPlaceholders
import androidx.room.util.getColumnIndexOrThrow
import androidx.room.util.performInTransactionSuspending
import androidx.room.util.performSuspending
import androidx.sqlite.SQLiteStatement
import javax.`annotation`.processing.Generated
import kotlin.Boolean
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.Suppress
import kotlin.Unit
import kotlin.collections.List
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.reflect.KClass
import kotlin.text.StringBuilder
import kotlinx.coroutines.flow.Flow

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class AssistantDao_Impl(
  __db: RoomDatabase,
) : AssistantDao {
  private val __db: RoomDatabase

  private val __insertAdapterOfMemoryEntity: EntityInsertAdapter<MemoryEntity>

  private val __insertAdapterOfPlanEntity: EntityInsertAdapter<PlanEntity>

  private val __insertAdapterOfPlanTaskEntity: EntityInsertAdapter<PlanTaskEntity>

  private val __insertAdapterOfChatSessionEntity: EntityInsertAdapter<ChatSessionEntity>

  private val __insertAdapterOfChatMessageEntity: EntityInsertAdapter<ChatMessageEntity>

  private val __insertAdapterOfContextEventEntity: EntityInsertAdapter<ContextEventEntity>

  private val __insertAdapterOfContextSnapshotEntity: EntityInsertAdapter<ContextSnapshotEntity>

  private val __insertAdapterOfCommitmentEntity: EntityInsertAdapter<CommitmentEntity>

  private val __insertAdapterOfDailyInsightEntity: EntityInsertAdapter<DailyInsightEntity>

  private val __insertAdapterOfGoalEntity: EntityInsertAdapter<GoalEntity>

  private val __insertAdapterOfActionEntity: EntityInsertAdapter<ActionEntity>

  private val __insertAdapterOfGraphNodeEntity: EntityInsertAdapter<GraphNodeEntity>

  private val __insertAdapterOfGraphEdgeEntity: EntityInsertAdapter<GraphEdgeEntity>

  private val __insertAdapterOfCurationQueueEntity: EntityInsertAdapter<CurationQueueEntity>
  init {
    this.__db = __db
    this.__insertAdapterOfMemoryEntity = object : EntityInsertAdapter<MemoryEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `memories` (`id`,`text`,`source`,`importance`,`tags`,`createdAt`,`lastUsedAt`,`archived`) VALUES (?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: MemoryEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.text)
        statement.bindText(3, entity.source)
        statement.bindDouble(4, entity.importance.toDouble())
        statement.bindText(5, entity.tags)
        statement.bindLong(6, entity.createdAt)
        statement.bindLong(7, entity.lastUsedAt)
        val _tmp: Int = if (entity.archived) 1 else 0
        statement.bindLong(8, _tmp.toLong())
      }
    }
    this.__insertAdapterOfPlanEntity = object : EntityInsertAdapter<PlanEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `plans` (`id`,`title`,`objective`,`status`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlanEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.objective)
        statement.bindText(4, entity.status)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
      }
    }
    this.__insertAdapterOfPlanTaskEntity = object : EntityInsertAdapter<PlanTaskEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `plan_tasks` (`id`,`planId`,`title`,`details`,`status`,`priority`,`position`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: PlanTaskEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.planId)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.details)
        statement.bindText(5, entity.status)
        statement.bindLong(6, entity.priority.toLong())
        statement.bindLong(7, entity.position.toLong())
      }
    }
    this.__insertAdapterOfChatSessionEntity = object : EntityInsertAdapter<ChatSessionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `chat_sessions` (`id`,`title`,`createdAt`,`updatedAt`) VALUES (?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatSessionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindLong(3, entity.createdAt)
        statement.bindLong(4, entity.updatedAt)
      }
    }
    this.__insertAdapterOfChatMessageEntity = object : EntityInsertAdapter<ChatMessageEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `chat_messages` (`id`,`sessionId`,`role`,`content`,`createdAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ChatMessageEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.sessionId)
        statement.bindText(3, entity.role)
        statement.bindText(4, entity.content)
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__insertAdapterOfContextEventEntity = object : EntityInsertAdapter<ContextEventEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `context_events` (`id`,`source`,`type`,`title`,`content`,`timestamp`,`importance`) VALUES (?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ContextEventEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.source)
        statement.bindText(3, entity.type)
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.content)
        statement.bindLong(6, entity.timestamp)
        statement.bindDouble(7, entity.importance.toDouble())
      }
    }
    this.__insertAdapterOfContextSnapshotEntity = object :
        EntityInsertAdapter<ContextSnapshotEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `context_snapshots` (`id`,`purpose`,`renderedContext`,`sourceIds`,`createdAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ContextSnapshotEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.purpose)
        statement.bindText(3, entity.renderedContext)
        statement.bindText(4, entity.sourceIds)
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__insertAdapterOfCommitmentEntity = object : EntityInsertAdapter<CommitmentEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `commitments` (`id`,`meetingId`,`sourceTitle`,`title`,`owner`,`dueText`,`evidence`,`confidence`,`status`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CommitmentEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.meetingId)
        statement.bindText(3, entity.sourceTitle)
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.owner)
        statement.bindText(6, entity.dueText)
        statement.bindText(7, entity.evidence)
        statement.bindDouble(8, entity.confidence.toDouble())
        statement.bindText(9, entity.status)
        statement.bindLong(10, entity.createdAt)
        statement.bindLong(11, entity.updatedAt)
      }
    }
    this.__insertAdapterOfDailyInsightEntity = object : EntityInsertAdapter<DailyInsightEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `daily_insights` (`date`,`briefing`,`contextSnapshotId`,`feedback`,`createdAt`) VALUES (?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: DailyInsightEntity) {
        statement.bindText(1, entity.date)
        statement.bindText(2, entity.briefing)
        statement.bindText(3, entity.contextSnapshotId)
        statement.bindLong(4, entity.feedback.toLong())
        statement.bindLong(5, entity.createdAt)
      }
    }
    this.__insertAdapterOfGoalEntity = object : EntityInsertAdapter<GoalEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `goals` (`id`,`title`,`description`,`status`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GoalEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.title)
        statement.bindText(3, entity.description)
        statement.bindText(4, entity.status)
        statement.bindLong(5, entity.createdAt)
        statement.bindLong(6, entity.updatedAt)
      }
    }
    this.__insertAdapterOfActionEntity = object : EntityInsertAdapter<ActionEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `actions` (`id`,`source`,`sourceId`,`type`,`title`,`summary`,`tags`,`importance`,`linkStatus`,`occurredAt`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: ActionEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.source)
        statement.bindText(3, entity.sourceId)
        statement.bindText(4, entity.type)
        statement.bindText(5, entity.title)
        statement.bindText(6, entity.summary)
        statement.bindText(7, entity.tags)
        statement.bindDouble(8, entity.importance.toDouble())
        statement.bindText(9, entity.linkStatus)
        statement.bindLong(10, entity.occurredAt)
        statement.bindLong(11, entity.createdAt)
      }
    }
    this.__insertAdapterOfGraphNodeEntity = object : EntityInsertAdapter<GraphNodeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR REPLACE INTO `graph_nodes` (`id`,`type`,`title`,`details`,`tags`,`status`,`sourceId`,`createdAt`,`updatedAt`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GraphNodeEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.type)
        statement.bindText(3, entity.title)
        statement.bindText(4, entity.details)
        statement.bindText(5, entity.tags)
        statement.bindText(6, entity.status)
        statement.bindText(7, entity.sourceId)
        statement.bindLong(8, entity.createdAt)
        statement.bindLong(9, entity.updatedAt)
      }
    }
    this.__insertAdapterOfGraphEdgeEntity = object : EntityInsertAdapter<GraphEdgeEntity>() {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `graph_edges` (`id`,`fromType`,`fromId`,`toType`,`toId`,`type`,`confidence`,`evidence`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: GraphEdgeEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.fromType)
        statement.bindText(3, entity.fromId)
        statement.bindText(4, entity.toType)
        statement.bindText(5, entity.toId)
        statement.bindText(6, entity.type)
        statement.bindDouble(7, entity.confidence.toDouble())
        statement.bindText(8, entity.evidence)
        statement.bindLong(9, entity.createdAt)
      }
    }
    this.__insertAdapterOfCurationQueueEntity = object : EntityInsertAdapter<CurationQueueEntity>()
        {
      protected override fun createQuery(): String =
          "INSERT OR IGNORE INTO `curation_queue` (`id`,`source`,`sourceId`,`title`,`content`,`occurredAt`,`status`,`attempts`,`lastError`,`createdAt`) VALUES (?,?,?,?,?,?,?,?,?,?)"

      protected override fun bind(statement: SQLiteStatement, entity: CurationQueueEntity) {
        statement.bindText(1, entity.id)
        statement.bindText(2, entity.source)
        statement.bindText(3, entity.sourceId)
        statement.bindText(4, entity.title)
        statement.bindText(5, entity.content)
        statement.bindLong(6, entity.occurredAt)
        statement.bindText(7, entity.status)
        statement.bindLong(8, entity.attempts.toLong())
        statement.bindText(9, entity.lastError)
        statement.bindLong(10, entity.createdAt)
      }
    }
  }

  public override suspend fun saveMemory(memory: MemoryEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfMemoryEntity.insert(_connection, memory)
  }

  public override suspend fun saveMemories(memories: List<MemoryEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfMemoryEntity.insert(_connection, memories)
  }

  public override suspend fun savePlan(plan: PlanEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfPlanEntity.insert(_connection, plan)
  }

  public override suspend fun savePlans(plans: List<PlanEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfPlanEntity.insert(_connection, plans)
  }

  public override suspend fun saveTasks(tasks: List<PlanTaskEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfPlanTaskEntity.insert(_connection, tasks)
  }

  public override suspend fun saveSession(session: ChatSessionEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfChatSessionEntity.insert(_connection, session)
  }

  public override suspend fun saveMessage(message: ChatMessageEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfChatMessageEntity.insert(_connection, message)
  }

  public override suspend fun saveEvent(event: ContextEventEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfContextEventEntity.insert(_connection, event)
  }

  public override suspend fun saveEvents(events: List<ContextEventEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfContextEventEntity.insert(_connection, events)
  }

  public override suspend fun saveSnapshot(snapshot: ContextSnapshotEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfContextSnapshotEntity.insert(_connection, snapshot)
  }

  public override suspend fun saveCommitments(commitments: List<CommitmentEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfCommitmentEntity.insert(_connection, commitments)
  }

  public override suspend fun saveDailyInsight(insight: DailyInsightEntity): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfDailyInsightEntity.insert(_connection, insight)
  }

  public override suspend fun saveGoal(goal: GoalEntity): Unit = performSuspending(__db, false,
      true) { _connection ->
    __insertAdapterOfGoalEntity.insert(_connection, goal)
  }

  public override suspend fun saveGoals(goals: List<GoalEntity>): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfGoalEntity.insert(_connection, goals)
  }

  public override suspend fun saveAction(action: ActionEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfActionEntity.insert(_connection, action)
  }

  public override suspend fun saveActions(actions: List<ActionEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfActionEntity.insert(_connection, actions)
  }

  public override suspend fun saveGraphNodes(nodes: List<GraphNodeEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGraphNodeEntity.insert(_connection, nodes)
  }

  public override suspend fun saveEdges(edges: List<GraphEdgeEntity>): Unit =
      performSuspending(__db, false, true) { _connection ->
    __insertAdapterOfGraphEdgeEntity.insert(_connection, edges)
  }

  public override suspend fun enqueue(item: CurationQueueEntity): Unit = performSuspending(__db,
      false, true) { _connection ->
    __insertAdapterOfCurationQueueEntity.insert(_connection, item)
  }

  public override suspend fun saveCurationResult(
    item: CurationQueueEntity,
    action: ActionEntity,
    node: GraphNodeEntity,
    edges: List<GraphEdgeEntity>,
  ): Unit = performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.saveCurationResult(item, action, node, edges)
  }

  public override suspend fun deleteDemoData(): Unit = performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.deleteDemoData()
  }

  public override suspend fun updateTaskAndGraphStatus(id: String, status: String): Unit =
      performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.updateTaskAndGraphStatus(id, status)
  }

  public override suspend fun deletePlan(planId: String): Unit =
      performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.deletePlan(planId)
  }

  public override suspend fun replaceMeetingCommitments(meetingId: String,
      commitments: List<CommitmentEntity>): Unit = performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.replaceMeetingCommitments(meetingId, commitments)
  }

  public override suspend fun replaceCalendarEvents(events: List<ContextEventEntity>): Unit =
      performInTransactionSuspending(__db) {
    super@AssistantDao_Impl.replaceCalendarEvents(events)
  }

  public override fun observeGoals(): Flow<List<GoalEntity>> {
    val _sql: String = "SELECT * FROM goals WHERE status = 'active' ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("goals")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              GoalEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeActions(): Flow<List<ActionEntity>> {
    val _sql: String = "SELECT * FROM actions ORDER BY occurredAt DESC"
    return createFlow(__db, false, arrayOf("actions")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSummary: Int = getColumnIndexOrThrow(_stmt, "summary")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _columnIndexOfLinkStatus: Int = getColumnIndexOrThrow(_stmt, "linkStatus")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ActionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          val _tmpLinkStatus: String
          _tmpLinkStatus = _stmt.getText(_columnIndexOfLinkStatus)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ActionEntity(_tmpId,_tmpSource,_tmpSourceId,_tmpType,_tmpTitle,_tmpSummary,_tmpTags,_tmpImportance,_tmpLinkStatus,_tmpOccurredAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeEdges(): Flow<List<GraphEdgeEntity>> {
    val _sql: String = "SELECT * FROM graph_edges ORDER BY createdAt DESC"
    return createFlow(__db, false, arrayOf("graph_edges")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFromType: Int = getColumnIndexOrThrow(_stmt, "fromType")
        val _columnIndexOfFromId: Int = getColumnIndexOrThrow(_stmt, "fromId")
        val _columnIndexOfToType: Int = getColumnIndexOrThrow(_stmt, "toType")
        val _columnIndexOfToId: Int = getColumnIndexOrThrow(_stmt, "toId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfEvidence: Int = getColumnIndexOrThrow(_stmt, "evidence")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<GraphEdgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GraphEdgeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpFromType: String
          _tmpFromType = _stmt.getText(_columnIndexOfFromType)
          val _tmpFromId: String
          _tmpFromId = _stmt.getText(_columnIndexOfFromId)
          val _tmpToType: String
          _tmpToType = _stmt.getText(_columnIndexOfToType)
          val _tmpToId: String
          _tmpToId = _stmt.getText(_columnIndexOfToId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpConfidence: Float
          _tmpConfidence = _stmt.getDouble(_columnIndexOfConfidence).toFloat()
          val _tmpEvidence: String
          _tmpEvidence = _stmt.getText(_columnIndexOfEvidence)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              GraphEdgeEntity(_tmpId,_tmpFromType,_tmpFromId,_tmpToType,_tmpToId,_tmpType,_tmpConfidence,_tmpEvidence,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeGraphNodes(): Flow<List<GraphNodeEntity>> {
    val _sql: String = "SELECT * FROM graph_nodes ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("graph_nodes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDetails: Int = getColumnIndexOrThrow(_stmt, "details")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<GraphNodeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GraphNodeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDetails: String
          _tmpDetails = _stmt.getText(_columnIndexOfDetails)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              GraphNodeEntity(_tmpId,_tmpType,_tmpTitle,_tmpDetails,_tmpTags,_tmpStatus,_tmpSourceId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun graphNodes(limit: Int): List<GraphNodeEntity> {
    val _sql: String = "SELECT * FROM graph_nodes ORDER BY updatedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDetails: Int = getColumnIndexOrThrow(_stmt, "details")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<GraphNodeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GraphNodeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDetails: String
          _tmpDetails = _stmt.getText(_columnIndexOfDetails)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              GraphNodeEntity(_tmpId,_tmpType,_tmpTitle,_tmpDetails,_tmpTags,_tmpStatus,_tmpSourceId,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun graphEdges(limit: Int): List<GraphEdgeEntity> {
    val _sql: String = "SELECT * FROM graph_edges ORDER BY createdAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfFromType: Int = getColumnIndexOrThrow(_stmt, "fromType")
        val _columnIndexOfFromId: Int = getColumnIndexOrThrow(_stmt, "fromId")
        val _columnIndexOfToType: Int = getColumnIndexOrThrow(_stmt, "toType")
        val _columnIndexOfToId: Int = getColumnIndexOrThrow(_stmt, "toId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfEvidence: Int = getColumnIndexOrThrow(_stmt, "evidence")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<GraphEdgeEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GraphEdgeEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpFromType: String
          _tmpFromType = _stmt.getText(_columnIndexOfFromType)
          val _tmpFromId: String
          _tmpFromId = _stmt.getText(_columnIndexOfFromId)
          val _tmpToType: String
          _tmpToType = _stmt.getText(_columnIndexOfToType)
          val _tmpToId: String
          _tmpToId = _stmt.getText(_columnIndexOfToId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpConfidence: Float
          _tmpConfidence = _stmt.getDouble(_columnIndexOfConfidence).toFloat()
          val _tmpEvidence: String
          _tmpEvidence = _stmt.getText(_columnIndexOfEvidence)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              GraphEdgeEntity(_tmpId,_tmpFromType,_tmpFromId,_tmpToType,_tmpToId,_tmpType,_tmpConfidence,_tmpEvidence,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeDemoNodeCount(): Flow<Int> {
    val _sql: String = "SELECT COUNT(*) FROM graph_nodes WHERE id LIKE 'demo:%'"
    return createFlow(__db, false, arrayOf("graph_nodes")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _result: Int
        if (_stmt.step()) {
          val _tmp: Int
          _tmp = _stmt.getLong(0).toInt()
          _result = _tmp
        } else {
          _result = 0
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun activeGoals(limit: Int): List<GoalEntity> {
    val _sql: String = "SELECT * FROM goals WHERE status = 'active' ORDER BY updatedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDescription: Int = getColumnIndexOrThrow(_stmt, "description")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<GoalEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: GoalEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDescription: String
          _tmpDescription = _stmt.getText(_columnIndexOfDescription)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              GoalEntity(_tmpId,_tmpTitle,_tmpDescription,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recentActions(limit: Int): List<ActionEntity> {
    val _sql: String = "SELECT * FROM actions ORDER BY occurredAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSummary: Int = getColumnIndexOrThrow(_stmt, "summary")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _columnIndexOfLinkStatus: Int = getColumnIndexOrThrow(_stmt, "linkStatus")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ActionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          val _tmpLinkStatus: String
          _tmpLinkStatus = _stmt.getText(_columnIndexOfLinkStatus)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ActionEntity(_tmpId,_tmpSource,_tmpSourceId,_tmpType,_tmpTitle,_tmpSummary,_tmpTags,_tmpImportance,_tmpLinkStatus,_tmpOccurredAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun actionsForGoal(goalId: String, since: Long): List<ActionEntity> {
    val _sql: String =
        "SELECT a.* FROM actions a INNER JOIN graph_edges e ON e.fromType = 'action' AND e.fromId = a.id WHERE e.toType = 'goal' AND e.toId = ? AND a.occurredAt >= ? ORDER BY a.occurredAt DESC"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, goalId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, since)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfSummary: Int = getColumnIndexOrThrow(_stmt, "summary")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _columnIndexOfLinkStatus: Int = getColumnIndexOrThrow(_stmt, "linkStatus")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ActionEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ActionEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpSummary: String
          _tmpSummary = _stmt.getText(_columnIndexOfSummary)
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          val _tmpLinkStatus: String
          _tmpLinkStatus = _stmt.getText(_columnIndexOfLinkStatus)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              ActionEntity(_tmpId,_tmpSource,_tmpSourceId,_tmpType,_tmpTitle,_tmpSummary,_tmpTags,_tmpImportance,_tmpLinkStatus,_tmpOccurredAt,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun pendingCuration(limit: Int): List<CurationQueueEntity> {
    val _sql: String =
        "SELECT * FROM curation_queue WHERE status = 'pending' ORDER BY createdAt ASC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfSourceId: Int = getColumnIndexOrThrow(_stmt, "sourceId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfOccurredAt: Int = getColumnIndexOrThrow(_stmt, "occurredAt")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfAttempts: Int = getColumnIndexOrThrow(_stmt, "attempts")
        val _columnIndexOfLastError: Int = getColumnIndexOrThrow(_stmt, "lastError")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<CurationQueueEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CurationQueueEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpSourceId: String
          _tmpSourceId = _stmt.getText(_columnIndexOfSourceId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpOccurredAt: Long
          _tmpOccurredAt = _stmt.getLong(_columnIndexOfOccurredAt)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpAttempts: Int
          _tmpAttempts = _stmt.getLong(_columnIndexOfAttempts).toInt()
          val _tmpLastError: String
          _tmpLastError = _stmt.getText(_columnIndexOfLastError)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              CurationQueueEntity(_tmpId,_tmpSource,_tmpSourceId,_tmpTitle,_tmpContent,_tmpOccurredAt,_tmpStatus,_tmpAttempts,_tmpLastError,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeMemories(): Flow<List<MemoryEntity>> {
    val _sql: String =
        "SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, createdAt DESC"
    return createFlow(__db, false, arrayOf("memories")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastUsedAt: Int = getColumnIndexOrThrow(_stmt, "lastUsedAt")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _result: MutableList<MemoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastUsedAt: Long
          _tmpLastUsedAt = _stmt.getLong(_columnIndexOfLastUsedAt)
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          _item =
              MemoryEntity(_tmpId,_tmpText,_tmpSource,_tmpImportance,_tmpTags,_tmpCreatedAt,_tmpLastUsedAt,_tmpArchived)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePlans(): Flow<List<PlanEntity>> {
    val _sql: String = "SELECT * FROM plans ORDER BY updatedAt DESC"
    return createFlow(__db, false, arrayOf("plans")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfObjective: Int = getColumnIndexOrThrow(_stmt, "objective")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<PlanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlanEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpObjective: String
          _tmpObjective = _stmt.getText(_columnIndexOfObjective)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = PlanEntity(_tmpId,_tmpTitle,_tmpObjective,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observePlanTasks(): Flow<List<PlanTaskEntity>> {
    val _sql: String = "SELECT * FROM plan_tasks ORDER BY priority ASC, position ASC"
    return createFlow(__db, false, arrayOf("plan_tasks")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDetails: Int = getColumnIndexOrThrow(_stmt, "details")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<PlanTaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlanTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: String
          _tmpPlanId = _stmt.getText(_columnIndexOfPlanId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDetails: String
          _tmpDetails = _stmt.getText(_columnIndexOfDetails)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item =
              PlanTaskEntity(_tmpId,_tmpPlanId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpPosition)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeMessages(sessionId: String): Flow<List<ChatMessageEntity>> {
    val _sql: String = "SELECT * FROM chat_messages WHERE sessionId = ? ORDER BY createdAt ASC"
    return createFlow(__db, false, arrayOf("chat_messages")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ChatMessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatMessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ChatMessageEntity(_tmpId,_tmpSessionId,_tmpRole,_tmpContent,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeCommitments(): Flow<List<CommitmentEntity>> {
    val _sql: String =
        "SELECT * FROM commitments ORDER BY CASE status WHEN 'open' THEN 0 ELSE 1 END, updatedAt DESC"
    return createFlow(__db, false, arrayOf("commitments")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMeetingId: Int = getColumnIndexOrThrow(_stmt, "meetingId")
        val _columnIndexOfSourceTitle: Int = getColumnIndexOrThrow(_stmt, "sourceTitle")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfOwner: Int = getColumnIndexOrThrow(_stmt, "owner")
        val _columnIndexOfDueText: Int = getColumnIndexOrThrow(_stmt, "dueText")
        val _columnIndexOfEvidence: Int = getColumnIndexOrThrow(_stmt, "evidence")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<CommitmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CommitmentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMeetingId: String
          _tmpMeetingId = _stmt.getText(_columnIndexOfMeetingId)
          val _tmpSourceTitle: String
          _tmpSourceTitle = _stmt.getText(_columnIndexOfSourceTitle)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpOwner: String
          _tmpOwner = _stmt.getText(_columnIndexOfOwner)
          val _tmpDueText: String
          _tmpDueText = _stmt.getText(_columnIndexOfDueText)
          val _tmpEvidence: String
          _tmpEvidence = _stmt.getText(_columnIndexOfEvidence)
          val _tmpConfidence: Float
          _tmpConfidence = _stmt.getDouble(_columnIndexOfConfidence).toFloat()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              CommitmentEntity(_tmpId,_tmpMeetingId,_tmpSourceTitle,_tmpTitle,_tmpOwner,_tmpDueText,_tmpEvidence,_tmpConfidence,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override fun observeDailyInsights(): Flow<List<DailyInsightEntity>> {
    val _sql: String = "SELECT * FROM daily_insights ORDER BY date DESC"
    return createFlow(__db, false, arrayOf("daily_insights")) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        val _columnIndexOfDate: Int = getColumnIndexOrThrow(_stmt, "date")
        val _columnIndexOfBriefing: Int = getColumnIndexOrThrow(_stmt, "briefing")
        val _columnIndexOfContextSnapshotId: Int = getColumnIndexOrThrow(_stmt, "contextSnapshotId")
        val _columnIndexOfFeedback: Int = getColumnIndexOrThrow(_stmt, "feedback")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<DailyInsightEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: DailyInsightEntity
          val _tmpDate: String
          _tmpDate = _stmt.getText(_columnIndexOfDate)
          val _tmpBriefing: String
          _tmpBriefing = _stmt.getText(_columnIndexOfBriefing)
          val _tmpContextSnapshotId: String
          _tmpContextSnapshotId = _stmt.getText(_columnIndexOfContextSnapshotId)
          val _tmpFeedback: Int
          _tmpFeedback = _stmt.getLong(_columnIndexOfFeedback).toInt()
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item =
              DailyInsightEntity(_tmpDate,_tmpBriefing,_tmpContextSnapshotId,_tmpFeedback,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun relevantMemories(limit: Int): List<MemoryEntity> {
    val _sql: String =
        "SELECT * FROM memories WHERE archived = 0 ORDER BY importance DESC, lastUsedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfText: Int = getColumnIndexOrThrow(_stmt, "text")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _columnIndexOfTags: Int = getColumnIndexOrThrow(_stmt, "tags")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfLastUsedAt: Int = getColumnIndexOrThrow(_stmt, "lastUsedAt")
        val _columnIndexOfArchived: Int = getColumnIndexOrThrow(_stmt, "archived")
        val _result: MutableList<MemoryEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: MemoryEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpText: String
          _tmpText = _stmt.getText(_columnIndexOfText)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          val _tmpTags: String
          _tmpTags = _stmt.getText(_columnIndexOfTags)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpLastUsedAt: Long
          _tmpLastUsedAt = _stmt.getLong(_columnIndexOfLastUsedAt)
          val _tmpArchived: Boolean
          val _tmp: Int
          _tmp = _stmt.getLong(_columnIndexOfArchived).toInt()
          _tmpArchived = _tmp != 0
          _item =
              MemoryEntity(_tmpId,_tmpText,_tmpSource,_tmpImportance,_tmpTags,_tmpCreatedAt,_tmpLastUsedAt,_tmpArchived)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun activePlans(limit: Int): List<PlanEntity> {
    val _sql: String = "SELECT * FROM plans WHERE status != 'done' ORDER BY updatedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfObjective: Int = getColumnIndexOrThrow(_stmt, "objective")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<PlanEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: PlanEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpObjective: String
          _tmpObjective = _stmt.getText(_columnIndexOfObjective)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item = PlanEntity(_tmpId,_tmpTitle,_tmpObjective,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun activeTasks(planIds: List<String>): List<PlanTaskEntity> {
    val _stringBuilder: StringBuilder = StringBuilder()
    _stringBuilder.append("SELECT * FROM plan_tasks WHERE planId IN (")
    val _inputSize: Int = planIds.size
    appendPlaceholders(_stringBuilder, _inputSize)
    _stringBuilder.append(") AND status != 'done' ORDER BY priority ASC, position ASC")
    val _sql: String = _stringBuilder.toString()
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        for (_item: String in planIds) {
          _stmt.bindText(_argIndex, _item)
          _argIndex++
        }
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfPlanId: Int = getColumnIndexOrThrow(_stmt, "planId")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfDetails: Int = getColumnIndexOrThrow(_stmt, "details")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfPriority: Int = getColumnIndexOrThrow(_stmt, "priority")
        val _columnIndexOfPosition: Int = getColumnIndexOrThrow(_stmt, "position")
        val _result: MutableList<PlanTaskEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item_1: PlanTaskEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpPlanId: String
          _tmpPlanId = _stmt.getText(_columnIndexOfPlanId)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpDetails: String
          _tmpDetails = _stmt.getText(_columnIndexOfDetails)
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpPriority: Int
          _tmpPriority = _stmt.getLong(_columnIndexOfPriority).toInt()
          val _tmpPosition: Int
          _tmpPosition = _stmt.getLong(_columnIndexOfPosition).toInt()
          _item_1 =
              PlanTaskEntity(_tmpId,_tmpPlanId,_tmpTitle,_tmpDetails,_tmpStatus,_tmpPriority,_tmpPosition)
          _result.add(_item_1)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recentMessages(sessionId: String, limit: Int):
      List<ChatMessageEntity> {
    val _sql: String =
        "SELECT * FROM chat_messages WHERE sessionId = ? ORDER BY createdAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, sessionId)
        _argIndex = 2
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSessionId: Int = getColumnIndexOrThrow(_stmt, "sessionId")
        val _columnIndexOfRole: Int = getColumnIndexOrThrow(_stmt, "role")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _result: MutableList<ChatMessageEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ChatMessageEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSessionId: String
          _tmpSessionId = _stmt.getText(_columnIndexOfSessionId)
          val _tmpRole: String
          _tmpRole = _stmt.getText(_columnIndexOfRole)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          _item = ChatMessageEntity(_tmpId,_tmpSessionId,_tmpRole,_tmpContent,_tmpCreatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun recentEvents(limit: Int): List<ContextEventEntity> {
    val _sql: String = "SELECT * FROM context_events ORDER BY timestamp DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfSource: Int = getColumnIndexOrThrow(_stmt, "source")
        val _columnIndexOfType: Int = getColumnIndexOrThrow(_stmt, "type")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfContent: Int = getColumnIndexOrThrow(_stmt, "content")
        val _columnIndexOfTimestamp: Int = getColumnIndexOrThrow(_stmt, "timestamp")
        val _columnIndexOfImportance: Int = getColumnIndexOrThrow(_stmt, "importance")
        val _result: MutableList<ContextEventEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: ContextEventEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpSource: String
          _tmpSource = _stmt.getText(_columnIndexOfSource)
          val _tmpType: String
          _tmpType = _stmt.getText(_columnIndexOfType)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpContent: String
          _tmpContent = _stmt.getText(_columnIndexOfContent)
          val _tmpTimestamp: Long
          _tmpTimestamp = _stmt.getLong(_columnIndexOfTimestamp)
          val _tmpImportance: Float
          _tmpImportance = _stmt.getDouble(_columnIndexOfImportance).toFloat()
          _item =
              ContextEventEntity(_tmpId,_tmpSource,_tmpType,_tmpTitle,_tmpContent,_tmpTimestamp,_tmpImportance)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun activeCommitments(limit: Int): List<CommitmentEntity> {
    val _sql: String =
        "SELECT * FROM commitments WHERE status = 'open' ORDER BY updatedAt DESC LIMIT ?"
    return performSuspending(__db, true, false) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, limit.toLong())
        val _columnIndexOfId: Int = getColumnIndexOrThrow(_stmt, "id")
        val _columnIndexOfMeetingId: Int = getColumnIndexOrThrow(_stmt, "meetingId")
        val _columnIndexOfSourceTitle: Int = getColumnIndexOrThrow(_stmt, "sourceTitle")
        val _columnIndexOfTitle: Int = getColumnIndexOrThrow(_stmt, "title")
        val _columnIndexOfOwner: Int = getColumnIndexOrThrow(_stmt, "owner")
        val _columnIndexOfDueText: Int = getColumnIndexOrThrow(_stmt, "dueText")
        val _columnIndexOfEvidence: Int = getColumnIndexOrThrow(_stmt, "evidence")
        val _columnIndexOfConfidence: Int = getColumnIndexOrThrow(_stmt, "confidence")
        val _columnIndexOfStatus: Int = getColumnIndexOrThrow(_stmt, "status")
        val _columnIndexOfCreatedAt: Int = getColumnIndexOrThrow(_stmt, "createdAt")
        val _columnIndexOfUpdatedAt: Int = getColumnIndexOrThrow(_stmt, "updatedAt")
        val _result: MutableList<CommitmentEntity> = mutableListOf()
        while (_stmt.step()) {
          val _item: CommitmentEntity
          val _tmpId: String
          _tmpId = _stmt.getText(_columnIndexOfId)
          val _tmpMeetingId: String
          _tmpMeetingId = _stmt.getText(_columnIndexOfMeetingId)
          val _tmpSourceTitle: String
          _tmpSourceTitle = _stmt.getText(_columnIndexOfSourceTitle)
          val _tmpTitle: String
          _tmpTitle = _stmt.getText(_columnIndexOfTitle)
          val _tmpOwner: String
          _tmpOwner = _stmt.getText(_columnIndexOfOwner)
          val _tmpDueText: String
          _tmpDueText = _stmt.getText(_columnIndexOfDueText)
          val _tmpEvidence: String
          _tmpEvidence = _stmt.getText(_columnIndexOfEvidence)
          val _tmpConfidence: Float
          _tmpConfidence = _stmt.getDouble(_columnIndexOfConfidence).toFloat()
          val _tmpStatus: String
          _tmpStatus = _stmt.getText(_columnIndexOfStatus)
          val _tmpCreatedAt: Long
          _tmpCreatedAt = _stmt.getLong(_columnIndexOfCreatedAt)
          val _tmpUpdatedAt: Long
          _tmpUpdatedAt = _stmt.getLong(_columnIndexOfUpdatedAt)
          _item =
              CommitmentEntity(_tmpId,_tmpMeetingId,_tmpSourceTitle,_tmpTitle,_tmpOwner,_tmpDueText,_tmpEvidence,_tmpConfidence,_tmpStatus,_tmpCreatedAt,_tmpUpdatedAt)
          _result.add(_item)
        }
        _result
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCuration(
    id: String,
    status: String,
    attempts: Int,
    error: String,
  ) {
    val _sql: String =
        "UPDATE curation_queue SET status = ?, attempts = ?, lastError = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, attempts.toLong())
        _argIndex = 3
        _stmt.bindText(_argIndex, error)
        _argIndex = 4
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoMemories() {
    val _sql: String = "DELETE FROM memories WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoPlans() {
    val _sql: String = "DELETE FROM plans WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoTasks() {
    val _sql: String = "DELETE FROM plan_tasks WHERE id LIKE 'demo:%' OR planId LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoEvents() {
    val _sql: String = "DELETE FROM context_events WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoCommitments() {
    val _sql: String = "DELETE FROM commitments WHERE id LIKE 'demo:%' OR meetingId LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoGoals() {
    val _sql: String = "DELETE FROM goals WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoActions() {
    val _sql: String = "DELETE FROM actions WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoGraphNodes() {
    val _sql: String = "DELETE FROM graph_nodes WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoEdges() {
    val _sql: String =
        "DELETE FROM graph_edges WHERE id LIKE 'demo:%' OR fromId LIKE 'demo:%' OR toId LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteDemoCuration() {
    val _sql: String = "DELETE FROM curation_queue WHERE id LIKE 'demo:%'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun archiveMemory(id: String) {
    val _sql: String = "UPDATE memories SET archived = 1 WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateTaskStatus(id: String, status: String) {
    val _sql: String = "UPDATE plan_tasks SET status = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateGraphNodeStatus(
    id: String,
    status: String,
    updatedAt: Long,
  ) {
    val _sql: String =
        "UPDATE graph_nodes SET status = ?, updatedAt = ? WHERE id = ? OR sourceId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _argIndex = 4
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteTasksForPlan(planId: String) {
    val _sql: String = "DELETE FROM plan_tasks WHERE planId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, planId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deletePlanRecord(planId: String) {
    val _sql: String = "DELETE FROM plans WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, planId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteEvent(id: String) {
    val _sql: String = "DELETE FROM context_events WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateCommitmentStatus(
    id: String,
    status: String,
    updatedAt: Long,
  ) {
    val _sql: String = "UPDATE commitments SET status = ?, updatedAt = ? WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, status)
        _argIndex = 2
        _stmt.bindLong(_argIndex, updatedAt)
        _argIndex = 3
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCommitment(id: String) {
    val _sql: String = "DELETE FROM commitments WHERE id = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, id)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun deleteCommitmentsForMeeting(meetingId: String) {
    val _sql: String = "DELETE FROM commitments WHERE meetingId = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindText(_argIndex, meetingId)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun updateInsightFeedback(date: String, feedback: Int) {
    val _sql: String = "UPDATE daily_insights SET feedback = ? WHERE date = ?"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        var _argIndex: Int = 1
        _stmt.bindLong(_argIndex, feedback.toLong())
        _argIndex = 2
        _stmt.bindText(_argIndex, date)
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public override suspend fun clearCalendarEvents() {
    val _sql: String = "DELETE FROM context_events WHERE source = 'calendar'"
    return performSuspending(__db, false, true) { _connection ->
      val _stmt: SQLiteStatement = _connection.prepare(_sql)
      try {
        _stmt.step()
      } finally {
        _stmt.close()
      }
    }
  }

  public companion object {
    public fun getRequiredConverters(): List<KClass<*>> = emptyList()
  }
}
