package com.textalarm.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.text.SimpleDateFormat
import java.util.*

@Entity(tableName = "alarms")
data class Alarm(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val hour: Int,
    val minute: Int,
    val label: String = "",
    val isEnabled: Boolean = true,
    val isRecurring: Boolean = false,
    val daysOfWeek: Int = 0, // Bitmap: Sunday=1, Monday=2, Tuesday=4, etc.
    val soundUri: String? = null,
    val vibrate: Boolean = true,
    val createdAt: Long = System.currentTimeMillis(),
    val nextTriggerAt: Long? = null
) {
    fun getTimeString(): String {
        val time = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
        }
        return SimpleDateFormat("h:mm a", Locale.getDefault()).format(time.time)
    }

    fun getNextAlarmTime(now: Long = System.currentTimeMillis()): Long {
        val day = Calendar.getInstance().apply { timeInMillis = now }
        val selectedDays = if (isRecurring && daysOfWeek and 127 != 0) daysOfWeek else 127
        while (true) {
            val candidate = (day.clone() as Calendar).apply {
                set(Calendar.HOUR_OF_DAY, hour)
                set(Calendar.MINUTE, minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            val bit = 1 shl (day.get(Calendar.DAY_OF_WEEK) - 1)
            if (selectedDays and bit != 0 && candidate.timeInMillis > now) {
                return candidate.timeInMillis
            }
            day.add(Calendar.DATE, 1)
        }
    }

    fun getDaysOfWeekString(): String {
        if (!isRecurring) return "Once"
        
        val days = mutableListOf<String>()
        val dayNames = arrayOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        
        for (i in 0..6) {
            if (daysOfWeek and (1 shl i) != 0) {
                days.add(dayNames[i])
            }
        }
        
        return when {
            days.size == 7 -> "Every day"
            days.size == 5 && !days.contains("Sun") && !days.contains("Sat") -> "Weekdays"
            days.size == 2 && days.contains("Sun") && days.contains("Sat") -> "Weekends"
            else -> days.joinToString(", ")
        }
    }
    
    companion object {
        const val SUNDAY = 1
        const val MONDAY = 2
        const val TUESDAY = 4
        const val WEDNESDAY = 8
        const val THURSDAY = 16
        const val FRIDAY = 32
        const val SATURDAY = 64
        
        fun createDaysOfWeekBitmap(selectedDays: Set<Int>): Int {
            var bitmap = 0
            selectedDays.forEach { day ->
                bitmap = bitmap or (1 shl (day - 1))
            }
            return bitmap
        }
    }
}