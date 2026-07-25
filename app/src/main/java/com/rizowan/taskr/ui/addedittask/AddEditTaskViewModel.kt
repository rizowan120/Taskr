package com.rizowan.taskr.ui.addedittask

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizowan.taskr.R
import com.rizowan.taskr.data.local.entity.Priority
import com.rizowan.taskr.data.local.entity.RepeatType
import com.rizowan.taskr.data.local.entity.SubTask
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.repository.TaskRepository
import com.rizowan.taskr.notification.NotificationScheduler
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI Events for Add/Edit Task screen.
 */
sealed class AddEditTaskEvent {
    data object TaskSaved : AddEditTaskEvent()
    data object TaskDeleted : AddEditTaskEvent()
    data class ShowError(val messageResId: Int) : AddEditTaskEvent()
}

/**
 * UI State for Add/Edit Task screen.
 */
data class AddEditTaskUiState(
    val isEditMode: Boolean = false,
    val taskId: Long = 0,
    val title: String = "",
    val description: String = "",
    val priority: Priority = Priority.LOW,
    val dueDate: Long? = null,
    val dueTime: Long? = null,
    val hasReminder: Boolean = false,
    val isRepeating: Boolean = false,
    val repeatType: RepeatType = RepeatType.DAILY,
    val subTasks: List<SubTask> = emptyList(),
    val isLoading: Boolean = false,
    val isSaveEnabled: Boolean = false
)

/**
 * ViewModel for Add/Edit Task screen.
 */
