package com.rizowan.taskr.data.local.entity

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

/**
 * Represents a Task entity in the database.
 * Contains all task properties including repeat settings.
 */
@Parcelize
@Entity(tableName = "tasks")
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val priority: Priority = Priority.LOW,
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val isRepeating: Boolean = false,
    val repeatType: RepeatType? = null,
    val hasReminder: Boolean = false,
    val createdAt: Long = System.currentTimeMillis()
) : Parcelable

/**
 * Enum representing task priority levels.
 */
enum class Priority {
    LOW,
    MEDIUM,
    HIGH
}

/**
 * Enum representing repeat types for recurring tasks.
 */
enum class RepeatType {
    DAILY,
    WEEKLY,
    MONTHLY
}
