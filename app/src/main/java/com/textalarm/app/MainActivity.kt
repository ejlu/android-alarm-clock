package com.textalarm.app

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Pattern

class MainActivity : AppCompatActivity() {
    
    private lateinit var timeInput: EditText
    private lateinit var setAlarmButton: Button
    private lateinit var statusText: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        timeInput = findViewById(R.id.timeInput)
        setAlarmButton = findViewById(R.id.setAlarmButton)
        statusText = findViewById(R.id.statusText)
        
        setAlarmButton.setOnClickListener {
            val timeText = timeInput.text.toString().trim()
            if (timeText.isNotEmpty()) {
                setAlarm(timeText)
            } else {
                Toast.makeText(this, getString(R.string.invalid_time), Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun setAlarm(timeText: String) {
        try {
            val alarmTime = parseTime(timeText)
            if (alarmTime != null) {
                scheduleAlarm(alarmTime)
                val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
                statusText.text = getString(R.string.alarm_set, timeFormat.format(alarmTime))
                statusText.visibility = TextView.VISIBLE
                timeInput.text.clear()
            } else {
                Toast.makeText(this, getString(R.string.invalid_time), Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, getString(R.string.invalid_time), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun parseTime(timeText: String): Date? {
        val lowerText = timeText.lowercase().replace(" ", "")
        
        val patterns = listOf(
            Pattern.compile("(\\d{1,2}):(\\d{2})(am|pm)"),
            Pattern.compile("(\\d{1,2})(am|pm)"),
            Pattern.compile("(\\d{1,2}):(\\d{2})"),
            Pattern.compile("(\\d{3,4})")
        )
        
        val calendar = Calendar.getInstance()
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(lowerText)
            if (matcher.matches()) {
                when (pattern.pattern()) {
                    "(\\d{1,2}):(\\d{2})(am|pm)" -> {
                        var hour = matcher.group(1)!!.toInt()
                        val minute = matcher.group(2)!!.toInt()
                        val ampm = matcher.group(3)!!
                        
                        if (ampm == "pm" && hour != 12) hour += 12
                        if (ampm == "am" && hour == 12) hour = 0
                        
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                    }
                    "(\\d{1,2})(am|pm)" -> {
                        var hour = matcher.group(1)!!.toInt()
                        val ampm = matcher.group(2)!!
                        
                        if (ampm == "pm" && hour != 12) hour += 12
                        if (ampm == "am" && hour == 12) hour = 0
                        
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, 0)
                    }
                    "(\\d{1,2}):(\\d{2})" -> {
                        val hour = matcher.group(1)!!.toInt()
                        val minute = matcher.group(2)!!.toInt()
                        
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                    }
                    "(\\d{3,4})" -> {
                        val timeStr = matcher.group(1)!!
                        val hour = if (timeStr.length == 3) {
                            timeStr.substring(0, 1).toInt()
                        } else {
                            timeStr.substring(0, 2).toInt()
                        }
                        val minute = timeStr.substring(timeStr.length - 2).toInt()
                        
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                    }
                }
                
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                
                if (calendar.timeInMillis <= System.currentTimeMillis()) {
                    calendar.add(Calendar.DAY_OF_YEAR, 1)
                }
                
                return calendar.time
            }
        }
        
        return null
    }
    
    private fun scheduleAlarm(alarmTime: Date) {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        alarmManager.setExact(AlarmManager.RTC_WAKEUP, alarmTime.time, pendingIntent)
    }
}