package com.opengranola.android.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(entities = [MeetingEntity::class], version = 1, exportSchema = false)
abstract class OpenGranolaDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao

    companion object {
        @Volatile private var instance: OpenGranolaDatabase? = null

        fun get(context: Context): OpenGranolaDatabase = instance ?: synchronized(this) {
            instance ?: Room.databaseBuilder(
                context.applicationContext,
                OpenGranolaDatabase::class.java,
                "open_granola.db"
            ).build().also { instance = it }
        }
    }
}
