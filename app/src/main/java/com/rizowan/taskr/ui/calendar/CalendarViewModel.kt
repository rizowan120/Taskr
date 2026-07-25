package com.rizowan.taskr.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import com.rizowan.taskr.data.repository.TaskRepository
import com.rizowan.taskr.notification.NotificationScheduler
import com.rizowan.taskr.util.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI State for Calendar screen.
 */
data class CalendarUiState(
    val selectedDate: Long = System.currentTimeMillis(),
    val selectedDateFormatted: String = "",
    val tasksForDate: List<TaskWithSubTasks> = emptyList(),
    val datesWithTasks: List<Long> = emptyList()
)

/**
 * ViewModel for Calendar screen.
 */
@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _selectedDate = MutableStateFlow(System.currentTimeMillis())
    
    val uiState: StateFlow<CalendarUiState> = combine(
        _selectedDate,
        _selectedDate.flatMapLatest { date -> taskRepository.getTasksForDate(date) },
        taskRepository.getTaskDates()
    ) { selectedDate, tasksForDate, datesWithTasks ->
        CalendarUiState(
            selectedDate = selectedDate,
            selectedDateFormatted = DateUtils.getRelativeDateString(selectedDate),
            tasksForDate = tasksForDate,
            datesWithTasks = datesWithTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = CalendarUiState()
    )

    /**
     * Set the selected date.
     */
    fun setSelectedDate(date: Long) {
        _selectedDate.value = date
    }

    /**
     * Complete a task.
     */
    fun completeTask(task: Task) {
        viewModelScope.launch {
            notificationScheduler.cancelNotification(task.id)
            val newTaskId = taskRepository.completeTask(task)
            
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
     * Restore a task.
     */
    fun restoreTask(taskId: Long) {
        viewModelScope.launch {
            taskRepository.restoreTask(taskId)
        }
    }
}
