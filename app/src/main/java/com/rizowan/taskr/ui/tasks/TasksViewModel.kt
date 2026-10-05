package com.rizowan.taskr.ui.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import com.rizowan.taskr.data.preferences.PreferencesManager
import com.rizowan.taskr.data.repository.TaskRepository
import com.rizowan.taskr.notification.NotificationScheduler
import com.rizowan.taskr.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Filter types for tasks.
 */
enum class TaskFilter {
    ALL,
    TODAY,
    UPCOMING
}

/**
 * UI State for Tasks screen.
 */
data class TasksUiState(
    val tasks: List<TaskWithSubTasks> = emptyList(),
    val completedCount: Int = 0,
    val totalCount: Int = 0,
    val filter: TaskFilter = TaskFilter.ALL,
    val searchQuery: String = "",
    val userName: String = "",
    val greeting: String = "",
    val isLoading: Boolean = false
)

/**
 * ViewModel for Tasks screen.
 */
@HiltViewModel
class TasksViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferencesManager: PreferencesManager,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _currentFilter = MutableStateFlow(TaskFilter.ALL)
    val currentFilter: StateFlow<TaskFilter> = _currentFilter.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    // Combine flows for UI state
    val uiState: StateFlow<TasksUiState> = combine(
        taskRepository.getAllActiveTasks(),
        taskRepository.getTasksDueToday(),
        taskRepository.getUpcomingTasks(),
        taskRepository.getCompletedTaskCount(),
        taskRepository.getTotalTaskCount(),
        preferencesManager.userName,
        _currentFilter,
        _searchQuery
    ) { values ->
        @Suppress("UNCHECKED_CAST")
        val allTasks = values[0] as List<TaskWithSubTasks>
        @Suppress("UNCHECKED_CAST")
        val todayTasks = values[1] as List<TaskWithSubTasks>
        @Suppress("UNCHECKED_CAST")
        val upcomingTasks = values[2] as List<TaskWithSubTasks>
        val completedCount = values[3] as Int
        val totalCount = values[4] as Int
        val userName = values[5] as String
        val filter = values[6] as TaskFilter
        val searchQ = values[7] as String

        var tasks = when (filter) {
            TaskFilter.ALL -> allTasks
            TaskFilter.TODAY -> todayTasks
            TaskFilter.UPCOMING -> upcomingTasks
        }

        // Apply search filter
        if (searchQ.isNotBlank()) {
            tasks = tasks.filter {
                it.task.title.contains(searchQ, ignoreCase = true) ||
                (it.task.description?.contains(searchQ, ignoreCase = true) ?: false) ||
                it.subTasks.any { sub -> sub.title.contains(searchQ, ignoreCase = true) }
            }
        }

        val greeting = buildGreeting(userName)

        TasksUiState(
            tasks = tasks,
            completedCount = completedCount,
            totalCount = totalCount,
            filter = filter,
            searchQuery = searchQ,
            userName = userName,
            greeting = greeting
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = TasksUiState(isLoading = true)
    )

    /**
     * Set the current filter.
     */
    fun setFilter(filter: TaskFilter) {
        _currentFilter.value = filter
    }

    /**
     * Set the search query.
     */
    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    /**
     * Mark a task as completed.
     */
    fun completeTask(task: Task) {
        viewModelScope.launch {
            // Cancel any scheduled notification
            notificationScheduler.cancelNotification(task.id)
            
            // Complete the task (may generate new occurrence if repeating)
            val newTaskId = taskRepository.completeTask(task)
            
            // Schedule notification for new repeating task if applicable
            if (newTaskId != null) {
                val newTask = taskRepository.getTaskById(newTaskId)
                newTask?.let {
                    if (it.hasReminder && it.dueDate != null && it.dueTime != null) {
                        notificationScheduler.scheduleNotification(it)
                    }
                }
            }
        }
    }

    /**
     * Restore a completed task.
     */
    fun restoreTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.restoreTask(taskId)
        }
    }

    /**
     * Delete a task (with subtasks) and return it so it can be undone.
     */
    fun deleteTask(taskWithSubTasks: TaskWithSubTasks) {
        viewModelScope.launch {
            notificationScheduler.cancelNotification(taskWithSubTasks.task.id)
            taskRepository.deleteTask(taskWithSubTasks.task)
        }
    }

    /**
     * Undo a deleted task by re-inserting it.
     */
    fun undoDeleteTask(taskWithSubTasks: TaskWithSubTasks) {
        viewModelScope.launch {
            taskRepository.insertTask(taskWithSubTasks.task, taskWithSubTasks.subTasks)
            if (taskWithSubTasks.task.hasReminder && taskWithSubTasks.task.dueDate != null && taskWithSubTasks.task.dueTime != null) {
                notificationScheduler.scheduleNotification(taskWithSubTasks.task)
            }
        }
    }

    /**
     * Build greeting string based on time and user name.
     */
    private fun buildGreeting(userName: String): String {
        val greeting = DateUtils.getGreeting()
        return if (userName.isNotBlank()) {
            "$greeting, $userName"
        } else {
            greeting
        }
    }
}
