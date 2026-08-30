package com.opengranola.android.`data`

import androidx.room.InvalidationTracker
import androidx.room.RoomOpenDelegate
import androidx.room.migration.AutoMigrationSpec
import androidx.room.migration.Migration
import androidx.room.util.TableInfo
import androidx.room.util.TableInfo.Companion.read
import androidx.room.util.dropFtsSyncTriggers
import androidx.sqlite.SQLiteConnection
import androidx.sqlite.execSQL
import javax.`annotation`.processing.Generated
import kotlin.Lazy
import kotlin.String
import kotlin.Suppress
import kotlin.collections.List
import kotlin.collections.Map
import kotlin.collections.MutableList
import kotlin.collections.MutableMap
import kotlin.collections.MutableSet
import kotlin.collections.Set
import kotlin.collections.mutableListOf
import kotlin.collections.mutableMapOf
import kotlin.collections.mutableSetOf
import kotlin.reflect.KClass

@Generated(value = ["androidx.room.RoomProcessor"])
@Suppress(names = ["UNCHECKED_CAST", "DEPRECATION", "REDUNDANT_PROJECTION", "REMOVAL"])
public class OpenGranolaDatabase_Impl : OpenGranolaDatabase() {
  private val _meetingDao: Lazy<MeetingDao> = lazy {
    MeetingDao_Impl(this)
  }

  private val _notificationDao: Lazy<NotificationDao> = lazy {
    NotificationDao_Impl(this)
  }

  private val _assistantDao: Lazy<AssistantDao> = lazy {
    AssistantDao_Impl(this)
  }

