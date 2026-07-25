package com.rizowan.taskr.ui.tasks

import android.graphics.Paint
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rizowan.taskr.R
import com.rizowan.taskr.data.local.entity.Priority
import com.rizowan.taskr.data.local.entity.TaskWithSubTasks
import com.rizowan.taskr.databinding.ItemTaskBinding
import com.rizowan.taskr.util.DateUtils

/**
 * Adapter for displaying tasks in a RecyclerView.
 */
class TaskAdapter(
    private val onTaskClick: (TaskWithSubTasks) -> Unit,
    private val onTaskCheckedChange: (TaskWithSubTasks, Boolean) -> Unit
) : ListAdapter<TaskWithSubTasks, TaskAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class TaskViewHolder(
        private val binding: ItemTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        init {
            binding.root.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onTaskClick(getItem(position))
                }
            }

            binding.checkboxTask.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val task = getItem(position)
                    onTaskCheckedChange(task, !task.task.isCompleted)
                }
            }
        }

        fun bind(taskWithSubTasks: TaskWithSubTasks) {
            val task = taskWithSubTasks.task
            val subTasks = taskWithSubTasks.subTasks
            val context = binding.root.context

            // Task title
            binding.tvTaskTitle.text = task.title

            // Checkbox state
            binding.checkboxTask.isChecked = task.isCompleted

            // Strike-through if completed
            if (task.isCompleted) {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                binding.tvTaskTitle.alpha = 0.6f
            } else {
                binding.tvTaskTitle.paintFlags = binding.tvTaskTitle.paintFlags and Paint.STRIKE_THRU_TEXT_FLAG.inv()
                binding.tvTaskTitle.alpha = 1f
            }

            // Priority indicator color
            val priorityColor = when (task.priority) {
                Priority.LOW -> R.color.priority_low
                Priority.MEDIUM -> R.color.priority_medium
                Priority.HIGH -> R.color.priority_high
            }
            val drawable = binding.priorityIndicator.background as? GradientDrawable
            drawable?.setColor(ContextCompat.getColor(context, priorityColor))

            // Due date/time
            if (task.dueDate != null) {
                binding.layoutDueInfo.visibility = View.VISIBLE
                val dateStr = DateUtils.getRelativeDateString(task.dueDate)
                val timeStr = task.dueTime?.let { DateUtils.formatTime(it) }
                binding.tvDueDate.text = if (timeStr != null) "$dateStr, $timeStr" else dateStr

                // Show overdue color
                if (DateUtils.isOverdue(task.dueDate) && !task.isCompleted) {
                    binding.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.error))
                } else {
                    binding.tvDueDate.setTextColor(ContextCompat.getColor(context, R.color.text_hint))
                }
            } else {
                binding.layoutDueInfo.visibility = View.GONE
            }

            // Repeat icon
            binding.ivRepeat.visibility = if (task.isRepeating) View.VISIBLE else View.GONE

            // Subtasks count
            if (subTasks.isNotEmpty()) {
                val completedCount = subTasks.count { it.isCompleted }
                binding.tvSubtasksCount.visibility = View.VISIBLE
                binding.tvSubtasksCount.text = "$completedCount/${subTasks.size}"
            } else {
                binding.tvSubtasksCount.visibility = View.GONE
            }
        }
    }

    /**
     * DiffUtil callback for efficient list updates.
     */
    class TaskDiffCallback : DiffUtil.ItemCallback<TaskWithSubTasks>() {
        override fun areItemsTheSame(oldItem: TaskWithSubTasks, newItem: TaskWithSubTasks): Boolean {
            return oldItem.task.id == newItem.task.id
        }

        override fun areContentsTheSame(oldItem: TaskWithSubTasks, newItem: TaskWithSubTasks): Boolean {
            return oldItem == newItem
        }
    }
}
