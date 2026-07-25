package com.rizowan.taskr.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

/**
 * Utility class for date and time operations.
 */
object DateUtils {

    // ThreadLocal formatters for thread safety - SimpleDateFormat is NOT thread-safe
    private val dateFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    }
    private val timeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("hh:mm a", Locale.getDefault())
    }
    private val dateTimeFormat: ThreadLocal<SimpleDateFormat> = ThreadLocal.withInitial {
        SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault())
    }

    /**
     * Format a timestamp to a readable date string.
     */
    fun formatDate(timestamp: Long): String {
        return dateFormat.get()!!.format(timestamp)
    }

    /**
     * Format a timestamp to a readable time string.
     */
    fun formatTime(timestamp: Long): String {
        return timeFormat.get()!!.format(timestamp)
    }

    /**
     * Format a timestamp to a readable date and time string.
     */
    fun formatDateTime(timestamp: Long): String {
        return dateTimeFormat.get()!!.format(timestamp)
    }

    /**
     * Get the start and end of today.
     * @return Pair of (startOfDay, endOfDay) timestamps.
     */
    fun getTodayRange(): Pair<Long, Long> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    /**
     * Get the start of tomorrow.
     */
    fun getStartOfTomorrow(): Long {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        return calendar.timeInMillis
    }

    /**
     * Get the start and end of a specific day.
     */
    fun getDayRange(timestamp: Long): Pair<Long, Long> {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val startOfDay = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfDay = calendar.timeInMillis

        return Pair(startOfDay, endOfDay)
    }

    /**
     * Check if a timestamp is today.
     */
    fun isToday(timestamp: Long): Boolean {
        val (start, end) = getTodayRange()
        return timestamp in start until end
    }

    /**
     * Check if a timestamp is yesterday.
     */
    fun isYesterday(timestamp: Long): Boolean {
        val calendar = Calendar.getInstance()
        calendar.add(Calendar.DAY_OF_YEAR, -1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        val startOfYesterday = calendar.timeInMillis

        calendar.add(Calendar.DAY_OF_YEAR, 1)
        val endOfYesterday = calendar.timeInMillis

        return timestamp in startOfYesterday until endOfYesterday
    }

    /**
     * Get a greeting based on the current time.
     */
    fun getGreeting(): String {
        val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
        return when {
            hour < 12 -> "Good morning"
            hour < 17 -> "Good afternoon"
            else -> "Good evening"
        }
    }

    /**
     * Get relative date string (Today, Tomorrow, Yesterday, or formatted date).
     */
    fun getRelativeDateString(timestamp: Long): String {
        val (todayStart, todayEnd) = getTodayRange()
        
        return when {
            timestamp in todayStart until todayEnd -> "Today"
            timestamp >= todayEnd && timestamp < todayEnd + 24 * 60 * 60 * 1000 -> "Tomorrow"
            isYesterday(timestamp) -> "Yesterday"
            else -> formatDate(timestamp)
        }
    }

    /**
     * Check if a date is overdue (before today).
     */
    fun isOverdue(timestamp: Long): Boolean {
        val (startOfToday, _) = getTodayRange()
        return timestamp < startOfToday
    }

    /**
     * Combine date and time into a single timestamp.
     */
    fun combineDateTime(date: Long, time: Long): Long {
        val dateCalendar = Calendar.getInstance().apply { timeInMillis = date }
        val timeCalendar = Calendar.getInstance().apply { timeInMillis = time }
        
        dateCalendar.set(Calendar.HOUR_OF_DAY, timeCalendar.get(Calendar.HOUR_OF_DAY))
        dateCalendar.set(Calendar.MINUTE, timeCalendar.get(Calendar.MINUTE))
        dateCalendar.set(Calendar.SECOND, 0)
        dateCalendar.set(Calendar.MILLISECOND, 0)
        
        return dateCalendar.timeInMillis
    }
}
