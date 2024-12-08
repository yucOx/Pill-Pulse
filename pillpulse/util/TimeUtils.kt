package com.yucox.pillpulse.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object TimeUtils {
    private val _shf = SimpleDateFormat("HH:mm", Locale.getDefault())
    private val _sdf = SimpleDateFormat("dd.MM.yyyy")

    fun toStringCalendar(date: Date?): String {
        if (date == null)
            return ""
        return _sdf.format(date)
    }

    fun toStringHour(time: Date?): String {
        if (time == null)
            return ""
        return _shf.format(time)
    }

    fun toDateCalendar(dateString: String?): Date {
        if (dateString == null)
            return Date()
        return _sdf.parse(dateString) ?: Date()
    }

    fun toDateClock(timeString: String?): Date {
        if (timeString == null)
            return Date()
        return _shf.parse(timeString) ?: Date()
    }
}