  protected override fun createOpenDelegate(): RoomOpenDelegate {
    val _openDelegate: RoomOpenDelegate = object : RoomOpenDelegate(6,
        "a47e4f427ec934fc51cde1624acbe016", "21cee3e691cba0bcc3af8ba923c0488c") {
      public override fun createAllTables(connection: SQLiteConnection) {
        connection.execSQL("CREATE TABLE IF NOT EXISTS `meetings` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `startedAt` INTEGER NOT NULL, `transcript` TEXT NOT NULL, `notes` TEXT NOT NULL, `recordingPath` TEXT, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `notifications` (`id` TEXT NOT NULL, `packageName` TEXT NOT NULL, `appLabel` TEXT NOT NULL, `title` TEXT NOT NULL, `body` TEXT NOT NULL, `postedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `memories` (`id` TEXT NOT NULL, `text` TEXT NOT NULL, `source` TEXT NOT NULL, `importance` REAL NOT NULL, `tags` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `lastUsedAt` INTEGER NOT NULL, `archived` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `plans` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `objective` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `plan_tasks` (`id` TEXT NOT NULL, `planId` TEXT NOT NULL, `title` TEXT NOT NULL, `details` TEXT NOT NULL, `status` TEXT NOT NULL, `priority` INTEGER NOT NULL, `position` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chat_sessions` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `chat_messages` (`id` TEXT NOT NULL, `sessionId` TEXT NOT NULL, `role` TEXT NOT NULL, `content` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `context_events` (`id` TEXT NOT NULL, `source` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `timestamp` INTEGER NOT NULL, `importance` REAL NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `context_snapshots` (`id` TEXT NOT NULL, `purpose` TEXT NOT NULL, `renderedContext` TEXT NOT NULL, `sourceIds` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `commitments` (`id` TEXT NOT NULL, `meetingId` TEXT NOT NULL, `sourceTitle` TEXT NOT NULL, `title` TEXT NOT NULL, `owner` TEXT NOT NULL, `dueText` TEXT NOT NULL, `evidence` TEXT NOT NULL, `confidence` REAL NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `daily_insights` (`date` TEXT NOT NULL, `briefing` TEXT NOT NULL, `contextSnapshotId` TEXT NOT NULL, `feedback` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`date`))")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `goals` (`id` TEXT NOT NULL, `title` TEXT NOT NULL, `description` TEXT NOT NULL, `status` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_goals_status` ON `goals` (`status`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `actions` (`id` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `summary` TEXT NOT NULL, `tags` TEXT NOT NULL, `importance` REAL NOT NULL, `linkStatus` TEXT NOT NULL, `occurredAt` INTEGER NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_actions_occurredAt` ON `actions` (`occurredAt`)")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_actions_source_sourceId` ON `actions` (`source`, `sourceId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `graph_nodes` (`id` TEXT NOT NULL, `type` TEXT NOT NULL, `title` TEXT NOT NULL, `details` TEXT NOT NULL, `tags` TEXT NOT NULL, `status` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_graph_nodes_type` ON `graph_nodes` (`type`)")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_graph_nodes_sourceId` ON `graph_nodes` (`sourceId`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `graph_edges` (`id` TEXT NOT NULL, `fromType` TEXT NOT NULL, `fromId` TEXT NOT NULL, `toType` TEXT NOT NULL, `toId` TEXT NOT NULL, `type` TEXT NOT NULL, `confidence` REAL NOT NULL, `evidence` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_graph_edges_fromType_fromId_toType_toId_type` ON `graph_edges` (`fromType`, `fromId`, `toType`, `toId`, `type`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS `curation_queue` (`id` TEXT NOT NULL, `source` TEXT NOT NULL, `sourceId` TEXT NOT NULL, `title` TEXT NOT NULL, `content` TEXT NOT NULL, `occurredAt` INTEGER NOT NULL, `status` TEXT NOT NULL, `attempts` INTEGER NOT NULL, `lastError` TEXT NOT NULL, `createdAt` INTEGER NOT NULL, PRIMARY KEY(`id`))")
        connection.execSQL("CREATE INDEX IF NOT EXISTS `index_curation_queue_status_createdAt` ON `curation_queue` (`status`, `createdAt`)")
        connection.execSQL("CREATE TABLE IF NOT EXISTS room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)")
        connection.execSQL("INSERT OR REPLACE INTO room_master_table (id,identity_hash) VALUES(42, 'a47e4f427ec934fc51cde1624acbe016')")
      }

      public override fun dropAllTables(connection: SQLiteConnection) {
        connection.execSQL("DROP TABLE IF EXISTS `meetings`")
        connection.execSQL("DROP TABLE IF EXISTS `notifications`")
        connection.execSQL("DROP TABLE IF EXISTS `memories`")
        connection.execSQL("DROP TABLE IF EXISTS `plans`")
        connection.execSQL("DROP TABLE IF EXISTS `plan_tasks`")
        connection.execSQL("DROP TABLE IF EXISTS `chat_sessions`")
        connection.execSQL("DROP TABLE IF EXISTS `chat_messages`")
        connection.execSQL("DROP TABLE IF EXISTS `context_events`")
        connection.execSQL("DROP TABLE IF EXISTS `context_snapshots`")
        connection.execSQL("DROP TABLE IF EXISTS `commitments`")
        connection.execSQL("DROP TABLE IF EXISTS `daily_insights`")
        connection.execSQL("DROP TABLE IF EXISTS `goals`")
        connection.execSQL("DROP TABLE IF EXISTS `actions`")
        connection.execSQL("DROP TABLE IF EXISTS `graph_nodes`")
        connection.execSQL("DROP TABLE IF EXISTS `graph_edges`")
        connection.execSQL("DROP TABLE IF EXISTS `curation_queue`")
      }

      public override fun onCreate(connection: SQLiteConnection) {
      }

      public override fun onOpen(connection: SQLiteConnection) {
        internalInitInvalidationTracker(connection)
      }

      public override fun onPreMigrate(connection: SQLiteConnection) {
        dropFtsSyncTriggers(connection)
      }

      public override fun onPostMigrate(connection: SQLiteConnection) {
      }

      public override fun onValidateSchema(connection: SQLiteConnection):
          RoomOpenDelegate.ValidationResult {
        val _columnsMeetings: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMeetings.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeetings.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeetings.put("startedAt", TableInfo.Column("startedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeetings.put("transcript", TableInfo.Column("transcript", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeetings.put("notes", TableInfo.Column("notes", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMeetings.put("recordingPath", TableInfo.Column("recordingPath", "TEXT", false, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMeetings: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMeetings: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMeetings: TableInfo = TableInfo("meetings", _columnsMeetings, _foreignKeysMeetings,
            _indicesMeetings)
        val _existingMeetings: TableInfo = read(connection, "meetings")
        if (!_infoMeetings.equals(_existingMeetings)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |meetings(com.opengranola.android.data.MeetingEntity).
              | Expected:
              |""".trimMargin() + _infoMeetings + """
              |
              | Found:
              |""".trimMargin() + _existingMeetings)
        }
        val _columnsNotifications: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsNotifications.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("packageName", TableInfo.Column("packageName", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("appLabel", TableInfo.Column("appLabel", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("body", TableInfo.Column("body", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsNotifications.put("postedAt", TableInfo.Column("postedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysNotifications: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesNotifications: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoNotifications: TableInfo = TableInfo("notifications", _columnsNotifications,
            _foreignKeysNotifications, _indicesNotifications)
        val _existingNotifications: TableInfo = read(connection, "notifications")
        if (!_infoNotifications.equals(_existingNotifications)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |notifications(com.opengranola.android.data.NotificationEntity).
              | Expected:
              |""".trimMargin() + _infoNotifications + """
              |
              | Found:
              |""".trimMargin() + _existingNotifications)
        }
        val _columnsMemories: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsMemories.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("text", TableInfo.Column("text", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("importance", TableInfo.Column("importance", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("lastUsedAt", TableInfo.Column("lastUsedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsMemories.put("archived", TableInfo.Column("archived", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysMemories: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesMemories: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoMemories: TableInfo = TableInfo("memories", _columnsMemories, _foreignKeysMemories,
            _indicesMemories)
        val _existingMemories: TableInfo = read(connection, "memories")
        if (!_infoMemories.equals(_existingMemories)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |memories(com.opengranola.android.data.MemoryEntity).
              | Expected:
              |""".trimMargin() + _infoMemories + """
              |
              | Found:
              |""".trimMargin() + _existingMemories)
        }
        val _columnsPlans: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlans.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlans.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlans.put("objective", TableInfo.Column("objective", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlans.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlans.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlans.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlans: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlans: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlans: TableInfo = TableInfo("plans", _columnsPlans, _foreignKeysPlans,
            _indicesPlans)
        val _existingPlans: TableInfo = read(connection, "plans")
        if (!_infoPlans.equals(_existingPlans)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |plans(com.opengranola.android.data.PlanEntity).
              | Expected:
              |""".trimMargin() + _infoPlans + """
              |
              | Found:
              |""".trimMargin() + _existingPlans)
        }
        val _columnsPlanTasks: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsPlanTasks.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("planId", TableInfo.Column("planId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("details", TableInfo.Column("details", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("priority", TableInfo.Column("priority", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsPlanTasks.put("position", TableInfo.Column("position", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysPlanTasks: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesPlanTasks: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoPlanTasks: TableInfo = TableInfo("plan_tasks", _columnsPlanTasks,
            _foreignKeysPlanTasks, _indicesPlanTasks)
        val _existingPlanTasks: TableInfo = read(connection, "plan_tasks")
        if (!_infoPlanTasks.equals(_existingPlanTasks)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |plan_tasks(com.opengranola.android.data.PlanTaskEntity).
              | Expected:
              |""".trimMargin() + _infoPlanTasks + """
              |
              | Found:
              |""".trimMargin() + _existingPlanTasks)
        }
        val _columnsChatSessions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChatSessions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatSessions.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatSessions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsChatSessions.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChatSessions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChatSessions: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChatSessions: TableInfo = TableInfo("chat_sessions", _columnsChatSessions,
            _foreignKeysChatSessions, _indicesChatSessions)
        val _existingChatSessions: TableInfo = read(connection, "chat_sessions")
        if (!_infoChatSessions.equals(_existingChatSessions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chat_sessions(com.opengranola.android.data.ChatSessionEntity).
              | Expected:
              |""".trimMargin() + _infoChatSessions + """
              |
              | Found:
              |""".trimMargin() + _existingChatSessions)
        }
        val _columnsChatMessages: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsChatMessages.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("sessionId", TableInfo.Column("sessionId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("role", TableInfo.Column("role", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsChatMessages.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysChatMessages: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesChatMessages: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoChatMessages: TableInfo = TableInfo("chat_messages", _columnsChatMessages,
            _foreignKeysChatMessages, _indicesChatMessages)
        val _existingChatMessages: TableInfo = read(connection, "chat_messages")
        if (!_infoChatMessages.equals(_existingChatMessages)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |chat_messages(com.opengranola.android.data.ChatMessageEntity).
              | Expected:
              |""".trimMargin() + _infoChatMessages + """
              |
              | Found:
              |""".trimMargin() + _existingChatMessages)
        }
        val _columnsContextEvents: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsContextEvents.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("timestamp", TableInfo.Column("timestamp", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContextEvents.put("importance", TableInfo.Column("importance", "REAL", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysContextEvents: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesContextEvents: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoContextEvents: TableInfo = TableInfo("context_events", _columnsContextEvents,
            _foreignKeysContextEvents, _indicesContextEvents)
        val _existingContextEvents: TableInfo = read(connection, "context_events")
        if (!_infoContextEvents.equals(_existingContextEvents)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |context_events(com.opengranola.android.data.ContextEventEntity).
              | Expected:
              |""".trimMargin() + _infoContextEvents + """
              |
              | Found:
              |""".trimMargin() + _existingContextEvents)
        }
        val _columnsContextSnapshots: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsContextSnapshots.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextSnapshots.put("purpose", TableInfo.Column("purpose", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsContextSnapshots.put("renderedContext", TableInfo.Column("renderedContext", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContextSnapshots.put("sourceIds", TableInfo.Column("sourceIds", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsContextSnapshots.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysContextSnapshots: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesContextSnapshots: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoContextSnapshots: TableInfo = TableInfo("context_snapshots",
            _columnsContextSnapshots, _foreignKeysContextSnapshots, _indicesContextSnapshots)
        val _existingContextSnapshots: TableInfo = read(connection, "context_snapshots")
        if (!_infoContextSnapshots.equals(_existingContextSnapshots)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |context_snapshots(com.opengranola.android.data.ContextSnapshotEntity).
              | Expected:
              |""".trimMargin() + _infoContextSnapshots + """
              |
              | Found:
              |""".trimMargin() + _existingContextSnapshots)
        }
        val _columnsCommitments: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCommitments.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("meetingId", TableInfo.Column("meetingId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("sourceTitle", TableInfo.Column("sourceTitle", "TEXT", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("owner", TableInfo.Column("owner", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("dueText", TableInfo.Column("dueText", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("evidence", TableInfo.Column("evidence", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("confidence", TableInfo.Column("confidence", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCommitments.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCommitments: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCommitments: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoCommitments: TableInfo = TableInfo("commitments", _columnsCommitments,
            _foreignKeysCommitments, _indicesCommitments)
        val _existingCommitments: TableInfo = read(connection, "commitments")
        if (!_infoCommitments.equals(_existingCommitments)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |commitments(com.opengranola.android.data.CommitmentEntity).
              | Expected:
              |""".trimMargin() + _infoCommitments + """
              |
              | Found:
              |""".trimMargin() + _existingCommitments)
        }
        val _columnsDailyInsights: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsDailyInsights.put("date", TableInfo.Column("date", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyInsights.put("briefing", TableInfo.Column("briefing", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyInsights.put("contextSnapshotId", TableInfo.Column("contextSnapshotId", "TEXT",
            true, 0, null, TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyInsights.put("feedback", TableInfo.Column("feedback", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsDailyInsights.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysDailyInsights: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesDailyInsights: MutableSet<TableInfo.Index> = mutableSetOf()
        val _infoDailyInsights: TableInfo = TableInfo("daily_insights", _columnsDailyInsights,
            _foreignKeysDailyInsights, _indicesDailyInsights)
        val _existingDailyInsights: TableInfo = read(connection, "daily_insights")
        if (!_infoDailyInsights.equals(_existingDailyInsights)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |daily_insights(com.opengranola.android.data.DailyInsightEntity).
              | Expected:
              |""".trimMargin() + _infoDailyInsights + """
              |
              | Found:
              |""".trimMargin() + _existingDailyInsights)
        }
        val _columnsGoals: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGoals.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("description", TableInfo.Column("description", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGoals.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGoals: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGoals: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGoals.add(TableInfo.Index("index_goals_status", false, listOf("status"),
            listOf("ASC")))
        val _infoGoals: TableInfo = TableInfo("goals", _columnsGoals, _foreignKeysGoals,
            _indicesGoals)
        val _existingGoals: TableInfo = read(connection, "goals")
        if (!_infoGoals.equals(_existingGoals)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |goals(com.opengranola.android.data.GoalEntity).
              | Expected:
              |""".trimMargin() + _infoGoals + """
              |
              | Found:
              |""".trimMargin() + _existingGoals)
        }
        val _columnsActions: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsActions.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("sourceId", TableInfo.Column("sourceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("summary", TableInfo.Column("summary", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("importance", TableInfo.Column("importance", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("linkStatus", TableInfo.Column("linkStatus", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("occurredAt", TableInfo.Column("occurredAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsActions.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysActions: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesActions: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesActions.add(TableInfo.Index("index_actions_occurredAt", false, listOf("occurredAt"),
            listOf("ASC")))
        _indicesActions.add(TableInfo.Index("index_actions_source_sourceId", true, listOf("source",
            "sourceId"), listOf("ASC", "ASC")))
        val _infoActions: TableInfo = TableInfo("actions", _columnsActions, _foreignKeysActions,
            _indicesActions)
        val _existingActions: TableInfo = read(connection, "actions")
        if (!_infoActions.equals(_existingActions)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |actions(com.opengranola.android.data.ActionEntity).
              | Expected:
              |""".trimMargin() + _infoActions + """
              |
              | Found:
              |""".trimMargin() + _existingActions)
        }
        val _columnsGraphNodes: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGraphNodes.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("details", TableInfo.Column("details", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("tags", TableInfo.Column("tags", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("sourceId", TableInfo.Column("sourceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphNodes.put("updatedAt", TableInfo.Column("updatedAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGraphNodes: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGraphNodes: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGraphNodes.add(TableInfo.Index("index_graph_nodes_type", false, listOf("type"),
            listOf("ASC")))
        _indicesGraphNodes.add(TableInfo.Index("index_graph_nodes_sourceId", false,
            listOf("sourceId"), listOf("ASC")))
        val _infoGraphNodes: TableInfo = TableInfo("graph_nodes", _columnsGraphNodes,
            _foreignKeysGraphNodes, _indicesGraphNodes)
        val _existingGraphNodes: TableInfo = read(connection, "graph_nodes")
        if (!_infoGraphNodes.equals(_existingGraphNodes)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |graph_nodes(com.opengranola.android.data.GraphNodeEntity).
              | Expected:
              |""".trimMargin() + _infoGraphNodes + """
              |
              | Found:
              |""".trimMargin() + _existingGraphNodes)
        }
        val _columnsGraphEdges: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsGraphEdges.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("fromType", TableInfo.Column("fromType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("fromId", TableInfo.Column("fromId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("toType", TableInfo.Column("toType", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("toId", TableInfo.Column("toId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("type", TableInfo.Column("type", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("confidence", TableInfo.Column("confidence", "REAL", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("evidence", TableInfo.Column("evidence", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsGraphEdges.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysGraphEdges: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesGraphEdges: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesGraphEdges.add(TableInfo.Index("index_graph_edges_fromType_fromId_toType_toId_type",
            true, listOf("fromType", "fromId", "toType", "toId", "type"), listOf("ASC", "ASC",
            "ASC", "ASC", "ASC")))
        val _infoGraphEdges: TableInfo = TableInfo("graph_edges", _columnsGraphEdges,
            _foreignKeysGraphEdges, _indicesGraphEdges)
        val _existingGraphEdges: TableInfo = read(connection, "graph_edges")
        if (!_infoGraphEdges.equals(_existingGraphEdges)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |graph_edges(com.opengranola.android.data.GraphEdgeEntity).
              | Expected:
              |""".trimMargin() + _infoGraphEdges + """
              |
              | Found:
              |""".trimMargin() + _existingGraphEdges)
        }
        val _columnsCurationQueue: MutableMap<String, TableInfo.Column> = mutableMapOf()
        _columnsCurationQueue.put("id", TableInfo.Column("id", "TEXT", true, 1, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("source", TableInfo.Column("source", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("sourceId", TableInfo.Column("sourceId", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("title", TableInfo.Column("title", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("content", TableInfo.Column("content", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("occurredAt", TableInfo.Column("occurredAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("status", TableInfo.Column("status", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("attempts", TableInfo.Column("attempts", "INTEGER", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("lastError", TableInfo.Column("lastError", "TEXT", true, 0, null,
            TableInfo.CREATED_FROM_ENTITY))
        _columnsCurationQueue.put("createdAt", TableInfo.Column("createdAt", "INTEGER", true, 0,
            null, TableInfo.CREATED_FROM_ENTITY))
        val _foreignKeysCurationQueue: MutableSet<TableInfo.ForeignKey> = mutableSetOf()
        val _indicesCurationQueue: MutableSet<TableInfo.Index> = mutableSetOf()
        _indicesCurationQueue.add(TableInfo.Index("index_curation_queue_status_createdAt", false,
            listOf("status", "createdAt"), listOf("ASC", "ASC")))
        val _infoCurationQueue: TableInfo = TableInfo("curation_queue", _columnsCurationQueue,
            _foreignKeysCurationQueue, _indicesCurationQueue)
        val _existingCurationQueue: TableInfo = read(connection, "curation_queue")
        if (!_infoCurationQueue.equals(_existingCurationQueue)) {
          return RoomOpenDelegate.ValidationResult(false, """
              |curation_queue(com.opengranola.android.data.CurationQueueEntity).
              | Expected:
              |""".trimMargin() + _infoCurationQueue + """
              |
              | Found:
              |""".trimMargin() + _existingCurationQueue)
        }
        return RoomOpenDelegate.ValidationResult(true, null)
      }
    }
    return _openDelegate
  }

  protected override fun createInvalidationTracker(): InvalidationTracker {
    val _shadowTablesMap: MutableMap<String, String> = mutableMapOf()
    val _viewTables: MutableMap<String, Set<String>> = mutableMapOf()
    return InvalidationTracker(this, _shadowTablesMap, _viewTables, "meetings", "notifications",
        "memories", "plans", "plan_tasks", "chat_sessions", "chat_messages", "context_events",
        "context_snapshots", "commitments", "daily_insights", "goals", "actions", "graph_nodes",
        "graph_edges", "curation_queue")
  }

  public override fun clearAllTables() {
    super.performClear(false, "meetings", "notifications", "memories", "plans", "plan_tasks",
        "chat_sessions", "chat_messages", "context_events", "context_snapshots", "commitments",
        "daily_insights", "goals", "actions", "graph_nodes", "graph_edges", "curation_queue")
  }

  protected override fun getRequiredTypeConverterClasses(): Map<KClass<*>, List<KClass<*>>> {
    val _typeConvertersMap: MutableMap<KClass<*>, List<KClass<*>>> = mutableMapOf()
    _typeConvertersMap.put(MeetingDao::class, MeetingDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(NotificationDao::class, NotificationDao_Impl.getRequiredConverters())
    _typeConvertersMap.put(AssistantDao::class, AssistantDao_Impl.getRequiredConverters())
    return _typeConvertersMap
  }

  public override fun getRequiredAutoMigrationSpecClasses(): Set<KClass<out AutoMigrationSpec>> {
    val _autoMigrationSpecsSet: MutableSet<KClass<out AutoMigrationSpec>> = mutableSetOf()
    return _autoMigrationSpecsSet
  }

  public override
      fun createAutoMigrations(autoMigrationSpecs: Map<KClass<out AutoMigrationSpec>, AutoMigrationSpec>):
      List<Migration> {
    val _autoMigrations: MutableList<Migration> = mutableListOf()
    return _autoMigrations
  }

  public override fun meetingDao(): MeetingDao = _meetingDao.value

  public override fun notificationDao(): NotificationDao = _notificationDao.value

  public override fun assistantDao(): AssistantDao = _assistantDao.value
}
