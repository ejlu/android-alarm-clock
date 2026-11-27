package com.textalarm.app.data

import android.content.Context
import android.os.UserManager
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [Alarm::class],
    version = 2,
    exportSchema = false
)
abstract class AlarmDatabase : RoomDatabase() {
    abstract fun alarmDao(): AlarmDao
    
    companion object {
        @Volatile
        private var INSTANCE: AlarmDatabase? = null
        
        private const val NAME = "alarm_database"

        fun canReadAlarms(context: Context): Boolean =
            context.getSystemService(UserManager::class.java).isUserUnlocked ||
                context.createDeviceProtectedStorageContext().getDatabasePath(NAME).exists()

        fun getDatabase(context: Context): AlarmDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: openDatabase(context.applicationContext).also { INSTANCE = it }
            }

        private fun openDatabase(context: Context): AlarmDatabase {
            val storage = context.createDeviceProtectedStorageContext()
            if (context.getSystemService(UserManager::class.java).isUserUnlocked &&
                context.getDatabasePath(NAME).exists()) {
                check(storage.moveDatabaseFrom(context, NAME)) { "Could not move alarm database" }
            }
            return Room.databaseBuilder(storage, AlarmDatabase::class.java, NAME)
                .addMigrations(object : Migration(1, 2) {
                    override fun migrate(db: SupportSQLiteDatabase) {
                        db.execSQL("ALTER TABLE alarms ADD COLUMN nextTriggerAt INTEGER")
                    }
                })
                .build()
        }
    }
}
