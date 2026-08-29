package com.opengranola.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [MeetingEntity::class, NotificationEntity::class], version = 2, exportSchema = false)
abstract class OpenGranolaDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun notificationDao(): NotificationDao

    companion object {
        @Volatile private var instance: OpenGranolaDatabase? = null

        fun get(context: Context): OpenGranolaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                OpenGranolaDatabase::class.java,
                "open_granola.db"
            ).addMigrations(MIGRATION_1_2).build().also { instance = it }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL("CREATE TABLE IF NOT EXISTS notifications (id TEXT NOT NULL PRIMARY KEY, packageName TEXT NOT NULL, appLabel TEXT NOT NULL, title TEXT NOT NULL, body TEXT NOT NULL, postedAt INTEGER NOT NULL)")
            }
        }
    }
}
