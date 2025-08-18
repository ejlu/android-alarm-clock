package com.textalarm.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import com.textalarm.app.data.Alarm
import com.textalarm.app.data.AlarmDatabase
import com.textalarm.app.repository.AlarmRepository
import com.textalarm.app.utils.AlarmScheduler
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    
    private val repository: AlarmRepository
    private val alarmScheduler: AlarmScheduler
    
    val allAlarms: LiveData<List<Alarm>>
    
    init {
        val alarmDao = AlarmDatabase.getDatabase(application).alarmDao()
        repository = AlarmRepository(alarmDao)
        alarmScheduler = AlarmScheduler(application)
        allAlarms = repository.getAllAlarms()
    }
    
    fun insertAlarm(alarm: Alarm) = viewModelScope.launch {
        val alarmId = repository.insertAlarm(alarm)
        if (alarm.isEnabled) {
            val alarmWithId = alarm.copy(id = alarmId)
            alarmScheduler.scheduleAlarm(alarmWithId)
        }
    }
    
    fun updateAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.updateAlarm(alarm)
        alarmScheduler.cancelAlarm(alarm.id)
        if (alarm.isEnabled) {
            alarmScheduler.scheduleAlarm(alarm)
        }
    }
    
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch {
        repository.deleteAlarm(alarm)
        alarmScheduler.cancelAlarm(alarm.id)
    }
    
    fun toggleAlarm(alarm: Alarm, enabled: Boolean) = viewModelScope.launch {
        val updatedAlarm = alarm.copy(isEnabled = enabled)
        repository.updateAlarm(updatedAlarm)
        
        if (enabled) {
            alarmScheduler.scheduleAlarm(updatedAlarm)
        } else {
            alarmScheduler.cancelAlarm(alarm.id)
        }
    }
    
    suspend fun getAlarmById(id: Long): Alarm? {
        return repository.getAlarmById(id)
    }
}