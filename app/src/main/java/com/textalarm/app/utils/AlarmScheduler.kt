package com.textalarm.app.utils

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.textalarm.app.AlarmReceiver
import com.textalarm.app.MainActivity

class AlarmScheduler(private val context: Context) {
    private val manager = context.getSystemService(AlarmManager::class.java)
    private val flags = PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT

    fun canSchedule(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.S || manager.canScheduleExactAlarms()

    fun schedule(alarmId: Long, triggerAt: Long) {
        if (!canSchedule()) return // The permission-granted broadcast restores saved alarms.
        val show = PendingIntent.getActivity(context, 0, Intent(context, MainActivity::class.java), flags)
        val intent = Intent(context, AlarmReceiver::class.java)
            .putExtra("alarm_id", alarmId)
            .putExtra("trigger_at", triggerAt)
        val delivery = PendingIntent.getBroadcast(context, alarmId.toInt(), intent, flags)
        manager.setAlarmClock(AlarmManager.AlarmClockInfo(triggerAt, show), delivery)
    }

    fun cancelAlarm(alarmId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        val delivery = PendingIntent.getBroadcast(context, alarmId.toInt(), intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE) ?: return
        manager.cancel(delivery)
        delivery.cancel()
    }
}
