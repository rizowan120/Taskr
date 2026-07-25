package com.rizowan.taskr.ui.tasks

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.rizowan.taskr.R
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.preferences.PreferencesManager
import com.rizowan.taskr.databinding.FragmentTasksBinding
import com.rizowan.taskr.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Fragment displaying the list of active tasks with filters.
 */
@AndroidEntryPoint
class TasksFragment : Fragment() {

    private var _binding: FragmentTasksBinding? = null
    private val binding get() = _binding!!

    private val viewModel: TasksViewModel by viewModels()

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private lateinit var taskAdapter: TaskAdapter
    private var hapticsEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTasksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        setupRecyclerView()
        setupFilterChips()
        observeUiState()
        observeHaptics()
    }

    /**
     * Setup RecyclerView with adapter.
     */
    private fun setupRecyclerView() {
        taskAdapter = TaskAdapter(
            onTaskClick = { taskWithSubTasks ->
                // Navigate to edit task
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
        val context = requireContext()
        binding.rvTasks.apply {
            adapter = taskAdapter
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            // Apply subtle layout animation
            layoutAnimation = AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        }
    }

    /**
     * Setup filter chips.
     */
    private fun setupFilterChips() {
        binding.chipAll.setOnClickListener {
            viewModel.setFilter(TaskFilter.ALL)
        }
        binding.chipToday.setOnClickListener {
            viewModel.setFilter(TaskFilter.TODAY)
        }
        binding.chipUpcoming.setOnClickListener {
            viewModel.setFilter(TaskFilter.UPCOMING)
        }
    }

    /**
     * Observe UI state from ViewModel.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    // Update greeting
                    binding.tvGreeting.text = state.greeting

                    // Update progress
                    binding.tvProgress.text = getString(
                        R.string.task_progress,
                        state.completedCount,
                        state.totalCount
                    )
                    val progress = if (state.totalCount > 0) {
                        (state.completedCount.toFloat() / state.totalCount * 100).toInt()
                    } else {
                        0
                    }
                    binding.progressBar.progress = progress

                    // Update filter chip selection
                    when (state.filter) {
                        TaskFilter.ALL -> binding.chipAll.isChecked = true
                        TaskFilter.TODAY -> binding.chipToday.isChecked = true
                        TaskFilter.UPCOMING -> binding.chipUpcoming.isChecked = true
                    }

                    // Update task list
                    taskAdapter.submitList(state.tasks)

                    // Show/hide empty state
                    if (state.tasks.isEmpty() && !state.isLoading) {
                        binding.rvTasks.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                        binding.tvEmptyMessage.text = when (state.filter) {
                            TaskFilter.ALL -> getString(R.string.no_tasks)
                            TaskFilter.TODAY -> getString(R.string.no_tasks_today)
                            TaskFilter.UPCOMING -> getString(R.string.no_upcoming_tasks)
                        }
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
