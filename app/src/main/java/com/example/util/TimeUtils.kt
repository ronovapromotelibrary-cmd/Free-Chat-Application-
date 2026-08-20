package com.example.util

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class TimeZoneOption(
    val id: String,
    val displayName: String,
    val offsetDisplay: String
)

object TimeUtils {
    private const val PREFS_NAME = "freechat_time_prefs"
    private const val KEY_TIMEZONE_ID = "pref_timezone_id"
    private const val KEY_24_HOUR = "pref_is_24_hour"

    val AVAILABLE_TIMEZONES = listOf(
        TimeZoneOption("Asia/Dhaka", "Bangladesh Time (BST)", "GMT+6:00"),
        TimeZoneOption("Asia/Kolkata", "India Time (IST)", "GMT+5:30"),
        TimeZoneOption("Asia/Dubai", "UAE / Gulf (GST)", "GMT+4:00"),
        TimeZoneOption("Asia/Riyadh", "Saudi Arabia (AST)", "GMT+3:00"),
        TimeZoneOption("Europe/London", "London (GMT/BST)", "GMT+0 / +1"),
        TimeZoneOption("America/New_York", "New York (EST/EDT)", "GMT-5 / -4"),
        TimeZoneOption("America/Los_Angeles", "Pacific Time (PST/PDT)", "GMT-8 / -7"),
        TimeZoneOption("UTC", "Universal Time (UTC)", "GMT+0:00"),
        TimeZoneOption("SYSTEM", "Device Default", "Auto")
    )

    private fun getPrefs(context: Context): SharedPreferences {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun getTimeZoneId(context: Context): String {
        // Default to Asia/Dhaka if not set, or system if appropriate
        return getPrefs(context).getString(KEY_TIMEZONE_ID, "Asia/Dhaka") ?: "Asia/Dhaka"
    }

    fun setTimeZoneId(context: Context, timeZoneId: String) {
        getPrefs(context).edit().putString(KEY_TIMEZONE_ID, timeZoneId).apply()
    }

    fun is24HourFormat(context: Context): Boolean {
        return getPrefs(context).getBoolean(KEY_24_HOUR, false)
    }

    fun set24HourFormat(context: Context, is24Hour: Boolean) {
        getPrefs(context).edit().putBoolean(KEY_24_HOUR, is24Hour).apply()
    }

    fun getTimeZone(context: Context): TimeZone {
        val id = getTimeZoneId(context)
        return if (id == "SYSTEM" || id.isBlank()) {
            TimeZone.getDefault()
        } else {
            try {
                TimeZone.getTimeZone(id)
            } catch (_: Exception) {
                TimeZone.getDefault()
            }
        }
    }

    fun formatMessageTime(context: Context, timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val tz = getTimeZone(context)
        val is24 = is24HourFormat(context)
        val pattern = if (is24) "HH:mm" else "h:mm a"
        val sdf = SimpleDateFormat(pattern, Locale.US).apply {
            timeZone = tz
        }
        return sdf.format(Date(timestamp))
    }

    fun formatConversationTime(context: Context, timestamp: Long): String {
        if (timestamp <= 0L) return ""
        val tz = getTimeZone(context)
        val is24 = is24HourFormat(context)
        val now = System.currentTimeMillis()

        val msgCal = Calendar.getInstance(tz).apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance(tz).apply { timeInMillis = now }

        val isSameDay = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                (nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1)

        return when {
            isSameDay -> {
                val pattern = if (is24) "HH:mm" else "h:mm a"
                SimpleDateFormat(pattern, Locale.US).apply { timeZone = tz }.format(Date(timestamp))
            }
            isYesterday -> "Yesterday"
            nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) -> {
                SimpleDateFormat("MMM d", Locale.US).apply { timeZone = tz }.format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("MMM d, yyyy", Locale.US).apply { timeZone = tz }.format(Date(timestamp))
            }
        }
    }

    fun formatDateHeader(context: Context, timestamp: Long): String {
        if (timestamp <= 0L) return "Today"
        val tz = getTimeZone(context)
        val now = System.currentTimeMillis()

        val msgCal = Calendar.getInstance(tz).apply { timeInMillis = timestamp }
        val nowCal = Calendar.getInstance(tz).apply { timeInMillis = now }

        val isSameDay = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                (nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1)

        return when {
            isSameDay -> "Today"
            isYesterday -> "Yesterday"
            nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) -> {
                SimpleDateFormat("MMMM d", Locale.US).apply { timeZone = tz }.format(Date(timestamp))
            }
            else -> {
                SimpleDateFormat("MMMM d, yyyy", Locale.US).apply { timeZone = tz }.format(Date(timestamp))
            }
        }
    }

    fun formatLastSeen(context: Context, lastSeen: Long): String {
        if (lastSeen <= 0L) return "recently"
        val now = System.currentTimeMillis()
        val diffMillis = now - lastSeen
        if (diffMillis < 0) return "just now"

        val diffMinutes = diffMillis / (60 * 1000)
        val diffHours = diffMillis / (60 * 60 * 1000)

        val tz = getTimeZone(context)
        val is24 = is24HourFormat(context)
        val timePattern = if (is24) "HH:mm" else "h:mm a"
        val timeStr = SimpleDateFormat(timePattern, Locale.US).apply { timeZone = tz }.format(Date(lastSeen))

        val msgCal = Calendar.getInstance(tz).apply { timeInMillis = lastSeen }
        val nowCal = Calendar.getInstance(tz).apply { timeInMillis = now }

        val isSameDay = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                nowCal.get(Calendar.DAY_OF_YEAR) == msgCal.get(Calendar.DAY_OF_YEAR)

        val isYesterday = nowCal.get(Calendar.YEAR) == msgCal.get(Calendar.YEAR) &&
                (nowCal.get(Calendar.DAY_OF_YEAR) - msgCal.get(Calendar.DAY_OF_YEAR) == 1)

        return when {
            diffMinutes < 1 -> "just now"
            diffMinutes < 60 -> "$diffMinutes ${if (diffMinutes == 1L) "minute" else "minutes"} ago"
            isSameDay -> "today at $timeStr"
            isYesterday -> "yesterday at $timeStr"
            else -> {
                val dateStr = SimpleDateFormat("MMM d", Locale.US).apply { timeZone = tz }.format(Date(lastSeen))
                "$dateStr at $timeStr"
            }
        }
    }
}
