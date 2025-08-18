package com.textalarm.app.utils

import java.util.regex.Pattern

object TimeParser {
    
    fun parseTime(timeText: String): Pair<Int, Int>? {
        val lowerText = timeText.lowercase().replace(" ", "")
        
        val patterns = listOf(
            Pattern.compile("(\\d{1,2}):(\\d{2})(am|pm)"),
            Pattern.compile("(\\d{1,2})(am|pm)"),
            Pattern.compile("(\\d{1,2}):(\\d{2})"),
            Pattern.compile("(\\d{3,4})")
        )
        
        for (pattern in patterns) {
            val matcher = pattern.matcher(lowerText)
            if (matcher.matches()) {
                when (pattern.pattern()) {
                    "(\\d{1,2}):(\\d{2})(am|pm)" -> {
                        var hour = matcher.group(1)!!.toInt()
                        val minute = matcher.group(2)!!.toInt()
                        val ampm = matcher.group(3)!!
                        
                        if (minute >= 60) return null
                        if (hour < 1 || hour > 12) return null
                        
                        if (ampm == "pm" && hour != 12) hour += 12
                        if (ampm == "am" && hour == 12) hour = 0
                        
                        return Pair(hour, minute)
                    }
                    "(\\d{1,2})(am|pm)" -> {
                        var hour = matcher.group(1)!!.toInt()
                        val ampm = matcher.group(2)!!
                        
                        if (hour < 1 || hour > 12) return null
                        
                        if (ampm == "pm" && hour != 12) hour += 12
                        if (ampm == "am" && hour == 12) hour = 0
                        
                        return Pair(hour, 0)
                    }
                    "(\\d{1,2}):(\\d{2})" -> {
                        val hour = matcher.group(1)!!.toInt()
                        val minute = matcher.group(2)!!.toInt()
                        
                        if (hour >= 24 || minute >= 60) return null
                        
                        return Pair(hour, minute)
                    }
                    "(\\d{3,4})" -> {
                        val timeStr = matcher.group(1)!!
                        val hour = if (timeStr.length == 3) {
                            timeStr.substring(0, 1).toInt()
                        } else {
                            timeStr.substring(0, 2).toInt()
                        }
                        val minute = timeStr.substring(timeStr.length - 2).toInt()
                        
                        if (hour >= 24 || minute >= 60) return null
                        
                        return Pair(hour, minute)
                    }
                }
            }
        }
        
        return null
    }
}