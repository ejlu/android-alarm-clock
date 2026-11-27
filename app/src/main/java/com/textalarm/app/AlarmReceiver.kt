package com.textalarm.app

import android.app.AlarmManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat
import com.textalarm.app.data.AlarmDatabase
import com.textalarm.app.repository.AlarmRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (!AlarmDatabase.canReadAlarms(context)) return@launch
                val repository = AlarmRepository(context)
                when (intent.action) {
                    Intent.ACTION_LOCKED_BOOT_COMPLETED, Intent.ACTION_BOOT_COMPLETED,
                    AlarmManager.ACTION_SCHEDULE_EXACT_ALARM_PERMISSION_STATE_CHANGED ->
                        repository.restoreAlarms()
                    null -> {
                        val id = intent.getLongExtra("alarm_id", -1)
                        val expected = intent.getLongExtra("trigger_at", -1).takeIf { it >= 0 }
                        val alarm = repository.consumeAlarm(id, expected) ?: return@launch
                        val ring = Intent(context, RingingService::class.java)
                            .putExtra("label", alarm.label)
                            .putExtra("vibrate", alarm.vibrate)
                        ContextCompat.startForegroundService(context, ring)
                    }
                }
            } finally {
                pending.finish()
            }
        }
    }
}