@HiltViewModel
class AddEditTaskViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: Long = savedStateHandle["taskId"] ?: -1L
    private val preSelectedDate: Long = savedStateHandle["preSelectedDate"] ?: -1L

    private val _uiState = MutableStateFlow(AddEditTaskUiState())
    val uiState: StateFlow<AddEditTaskUiState> = _uiState.asStateFlow()

    private val _events = MutableSharedFlow<AddEditTaskEvent>()
    val events: SharedFlow<AddEditTaskEvent> = _events.asSharedFlow()

    init {
        if (taskId > 0) {
            loadTask()
        } else if (preSelectedDate > 0) {
            _uiState.value = _uiState.value.copy(dueDate = preSelectedDate)
        }
    }

    /**
     * Load existing task for editing.
     */
    private fun loadTask() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            val taskWithSubTasks = taskRepository.getTaskWithSubTasksById(taskId)
            if (taskWithSubTasks != null) {
                val task = taskWithSubTasks.task
                _uiState.value = AddEditTaskUiState(
                    isEditMode = true,
                    taskId = task.id,
                    title = task.title,
                    description = task.description ?: "",
                    priority = task.priority,
                    dueDate = task.dueDate,
                    dueTime = task.dueTime,
                    hasReminder = task.hasReminder,
                    isRepeating = task.isRepeating,
                    repeatType = task.repeatType ?: RepeatType.DAILY,
                    subTasks = taskWithSubTasks.subTasks,
                    isSaveEnabled = task.title.isNotBlank()
                )
            }
        }
    }

    /**
     * Update task title.
     */
    fun setTitle(title: String) {
        _uiState.value = _uiState.value.copy(
            title = title,
            isSaveEnabled = title.isNotBlank()
        )
    }

    /**
     * Update task description.
     */
    fun setDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    /**
     * Update task priority.
     */
    fun setPriority(priority: Priority) {
        _uiState.value = _uiState.value.copy(priority = priority)
    }

    /**
     * Update due date.
     */
    fun setDueDate(date: Long?) {
        val currentState = _uiState.value
        // If date is cleared, also clear time and reminder
        val newDueTime = if (date == null) null else currentState.dueTime
        val newHasReminder = if (date == null) false else currentState.hasReminder
        val newIsRepeating = if (date == null) false else currentState.isRepeating
        
        _uiState.value = currentState.copy(
            dueDate = date,
            dueTime = newDueTime,
            hasReminder = newHasReminder,
            isRepeating = newIsRepeating
        )
    }

    /**
     * Update due time.
     */
    fun setDueTime(time: Long?) {
        val currentState = _uiState.value
        val newHasReminder = if (time == null) false else currentState.hasReminder
        
        _uiState.value = currentState.copy(
            dueTime = time,
            hasReminder = newHasReminder
        )
    }

    /**
     * Update reminder toggle.
     */
    fun setReminder(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(hasReminder = enabled)
    }

    /**
     * Update repeat toggle.
     */
    fun setRepeating(enabled: Boolean) {
        _uiState.value = _uiState.value.copy(isRepeating = enabled)
    }

    /**
     * Update repeat type.
     */
    fun setRepeatType(repeatType: RepeatType) {
        _uiState.value = _uiState.value.copy(repeatType = repeatType)
    }

    /**
     * Add a new subtask.
     */
    fun addSubtask(title: String) {
        if (title.isBlank()) return
        
        val newSubtask = SubTask(
            id = 0, // Will be assigned on save
            parentTaskId = taskId.coerceAtLeast(0),
            title = title,
            isCompleted = false
        )
        _uiState.value = _uiState.value.copy(
            subTasks = _uiState.value.subTasks + newSubtask
        )
    }

    /**
     * Update a subtask's completion status.
     */
    fun updateSubtaskCompletion(position: Int, isCompleted: Boolean) {
        val updatedList = _uiState.value.subTasks.toMutableList()
        if (position in updatedList.indices) {
            updatedList[position] = updatedList[position].copy(isCompleted = isCompleted)
            _uiState.value = _uiState.value.copy(subTasks = updatedList)
        }
    }

    /**
     * Update a subtask's title.
     */
    fun updateSubtaskTitle(position: Int, title: String) {
        val updatedList = _uiState.value.subTasks.toMutableList()
        if (position in updatedList.indices) {
            updatedList[position] = updatedList[position].copy(title = title)
            _uiState.value = _uiState.value.copy(subTasks = updatedList)
        }
    }

    /**
     * Remove a subtask.
     */
    fun removeSubtask(position: Int) {
        val updatedList = _uiState.value.subTasks.toMutableList()
        if (position in updatedList.indices) {
            updatedList.removeAt(position)
            _uiState.value = _uiState.value.copy(subTasks = updatedList)
        }
    }

    /**
     * Save the task.
     */
    fun saveTask() {
        val state = _uiState.value
        if (state.title.isBlank()) {
            viewModelScope.launch {
                _events.emit(AddEditTaskEvent.ShowError(R.string.task_title_required))
            }
            return
        }

        viewModelScope.launch {
            val task = Task(
                id = if (state.isEditMode) state.taskId else 0,
                title = state.title.trim(),
                description = state.description.trim().ifBlank { null },
                priority = state.priority,
                dueDate = state.dueDate,
                dueTime = state.dueTime,
                hasReminder = state.hasReminder && state.dueDate != null && state.dueTime != null,
                isRepeating = state.isRepeating && state.dueDate != null,
                repeatType = if (state.isRepeating && state.dueDate != null) state.repeatType else null
            )

            // Filter out empty subtasks
            val subTasks = state.subTasks.filter { it.title.isNotBlank() }

            if (state.isEditMode) {
                taskRepository.updateTask(task, subTasks)
                // Cancel old notification and schedule new if needed
                notificationScheduler.cancelNotification(task.id)
                if (task.hasReminder) {
                    notificationScheduler.scheduleNotification(task)
                }
            } else {
                val newTaskId = taskRepository.insertTask(task, subTasks)
                // Schedule notification for new task
                if (task.hasReminder) {
                    val savedTask = taskRepository.getTaskById(newTaskId)
                    savedTask?.let { notificationScheduler.scheduleNotification(it) }
                }
            }

            _events.emit(AddEditTaskEvent.TaskSaved)
        }
    }

    /**
     * Delete the task.
     */
    fun deleteTask() {
        viewModelScope.launch {
            val state = _uiState.value
            if (state.isEditMode) {
                val task = taskRepository.getTaskById(state.taskId)
                task?.let {
                    // Cancel notification
                    notificationScheduler.cancelNotification(it.id)
                    taskRepository.deleteTask(it)
                }
                _events.emit(AddEditTaskEvent.TaskDeleted)
            }
        }
    }
}
