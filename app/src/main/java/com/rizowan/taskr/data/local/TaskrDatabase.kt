package com.rizowan.taskr.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.rizowan.taskr.data.local.dao.SubTaskDao
import com.rizowan.taskr.data.local.dao.TaskDao
import com.rizowan.taskr.data.local.entity.SubTask
import com.rizowan.taskr.data.local.entity.Task

/**
 * Room database for Taskr application.
 * Contains Task and SubTask entities.
 */
@Database(
    entities = [Task::class, SubTask::class],
    version = 1,
    exportSchema = true  // Enable schema export for proper migration tracking
)
@TypeConverters(Converters::class)
abstract class TaskrDatabase : RoomDatabase() {
    
    abstract fun taskDao(): TaskDao
    
    abstract fun subTaskDao(): SubTaskDao
}
