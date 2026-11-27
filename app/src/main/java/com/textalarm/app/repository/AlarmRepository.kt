package com.textalarm.app.repository

import android.content.Context
import androidx.room.withTransaction
import com.textalarm.app.data.Alarm
import com.textalarm.app.data.AlarmDatabase
import com.textalarm.app.utils.AlarmScheduler

class AlarmRepository(context: Context) {
    private val db = AlarmDatabase.getDatabase(context)
    private val dao = db.alarmDao()
    private val scheduler = AlarmScheduler(context)

    fun getAllAlarms() = dao.getAllAlarms()

    suspend fun insertAlarm(alarm: Alarm) = db.withTransaction {
        val next = if (alarm.isEnabled) alarm.getNextAlarmTime() else null
        val id = dao.insertAlarm(alarm.copy(nextTriggerAt = next))
        if (next != null) scheduler.schedule(id, next)
    }

    suspend fun updateAlarm(alarm: Alarm) = db.withTransaction {
        val next = if (alarm.isEnabled) alarm.getNextAlarmTime() else null
        dao.updateAlarm(alarm.copy(nextTriggerAt = next))
        if (next != null) scheduler.schedule(alarm.id, next) else scheduler.cancelAlarm(alarm.id)
    }

    suspend fun deleteAlarm(alarm: Alarm) = db.withTransaction {
        dao.deleteAlarm(alarm)
        scheduler.cancelAlarm(alarm.id)
    }

    suspend fun consumeAlarm(id: Long, expectedTime: Long?): Alarm? = db.withTransaction {
        val alarm = dao.getAlarmById(id) ?: return@withTransaction null
        if (!alarm.isEnabled || (expectedTime != null && expectedTime != alarm.nextTriggerAt)) {
            return@withTransaction null
        }
        if (alarm.isRecurring) {
            val next = alarm.getNextAlarmTime()
            dao.updateAlarm(alarm.copy(nextTriggerAt = next))
            scheduler.schedule(id, next)
        } else {
            dao.updateAlarm(alarm.copy(isEnabled = false, nextTriggerAt = null))
        }
        alarm
    }

    suspend fun restoreAlarms() = db.withTransaction {
        val now = System.currentTimeMillis()
        for (alarm in dao.getEnabledAlarms()) {
            val saved = alarm.nextTriggerAt
            if (saved != null && saved <= now && !alarm.isRecurring) {
                dao.updateAlarm(alarm.copy(isEnabled = false, nextTriggerAt = null))
                continue
            }
            val next = saved?.takeIf { it > now } ?: alarm.getNextAlarmTime(now)
            if (next != saved) dao.updateAlarm(alarm.copy(nextTriggerAt = next))
            scheduler.schedule(alarm.id, next)
        }
    }
}
