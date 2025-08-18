package com.textalarm.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.time.LocalTime
import java.time.format.DateTimeFormatter
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
    val createdAt: Long = System.currentTimeMillis()
) {
    fun getTimeString(): String {
        val time = LocalTime.of(hour, minute)
        return time.format(DateTimeFormatter.ofPattern("h:mm a"))
    }
    
    fun getNextAlarmTime(): Long {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, hour)
        calendar.set(Calendar.MINUTE, minute)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        if (!isRecurring) {
            // One-time alarm
            if (calendar.timeInMillis <= System.currentTimeMillis()) {
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            return calendar.timeInMillis
        }
        
        // Recurring alarm - find next occurrence
        val currentDayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)
        val currentTime = System.currentTimeMillis()
        
        for (i in 0..6) {
            val dayOfWeek = ((currentDayOfWeek - 1 + i) % 7) + 1
            val dayBit = 1 shl (dayOfWeek - 1)
            
            if (daysOfWeek and dayBit != 0) {
                if (i == 0 && calendar.timeInMillis > currentTime) {
                    // Today and time hasn't passed
                    return calendar.timeInMillis
                } else if (i > 0) {
                    // Future day
                    calendar.add(Calendar.DAY_OF_YEAR, i)
                    return calendar.timeInMillis
                }
            }
        }
        
        return calendar.timeInMillis
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