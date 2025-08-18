package com.textalarm.app.repository

import androidx.lifecycle.LiveData
import com.textalarm.app.data.Alarm
import com.textalarm.app.data.AlarmDao
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AlarmRepository(private val alarmDao: AlarmDao) {
    
    fun getAllAlarms(): LiveData<List<Alarm>> = alarmDao.getAllAlarms()
    
    suspend fun getAlarmById(id: Long): Alarm? = withContext(Dispatchers.IO) {
        alarmDao.getAlarmById(id)
    }
    
    suspend fun getEnabledAlarms(): List<Alarm> = withContext(Dispatchers.IO) {
        alarmDao.getEnabledAlarms()
    }
    
    suspend fun insertAlarm(alarm: Alarm): Long = withContext(Dispatchers.IO) {
        alarmDao.insertAlarm(alarm)
    }
    
    suspend fun updateAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        alarmDao.updateAlarm(alarm)
    }
    
    suspend fun deleteAlarm(alarm: Alarm) = withContext(Dispatchers.IO) {
        alarmDao.deleteAlarm(alarm)
    }
    
    suspend fun updateAlarmEnabled(id: Long, enabled: Boolean) = withContext(Dispatchers.IO) {
        alarmDao.updateAlarmEnabled(id, enabled)
    }
    
    suspend fun deleteAlarmById(id: Long) = withContext(Dispatchers.IO) {
        alarmDao.deleteAlarmById(id)
    }
}