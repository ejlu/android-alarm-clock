package com.textalarm.app

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.VibrationEffect
import android.os.Vibrator
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import java.io.IOException

class RingingService : Service() {
    private var player: MediaPlayer? = null
    private val handler = Handler(Looper.getMainLooper())
    private val timeout = Runnable { stopSelf() }
    private val vibrator by lazy { getSystemService(Vibrator::class.java) }
    private val audio = AudioAttributes.Builder()
        .setUsage(AudioAttributes.USAGE_ALARM)
        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
        .build()

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent == null || intent.action == DISMISS) {
            stopSelf()
            return START_NOT_STICKY
        }
        val label = intent.getStringExtra("label") ?: getString(R.string.alarm_title)
        startForeground(1, notification(label))
        ringing.value = true
        if (player == null) player = startPlayback()
        vibrate(intent.getBooleanExtra("vibrate", true))
        handler.removeCallbacks(timeout)
        handler.postDelayed(timeout, 10 * 60_000L)
        return START_NOT_STICKY
    }

    private fun notification(label: String): Notification {
        val channelId = "ringing_alarms"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, getString(R.string.app_name),
                NotificationManager.IMPORTANCE_HIGH).apply { setSound(null, null) }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
        val immutable = PendingIntent.FLAG_IMMUTABLE
        val open = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java), immutable)
        val dismiss = PendingIntent.getService(this, 0,
            Intent(this, RingingService::class.java).setAction(DISMISS), immutable)
        return NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_lock_idle_alarm)
            .setContentTitle(label)
            .setContentText(getString(R.string.alarm_message))
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .setContentIntent(open)
            .setDeleteIntent(dismiss)
            .setOngoing(true)
            .addAction(0, getString(R.string.dismiss_alarm), dismiss)
            .build()
    }

    private fun startPlayback(): MediaPlayer = MediaPlayer().apply {
        setAudioAttributes(audio)
        try {
            setDataSource(this@RingingService, RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM))
            prepare()
        } catch (e: Exception) {
            if (e !is IOException && e !is SecurityException && e !is IllegalArgumentException) throw e
            Log.w("RingingService", "Default alarm tone unavailable, using bundled chime", e)
            reset()
            setAudioAttributes(audio)
            resources.openRawResourceFd(R.raw.alarm_chime).use {
                setDataSource(it.fileDescriptor, it.startOffset, it.length)
            }
            prepare()
        }
        setWakeMode(this@RingingService, PowerManager.PARTIAL_WAKE_LOCK)
        isLooping = true
        start()
    }

    private fun vibrate(enabled: Boolean) {
        if (enabled) {
            val pattern = longArrayOf(0, 500, 300, 500, 1000)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, 0), audio)
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, 0, audio)
            }
        } else {
            vibrator.cancel()
        }
    }

    override fun onDestroy() {
        handler.removeCallbacks(timeout)
        player?.release()
        vibrator.cancel()
        ringing.value = false
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    companion object {
        private const val DISMISS = "com.textalarm.app.DISMISS"
        private val ringing = MutableLiveData(false)
        val isRinging: LiveData<Boolean> = ringing
    }
}
