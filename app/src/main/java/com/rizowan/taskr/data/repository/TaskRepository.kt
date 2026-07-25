package com.rizowan.taskr.data.repository

import com.rizowan.taskr.data.local.dao.SubTaskDao
import com.rizowan.taskr.data.local.dao.TaskDao
import com.rizowan.taskr.data.local.entity.RepeatType
import com.rizowan.taskr.data.local.entity.SubTask
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import com.rizowan.taskr.util.DateUtils
import kotlinx.coroutines.flow.Flow
import java.util.Calendar
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for Task and SubTask operations.
 * Acts as a single source of truth for task data.
 */
@Singleton
class TaskRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao
) {
    // ========== Task Operations ==========

    /**
     * Insert a new task with its subtasks.
     * @return The ID of the newly inserted task.
     */
    suspend fun insertTask(task: Task, subTasks: List<SubTask>): Long {
        val taskId = taskDao.insertTask(task)
        if (subTasks.isNotEmpty()) {
            val subTasksWithParentId = subTasks.map { it.copy(parentTaskId = taskId) }
            subTaskDao.insertSubTasks(subTasksWithParentId)
        }
        return taskId
    }

    /**
     * Update an existing task and its subtasks.
     */
    suspend fun updateTask(task: Task, subTasks: List<SubTask>) {
        taskDao.updateTask(task)
        // Delete existing subtasks and insert new ones
        subTaskDao.deleteSubTasksForTask(task.id)
        if (subTasks.isNotEmpty()) {
            val subTasksWithParentId = subTasks.map { it.copy(parentTaskId = task.id) }
            subTaskDao.insertSubTasks(subTasksWithParentId)
        }
    }

    /**
     * Delete a task (subtasks are deleted automatically via CASCADE).
     */
    suspend fun deleteTask(task: Task) {
        taskDao.deleteTask(task)
    }

    /**
     * Delete a task by its ID.
     */
    suspend fun deleteTaskById(taskId: Long) {
        taskDao.deleteTaskById(taskId)
    }

    /**
     * Get a task by its ID.
     */
    suspend fun getTaskById(taskId: Long): Task? {
        return taskDao.getTaskById(taskId)
    }

    /**
     * Get a task with its subtasks by task ID.
     */
    suspend fun getTaskWithSubTasksById(taskId: Long): TaskWithSubTasks? {
        return taskDao.getTaskWithSubTasksById(taskId)
    }

    /**
     * Get all active tasks.
     */
    fun getAllActiveTasks(): Flow<List<TaskWithSubTasks>> {
        return taskDao.getAllActiveTasks()
    }

    /**
     * Get all completed tasks.
     */
    fun getAllCompletedTasks(): Flow<List<TaskWithSubTasks>> {
        return taskDao.getAllCompletedTasks()
    }

    /**
     * Get tasks due today.
     */
    fun getTasksDueToday(): Flow<List<TaskWithSubTasks>> {
        val (startOfDay, endOfDay) = DateUtils.getTodayRange()
        return taskDao.getTasksDueToday(startOfDay, endOfDay)
    }

    /**
     * Get upcoming tasks (due after today).
     */
    fun getUpcomingTasks(): Flow<List<TaskWithSubTasks>> {
        val startOfTomorrow = DateUtils.getStartOfTomorrow()
        return taskDao.getUpcomingTasks(startOfTomorrow)
    }

    /**
     * Get tasks for a specific date.
     */
    fun getTasksForDate(date: Long): Flow<List<TaskWithSubTasks>> {
        val (startOfDay, endOfDay) = DateUtils.getDayRange(date)
        return taskDao.getTasksForDate(startOfDay, endOfDay)
    }

    /**
     * Get dates that have tasks (for calendar highlighting).
     */
    fun getTaskDates(): Flow<List<Long>> {
        return taskDao.getTaskDates()
    }

    /**
     * Get completed task count.
     */
    fun getCompletedTaskCount(): Flow<Int> {
        return taskDao.getCompletedTaskCount()
    }

    /**
     * Get total task count.
     */
    fun getTotalTaskCount(): Flow<Int> {
        return taskDao.getTotalTaskCount()
    }

    /**
     * Mark a task as completed.
     * If the task is repeating, generates the next occurrence.
     * @return The ID of the new task if repeating, null otherwise.
     */
    suspend fun completeTask(task: Task): Long? {
        val completedAt = System.currentTimeMillis()
        taskDao.markTaskCompleted(task.id, completedAt)

        // If repeating, generate next occurrence
        if (task.isRepeating && task.repeatType != null && task.dueDate != null) {
            return generateNextOccurrence(task)
        }
        return null
    }

    /**
     * Restore a completed task back to active.
     */
    suspend fun restoreTask(taskId: Long) {
        taskDao.markTaskNotCompleted(taskId)
    }

    /**
     * Delete all completed tasks.
     */
    suspend fun deleteAllCompletedTasks() {
        taskDao.deleteAllCompletedTasks()
    }

    /**
     * Get all tasks with reminders (for rescheduling on boot).
     */
    suspend fun getTasksWithReminders(): List<Task> {
        return taskDao.getTasksWithReminders()
    }

    /**
     * Generate the next occurrence of a repeating task.
     */
    private suspend fun generateNextOccurrence(task: Task): Long {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = task.dueDate!!
        }

        // Advance the date based on repeat type
        when (task.repeatType) {
            RepeatType.DAILY -> calendar.add(Calendar.DAY_OF_YEAR, 1)
            RepeatType.WEEKLY -> calendar.add(Calendar.WEEK_OF_YEAR, 1)
            RepeatType.MONTHLY -> calendar.add(Calendar.MONTH, 1)
            null -> { /* Should not happen */ }
        }

        // Create new task with advanced date
        val newTask = task.copy(
            id = 0, // Auto-generate new ID
            dueDate = calendar.timeInMillis,
            isCompleted = false,
            completedAt = null,
            createdAt = System.currentTimeMillis()
        )

        val newTaskId = taskDao.insertTask(newTask)

        // Copy subtasks with reset completion status
        val subTasks = subTaskDao.getSubTasksForTaskSync(task.id)
        if (subTasks.isNotEmpty()) {
            val resetSubTasks = subTasks.map { 
                it.copy(id = 0, parentTaskId = newTaskId, isCompleted = false) 
            }
            subTaskDao.insertSubTasks(resetSubTasks)
        }

        return newTaskId
    }

    // ========== SubTask Operations ==========

    /**
     * Update subtask completion status.
     */
    suspend fun updateSubTaskCompletion(subTaskId: Long, isCompleted: Boolean) {
        subTaskDao.updateSubTaskCompletion(subTaskId, isCompleted)
    }

    /**
     * Get subtasks for a task.
     */
    fun getSubTasksForTask(taskId: Long): Flow<List<SubTask>> {
        return subTaskDao.getSubTasksForTask(taskId)
    }
}
