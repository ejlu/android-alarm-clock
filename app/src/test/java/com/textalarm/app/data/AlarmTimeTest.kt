package com.textalarm.app.data

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Calendar

class AlarmTimeTest {
    private fun date(day: Int, hour: Int, minute: Int = 0) =
        Calendar.getInstance().apply {
            clear()
            set(2026, Calendar.NOVEMBER, day, hour, minute)
        }.timeInMillis

    private val weekly = Alarm(hour = 7, minute = 30, isRecurring = true, daysOfWeek = Alarm.MONDAY)

    @Test fun weeklyAlarmStillUpcomingToday() {
        assertEquals(date(23, 7, 30), weekly.getNextAlarmTime(date(23, 7)))
    }

    @Test fun weeklyAlarmAtOrAfterTheScheduledMinuteMovesToNextWeek() {
        val next = date(30, 7, 30)
        assertEquals(next, weekly.getNextAlarmTime(date(23, 7, 30)))
        assertEquals(next, weekly.getNextAlarmTime(date(23, 8)))
    }

    @Test fun recurringAlarmUsesNextSelectedDay() {
        val weekdays = weekly.copy(daysOfWeek = Alarm.MONDAY or Alarm.TUESDAY)
        assertEquals(date(24, 7, 30), weekdays.getNextAlarmTime(date(23, 8)))
    }

    @Test fun oneTimeAlarmAfterItsTimeMovesToTomorrow() {
        val once = weekly.copy(isRecurring = false)
        assertEquals(date(24, 7, 30), once.getNextAlarmTime(date(23, 8)))
    }
}
