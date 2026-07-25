package com.rizowan.taskr.data.local.dao

import androidx.room.*
import com.rizowan.taskr.data.local.entity.SubTask
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for SubTask operations.
 */
@Dao
interface SubTaskDao {

    /**
     * Insert a new subtask.
     * @return The ID of the newly inserted subtask.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTask(subTask: SubTask): Long

    /**
     * Insert multiple subtasks.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSubTasks(subTasks: List<SubTask>)

    /**
     * Update an existing subtask.
     */
    @Update
    suspend fun updateSubTask(subTask: SubTask)

    /**
     * Delete a subtask.
     */
    @Delete
    suspend fun deleteSubTask(subTask: SubTask)

    /**
     * Delete a subtask by its ID.
     */
    @Query("DELETE FROM subtasks WHERE id = :subTaskId")
    suspend fun deleteSubTaskById(subTaskId: Long)

    /**
     * Get all subtasks for a parent task.
     */
    @Query("SELECT * FROM subtasks WHERE parentTaskId = :taskId ORDER BY id ASC")
    fun getSubTasksForTask(taskId: Long): Flow<List<SubTask>>

    /**
     * Get all subtasks for a parent task (non-Flow version).
     */
    @Query("SELECT * FROM subtasks WHERE parentTaskId = :taskId ORDER BY id ASC")
    suspend fun getSubTasksForTaskSync(taskId: Long): List<SubTask>

    /**
     * Mark a subtask as completed.
     */
    @Query("UPDATE subtasks SET isCompleted = :isCompleted WHERE id = :subTaskId")
    suspend fun updateSubTaskCompletion(subTaskId: Long, isCompleted: Boolean)

    /**
     * Reset all subtasks for a task (used when repeating task regenerates).
     */
    @Query("UPDATE subtasks SET isCompleted = 0 WHERE parentTaskId = :taskId")
    suspend fun resetSubTasksForTask(taskId: Long)

    /**
     * Delete all subtasks for a parent task.
     */
    @Query("DELETE FROM subtasks WHERE parentTaskId = :taskId")
    suspend fun deleteSubTasksForTask(taskId: Long)
}
