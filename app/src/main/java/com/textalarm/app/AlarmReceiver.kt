package com.textalarm.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.RingtoneManager
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.core.app.NotificationCompat

class AlarmReceiver : BroadcastReceiver() {
    
    override fun onReceive(context: Context, intent: Intent) {
        val alarmId = intent.getLongExtra("alarm_id", -1)
        val alarmLabel = intent.getStringExtra("alarm_label") ?: "Alarm"
        val isRecurring = intent.getBooleanExtra("is_recurring", false)
        
        showNotification(context, alarmLabel)
        vibrateDevice(context)
        
        // If it's a recurring alarm, reschedule it for the next occurrence
        if (isRecurring && alarmId != -1L) {
            rescheduleRecurringAlarm(context, alarmId)
        }
    }
    
    private fun rescheduleRecurringAlarm(context: Context, alarmId: Long) {
        // We'll need to get the alarm from the database and reschedule it
        // This would require access to the repository, which we'll implement if needed
        // For now, the AlarmScheduler handles the next occurrence calculation
    }
    
    private fun showNotification(context: Context, alarmLabel: String = "Alarm") {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        val channelId = "alarm_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarm Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            channel.description = "Notifications for alarms"
            notificationManager.createNotificationChannel(channel)
        }
        
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(alarmLabel)
            .setContentText(context.getString(R.string.alarm_message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            .build()
        
        notificationManager.notify(1, notification)
    }
    
    private fun vibrateDevice(context: Context) {
        val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val vibrationEffect = VibrationEffect.createWaveform(
                longArrayOf(0, 500, 200, 500, 200, 500),
                -1
            )
            vibrator.vibrate(vibrationEffect)
        } else {
            @Suppress("DEPRECATION")
            vibrator.vibrate(longArrayOf(0, 500, 200, 500, 200, 500), -1)
        }
    }
}