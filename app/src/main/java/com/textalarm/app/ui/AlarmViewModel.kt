package com.textalarm.app.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.textalarm.app.data.Alarm
import com.textalarm.app.repository.AlarmRepository
import kotlinx.coroutines.launch

class AlarmViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = AlarmRepository(application)
    val allAlarms = repository.getAllAlarms()

    fun insertAlarm(alarm: Alarm) = viewModelScope.launch { repository.insertAlarm(alarm) }
    fun updateAlarm(alarm: Alarm) = viewModelScope.launch { repository.updateAlarm(alarm) }
    fun deleteAlarm(alarm: Alarm) = viewModelScope.launch { repository.deleteAlarm(alarm) }
    fun toggleAlarm(alarm: Alarm, enabled: Boolean) = updateAlarm(alarm.copy(isEnabled = enabled))
}
