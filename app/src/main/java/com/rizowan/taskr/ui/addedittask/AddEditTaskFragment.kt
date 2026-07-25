package com.rizowan.taskr.ui.addedittask

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import com.rizowan.taskr.R
import com.rizowan.taskr.data.local.entity.Priority
import com.rizowan.taskr.data.local.entity.RepeatType
import com.rizowan.taskr.databinding.FragmentAddEditTaskBinding
import com.rizowan.taskr.util.DateUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.Calendar

/**
 * Fragment for adding or editing a task.
 */
@AndroidEntryPoint
class AddEditTaskFragment : Fragment() {

    private var _binding: FragmentAddEditTaskBinding? = null
    private val binding get() = _binding!!

    private val viewModel: AddEditTaskViewModel by viewModels()
    private lateinit var subtaskAdapter: SubtaskEditAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddEditTaskBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupSubtasksList()
        setupFormFields()
        observeUiState()
        observeEvents()
        
        // Subtle entry animation
        view.alpha = 0f
        view.translationY = 50f
        view.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(400)
            .setInterpolator(android.view.animation.DecelerateInterpolator())
            .start()
    }

    /**
     * Setup toolbar with navigation and save button.
     */
    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        binding.btnSave.setOnClickListener {
            viewModel.saveTask()
        }
    }

    /**
     * Setup subtasks RecyclerView.
     */
    private fun setupSubtasksList() {
        subtaskAdapter = SubtaskEditAdapter(
            onSubtaskCheckedChange = { position, isCompleted ->
                viewModel.updateSubtaskCompletion(position, isCompleted)
            },
            onSubtaskTitleChange = { position, title ->
                viewModel.updateSubtaskTitle(position, title)
            },
            onSubtaskDelete = { position ->
                viewModel.removeSubtask(position)
            }
        )
        binding.rvSubtasks.adapter = subtaskAdapter
    }

    /**
     * Setup form field listeners.
     */
    private fun setupFormFields() {
        // Title
        binding.etTitle.addTextChangedListener { text ->
            viewModel.setTitle(text?.toString() ?: "")
        }

        // Description
        binding.etDescription.addTextChangedListener { text ->
            viewModel.setDescription(text?.toString() ?: "")
        }

        // Priority toggle group
        binding.togglePriority.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                val priority = when (checkedId) {
                    R.id.btnPriorityLow -> Priority.LOW
                    R.id.btnPriorityMedium -> Priority.MEDIUM
                    R.id.btnPriorityHigh -> Priority.HIGH
                    else -> Priority.LOW
                }
                viewModel.setPriority(priority)
            }
        }

        // Due date picker
        binding.btnDueDate.setOnClickListener {
            showDatePicker()
        }

        // Due time picker
        binding.btnDueTime.setOnClickListener {
            showTimePicker()
        }

        // Reminder switch
        binding.switchReminder.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setReminder(isChecked)
        }

        // Repeat switch
        binding.switchRepeat.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setRepeating(isChecked)
        }

        // Repeat type chips
        binding.chipDaily.setOnClickListener { viewModel.setRepeatType(RepeatType.DAILY) }
        binding.chipWeekly.setOnClickListener { viewModel.setRepeatType(RepeatType.WEEKLY) }
        binding.chipMonthly.setOnClickListener { viewModel.setRepeatType(RepeatType.MONTHLY) }

        // Add subtask
        binding.btnAddSubtask.setOnClickListener {
            val title = binding.etNewSubtask.text?.toString() ?: ""
            if (title.isNotBlank()) {
                viewModel.addSubtask(title)
                binding.etNewSubtask.text?.clear()
            }
        }

        binding.etNewSubtask.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                val title = binding.etNewSubtask.text?.toString() ?: ""
                if (title.isNotBlank()) {
                    viewModel.addSubtask(title)
                    binding.etNewSubtask.text?.clear()
                }
                true
            } else {
                false
            }
        }

        // Delete button
        binding.btnDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }

    /**
     * Show date picker dialog.
     */
    private fun showDatePicker() {
        val currentDate = viewModel.uiState.value.dueDate ?: System.currentTimeMillis()
        
        val datePicker = MaterialDatePicker.Builder.datePicker()
            .setTitleText(getString(R.string.due_date))
            .setSelection(currentDate)
            .build()

        datePicker.addOnPositiveButtonClickListener { selection ->
            viewModel.setDueDate(selection)
        }

        datePicker.show(parentFragmentManager, "datePicker")
    }

    /**
     * Show time picker dialog.
     */
    private fun showTimePicker() {
        val currentTime = viewModel.uiState.value.dueTime
        val calendar = Calendar.getInstance()
        if (currentTime != null) {
            calendar.timeInMillis = currentTime
        }

        val timePicker = MaterialTimePicker.Builder()
            .setTimeFormat(TimeFormat.CLOCK_12H)
            .setTitleText(getString(R.string.due_time))
            .setHour(calendar.get(Calendar.HOUR_OF_DAY))
            .setMinute(calendar.get(Calendar.MINUTE))
            .build()

        timePicker.addOnPositiveButtonClickListener {
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, timePicker.hour)
                set(Calendar.MINUTE, timePicker.minute)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            viewModel.setDueTime(selectedCalendar.timeInMillis)
        }

        timePicker.show(parentFragmentManager, "timePicker")
    }

    /**
     * Show delete confirmation dialog.
     */
    private fun showDeleteConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.delete_task)
            .setMessage(R.string.delete_task_confirm)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.delete) { _, _ ->
                viewModel.deleteTask()
            }
            .show()
    }

    /**
     * Observe UI state changes.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    updateUi(state)
                }
            }
        }
    }

    /**
     * Update UI based on state.
     */
    private fun updateUi(state: AddEditTaskUiState) {
        // Toolbar title
        binding.toolbar.title = if (state.isEditMode) {
            getString(R.string.edit_task)
        } else {
            getString(R.string.add_task)
        }

        // Save button state
        binding.btnSave.isEnabled = state.isSaveEnabled
        binding.btnSave.alpha = if (state.isSaveEnabled) 1f else 0.5f

        // Only update text fields if they differ (prevent cursor jumping)
        if (binding.etTitle.text.toString() != state.title) {
            binding.etTitle.setText(state.title)
        }
        if (binding.etDescription.text.toString() != state.description) {
            binding.etDescription.setText(state.description)
        }

        // Priority
        when (state.priority) {
            Priority.LOW -> binding.togglePriority.check(R.id.btnPriorityLow)
            Priority.MEDIUM -> binding.togglePriority.check(R.id.btnPriorityMedium)
            Priority.HIGH -> binding.togglePriority.check(R.id.btnPriorityHigh)
        }

        // Due date button
        binding.btnDueDate.text = state.dueDate?.let { DateUtils.formatDate(it) }
            ?: getString(R.string.set_date)

        // Due time button
        binding.btnDueTime.text = state.dueTime?.let { DateUtils.formatTime(it) }
            ?: getString(R.string.set_time)

        // Reminder section visibility (only when time is set)
        binding.layoutReminder.visibility = if (state.dueTime != null) View.VISIBLE else View.GONE
        binding.switchReminder.isChecked = state.hasReminder

        // Repeat
        binding.switchRepeat.isChecked = state.isRepeating
        binding.chipGroupRepeat.visibility = if (state.isRepeating && state.dueDate != null) View.VISIBLE else View.GONE
        
        when (state.repeatType) {
            RepeatType.DAILY -> binding.chipDaily.isChecked = true
            RepeatType.WEEKLY -> binding.chipWeekly.isChecked = true
            RepeatType.MONTHLY -> binding.chipMonthly.isChecked = true
        }

        // Subtasks
        subtaskAdapter.submitList(state.subTasks.toList())

        // Delete button (only in edit mode)
        binding.btnDelete.visibility = if (state.isEditMode) View.VISIBLE else View.GONE
    }

    /**
     * Observe one-time events.
     */
    private fun observeEvents() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    when (event) {
                        is AddEditTaskEvent.TaskSaved,
                        is AddEditTaskEvent.TaskDeleted -> {
                            findNavController().navigateUp()
                        }
                        is AddEditTaskEvent.ShowError -> {
                            binding.tilTitle.error = getString(event.messageResId)
                        }
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
