package com.rizowan.taskr.ui.completed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import com.rizowan.taskr.data.repository.TaskRepository
import com.rizowan.taskr.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Grouped item for completed tasks list.
 */
sealed class CompletedListItem {
    data class Header(val title: String) : CompletedListItem()
    data class TaskItem(val taskWithSubTasks: TaskWithSubTasks) : CompletedListItem()
}

/**
 * UI State for Completed screen.
 */
data class CompletedUiState(
    val items: List<CompletedListItem> = emptyList(),
    val hasCompletedTasks: Boolean = false
)

/**
 * ViewModel for Completed screen.
 */
@HiltViewModel
class CompletedViewModel @Inject constructor(
    private val taskRepository: TaskRepository
) : ViewModel() {

    val uiState: StateFlow<CompletedUiState> = taskRepository.getAllCompletedTasks()
        .map { tasks -> groupCompletedTasks(tasks) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CompletedUiState()
        )

    /**
     * Group completed tasks by Today, Yesterday, Older.
     */
    private fun groupCompletedTasks(tasks: List<TaskWithSubTasks>): CompletedUiState {
        if (tasks.isEmpty()) {
            return CompletedUiState(items = emptyList(), hasCompletedTasks = false)
        }

        val items = mutableListOf<CompletedListItem>()
        val todayTasks = mutableListOf<TaskWithSubTasks>()
        val yesterdayTasks = mutableListOf<TaskWithSubTasks>()
        val olderTasks = mutableListOf<TaskWithSubTasks>()

        tasks.forEach { taskWithSubTasks ->
            val completedAt = taskWithSubTasks.task.completedAt ?: return@forEach
            when {
                DateUtils.isToday(completedAt) -> todayTasks.add(taskWithSubTasks)
                DateUtils.isYesterday(completedAt) -> yesterdayTasks.add(taskWithSubTasks)
                else -> olderTasks.add(taskWithSubTasks)
            }
        }

        if (todayTasks.isNotEmpty()) {
            items.add(CompletedListItem.Header("Today"))
            items.addAll(todayTasks.map { CompletedListItem.TaskItem(it) })
        }

        if (yesterdayTasks.isNotEmpty()) {
            items.add(CompletedListItem.Header("Yesterday"))
            items.addAll(yesterdayTasks.map { CompletedListItem.TaskItem(it) })
        }

        if (olderTasks.isNotEmpty()) {
            items.add(CompletedListItem.Header("Older"))
            items.addAll(olderTasks.map { CompletedListItem.TaskItem(it) })
        }

        return CompletedUiState(items = items, hasCompletedTasks = true)
    }

    /**
     * Clear all completed tasks.
     */
    fun clearAllCompletedTasks() {
        viewModelScope.launch {
            taskRepository.deleteAllCompletedTasks()
        }
    }
}
