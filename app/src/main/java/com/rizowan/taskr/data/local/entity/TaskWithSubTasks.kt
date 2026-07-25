package com.rizowan.taskr.data.local.entity

import android.os.Parcelable
import androidx.room.Embedded
import androidx.room.Relation
import kotlinx.parcelize.Parcelize

/**
 * Data class representing a Task with its associated SubTasks.
 * Used for querying tasks with their subtasks in a single query.
 */
@Parcelize
data class TaskWithSubTasks(
    @Embedded val task: Task,
    @Relation(
        parentColumn = "id",
        entityColumn = "parentTaskId"
    )
    val subTasks: List<SubTask> = emptyList()
) : Parcelable
