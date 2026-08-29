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
        ContextSnapshotEntity::class
    ],
    version = 3,
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
            ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build().also { instance = it }
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
    }
}
