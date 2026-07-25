package com.rizowan.taskr.data.local

import androidx.room.TypeConverter
import com.rizowan.taskr.data.local.entity.Priority
import com.rizowan.taskr.data.local.entity.RepeatType

/**
 * Type converters for Room database.
 * Converts enums to/from strings for storage.
 */
class Converters {

    @TypeConverter
    fun fromPriority(priority: Priority): String {
        return priority.name
    }

    @TypeConverter
    fun toPriority(value: String): Priority {
        return Priority.valueOf(value)
    }

    @TypeConverter
    fun fromRepeatType(repeatType: RepeatType?): String? {
        return repeatType?.name
    }

    @TypeConverter
    fun toRepeatType(value: String?): RepeatType? {
        return value?.let { RepeatType.valueOf(it) }
    }
}
