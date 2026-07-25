package com.rizowan.taskr.data.local.dao

import androidx.room.*
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Task operations.
 * Provides methods for CRUD operations and various queries.
 */
@Dao
interface TaskDao {

    /**
     * Insert a new task.
     * @return The ID of the newly inserted task.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /**
     * Update an existing task.
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Delete a task.
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Delete a task by its ID.
     */
    @Query("DELETE FROM tasks WHERE id = :taskId")
    suspend fun deleteTaskById(taskId: Long)

    /**
     * Get a task by its ID.
     */
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): Task?

    /**
     * Get a task with its subtasks by task ID.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithSubTasksById(taskId: Long): TaskWithSubTasks?

    /**
     * Get all active (not completed) tasks as a Flow.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 0 ORDER BY dueDate ASC, priority DESC, createdAt DESC")
    fun getAllActiveTasks(): Flow<List<TaskWithSubTasks>>

    /**
     * Get all completed tasks as a Flow.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE isCompleted = 1 ORDER BY completedAt DESC")
    fun getAllCompletedTasks(): Flow<List<TaskWithSubTasks>>

    /**
     * Get tasks due today (active only).
     */
    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate >= :startOfDay 
        AND dueDate < :endOfDay
        ORDER BY dueTime ASC, priority DESC
    """)
    fun getTasksDueToday(startOfDay: Long, endOfDay: Long): Flow<List<TaskWithSubTasks>>

    /**
     * Get upcoming tasks (due after today, active only).
     */
    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE isCompleted = 0 
        AND dueDate >= :startOfTomorrow
        ORDER BY dueDate ASC, dueTime ASC, priority DESC
    """)
    fun getUpcomingTasks(startOfTomorrow: Long): Flow<List<TaskWithSubTasks>>

    /**
     * Get tasks for a specific date.
     */
    @Transaction
    @Query("""
        SELECT * FROM tasks 
        WHERE dueDate >= :startOfDay 
        AND dueDate < :endOfDay
        ORDER BY dueTime ASC, priority DESC
    """)
    fun getTasksForDate(startOfDay: Long, endOfDay: Long): Flow<List<TaskWithSubTasks>>

    /**
     * Get count of completed parent tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks WHERE isCompleted = 1")
    fun getCompletedTaskCount(): Flow<Int>

    /**
     * Get count of all parent tasks.
     */
    @Query("SELECT COUNT(*) FROM tasks")
    fun getTotalTaskCount(): Flow<Int>

    /**
     * Get all tasks with reminders that are not completed.
     * Used for rescheduling alarms on boot.
     */
    @Query("""
        SELECT * FROM tasks 
        WHERE hasReminder = 1 
        AND isCompleted = 0 
        AND dueDate IS NOT NULL
        AND dueTime IS NOT NULL
    """)
    suspend fun getTasksWithReminders(): List<Task>

    /**
     * Mark a task as completed.
     */
    @Query("UPDATE tasks SET isCompleted = 1, completedAt = :completedAt WHERE id = :taskId")
    suspend fun markTaskCompleted(taskId: Long, completedAt: Long = System.currentTimeMillis())

    /**
     * Mark a task as not completed (restore).
     */
    @Query("UPDATE tasks SET isCompleted = 0, completedAt = NULL WHERE id = :taskId")
    suspend fun markTaskNotCompleted(taskId: Long)

    /**
     * Delete all completed tasks.
     */
    @Query("DELETE FROM tasks WHERE isCompleted = 1")
    suspend fun deleteAllCompletedTasks()

    /**
     * Get dates that have tasks (for calendar view).
     */
    @Query("SELECT DISTINCT dueDate FROM tasks WHERE dueDate IS NOT NULL")
    fun getTaskDates(): Flow<List<Long>>
}
