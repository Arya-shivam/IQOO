package com.opengranola.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        MeetingEntity::class,
        NotificationEntity::class,
        MemoryEntity::class,
        PlanEntity::class,
        PlanTaskEntity::class,
        ChatSessionEntity::class,
        ChatMessageEntity::class,
        ContextEventEntity::class,
        ContextSnapshotEntity::class,
        CommitmentEntity::class,
        DailyInsightEntity::class,
        GoalEntity::class,
        ActionEntity::class,
        GraphNodeEntity::class,
        GraphEdgeEntity::class,
        CurationQueueEntity::class
    ],
    version = 6,
    exportSchema = false
)
abstract class OpenGranolaDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun notificationDao(): NotificationDao
    abstract fun assistantDao(): AssistantDao

    companion object {
        @Volatile private var instance: OpenGranolaDatabase? = null

        fun get(context: Context): OpenGranolaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                OpenGranolaDatabase::class.java,
                "open_granola.db"
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3, MIGRATION_3_4, MIGRATION_4_5, MIGRATION_5_6).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS notifications (id TEXT NOT NULL PRIMARY KEY, packageName TEXT NOT NULL, appLabel TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, postedAt INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS memories (id TEXT NOT NULL PRIMARY KEY, text TEXT NOT NULL, source TEXT NOT NULL, importance REAL NOT NULL, tags TEXT NOT NULL, createdAt INTEGER NOT NULL, lastUsedAt INTEGER NOT NULL, archived INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS plans (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, objective TEXT NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS plan_tasks (id TEXT NOT NULL PRIMARY KEY, planId TEXT NOT NULL, title TEXT NOT NULL, details TEXT NOT NULL, status TEXT NOT NULL, priority INTEGER NOT NULL, position INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS chat_sessions (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS chat_messages (id TEXT NOT NULL PRIMARY KEY, sessionId TEXT NOT NULL, role TEXT NOT NULL, content TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS context_events (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, type TEXT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, timestamp INTEGER NOT NULL, importance REAL NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS context_snapshots (id TEXT NOT NULL PRIMARY KEY, purpose TEXT NOT NULL, renderedContext TEXT NOT NULL, sourceIds TEXT NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS commitments (id TEXT NOT NULL PRIMARY KEY, meetingId TEXT NOT NULL, sourceTitle TEXT NOT NULL, title TEXT NOT NULL, owner TEXT NOT NULL, dueText TEXT NOT NULL, evidence TEXT NOT NULL, confidence REAL NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE TABLE IF NOT EXISTS daily_insights (date TEXT NOT NULL PRIMARY KEY, briefing TEXT NOT NULL, contextSnapshotId TEXT NOT NULL, feedback INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
            }
        }

        private val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS goals (id TEXT NOT NULL PRIMARY KEY, title TEXT NOT NULL, description TEXT NOT NULL, status TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_goals_status ON goals(status)")
                db.execSQL("CREATE TABLE IF NOT EXISTS actions (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, sourceId TEXT NOT NULL, type TEXT NOT NULL, title TEXT NOT NULL, summary TEXT NOT NULL, tags TEXT NOT NULL, importance REAL NOT NULL, linkStatus TEXT NOT NULL, occurredAt INTEGER NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_actions_source_sourceId ON actions(source, sourceId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_actions_occurredAt ON actions(occurredAt)")
                db.execSQL("CREATE TABLE IF NOT EXISTS graph_edges (id TEXT NOT NULL PRIMARY KEY, fromType TEXT NOT NULL, fromId TEXT NOT NULL, toType TEXT NOT NULL, toId TEXT NOT NULL, type TEXT NOT NULL, confidence REAL NOT NULL, evidence TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_graph_edges_fromType_fromId_toType_toId_type ON graph_edges(fromType, fromId, toType, toId, type)")
                db.execSQL("CREATE TABLE IF NOT EXISTS curation_queue (id TEXT NOT NULL PRIMARY KEY, source TEXT NOT NULL, sourceId TEXT NOT NULL, title TEXT NOT NULL, content TEXT NOT NULL, occurredAt INTEGER NOT NULL, status TEXT NOT NULL, attempts INTEGER NOT NULL, lastError TEXT NOT NULL, createdAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_curation_queue_status_createdAt ON curation_queue(status, createdAt)")
                db.execSQL("INSERT OR IGNORE INTO goals (id, title, description, status, createdAt, updatedAt) SELECT 'goal:' || id, title, objective, status, createdAt, updatedAt FROM plans")
                db.execSQL("INSERT OR IGNORE INTO graph_edges (id, fromType, fromId, toType, toId, type, confidence, evidence, createdAt) SELECT 'plan-goal:' || id, 'plan', id, 'goal', 'goal:' || id, 'derived_from', 1.0, objective, updatedAt FROM plans")
            }
        }

        private val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS graph_nodes (id TEXT NOT NULL PRIMARY KEY, type TEXT NOT NULL, title TEXT NOT NULL, details TEXT NOT NULL, tags TEXT NOT NULL, status TEXT NOT NULL, sourceId TEXT NOT NULL, createdAt INTEGER NOT NULL, updatedAt INTEGER NOT NULL)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_graph_nodes_type ON graph_nodes(type)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_graph_nodes_sourceId ON graph_nodes(sourceId)")
                db.execSQL("INSERT OR IGNORE INTO graph_nodes SELECT id, 'goal', title, description, '', status, id, createdAt, updatedAt FROM goals")
                db.execSQL("INSERT OR IGNORE INTO graph_nodes SELECT 'plan:' || id, 'plan', title, objective, '', status, id, createdAt, updatedAt FROM plans")
                db.execSQL("INSERT OR IGNORE INTO graph_nodes SELECT 'task:' || id, 'task', title, details, '', status, id, 0, 0 FROM plan_tasks")
                db.execSQL("INSERT OR IGNORE INTO graph_nodes SELECT id, 'action', title, summary, tags, linkStatus, sourceId, createdAt, createdAt FROM actions")
                db.execSQL("INSERT OR IGNORE INTO graph_nodes SELECT 'memory:' || id, 'fact', substr(text, 1, 100), text, tags, CASE archived WHEN 1 THEN 'archived' ELSE 'active' END, id, createdAt, lastUsedAt FROM memories")
            }
        }
    }
}
