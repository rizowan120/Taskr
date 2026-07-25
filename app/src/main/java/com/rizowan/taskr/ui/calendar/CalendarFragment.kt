package com.rizowan.taskr.ui.calendar

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.rizowan.taskr.R
import com.rizowan.taskr.data.preferences.PreferencesManager
import com.rizowan.taskr.databinding.FragmentCalendarBinding
import com.rizowan.taskr.ui.tasks.TaskAdapter
import com.rizowan.taskr.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment displaying a calendar view with tasks.
 */
@AndroidEntryPoint
class CalendarFragment : Fragment() {

    private var _binding: FragmentCalendarBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CalendarViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var taskAdapter: TaskAdapter
    private var hapticsEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCalendarBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupCalendar()
        setupRecyclerView()
        observeUiState()
        observeHaptics()
    }

    /**
     * Setup calendar view.
     */
    private fun setupCalendar() {
        binding.calendarView.setOnDateChangeListener { _, year, month, dayOfMonth ->
            val calendar = java.util.Calendar.getInstance()
            calendar.set(year, month, dayOfMonth, 0, 0, 0)
            calendar.set(java.util.Calendar.MILLISECOND, 0)
            viewModel.setSelectedDate(calendar.timeInMillis)
        }

        // Long press to add task with pre-selected date
        binding.calendarView.setOnLongClickListener {
            HapticUtils.performLongPressFeedback(it, hapticsEnabled)
            val bundle = bundleOf(
                "taskId" to -1L,
                "preSelectedDate" to viewModel.uiState.value.selectedDate
            )
            findNavController().navigate(R.id.addEditTaskFragment, bundle)
            true
        }
    }

    /**
     * Setup RecyclerView for tasks.
     */
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { taskWithSubTasks ->
                val bundle = bundleOf(
                    "taskId" to taskWithSubTasks.task.id,
                    "preSelectedDate" to -1L
                )
                findNavController().navigate(R.id.addEditTaskFragment, bundle)
            },
            onTaskCheckedChange = { taskWithSubTasks, isCompleted ->
                if (isCompleted) {
                    HapticUtils.performConfirmFeedback(binding.root, hapticsEnabled)
                    viewModel.completeTask(taskWithSubTasks.task)
                } else {
                    viewModel.restoreTask(taskWithSubTasks.task.id)
                }
            }
        )
        binding.rvTasks.adapter = taskAdapter
    }

    /**
     * Observe UI state.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvSelectedDate.text = state.selectedDateFormatted
                    taskAdapter.submitList(state.tasksForDate)

                    if (state.tasksForDate.isEmpty()) {
                        binding.rvTasks.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvTasks.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
                    }
                }
            }
        }
    }

    /**
     * Observe haptics preference.
     */
    private fun observeHaptics() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferencesManager.hapticsEnabled.collect { enabled ->
                    hapticsEnabled = enabled
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
