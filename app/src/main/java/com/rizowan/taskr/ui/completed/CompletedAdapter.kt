package com.rizowan.taskr.ui.completed

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rizowan.taskr.databinding.ItemCompletedTaskBinding
import com.rizowan.taskr.databinding.ItemSectionHeaderBinding
import com.rizowan.taskr.util.DateUtils

/**
 * Adapter for completed tasks list with section headers.
 */
class CompletedAdapter : ListAdapter<CompletedListItem, RecyclerView.ViewHolder>(CompletedDiffCallback()) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_TASK = 1
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is CompletedListItem.Header -> TYPE_HEADER
            is CompletedListItem.TaskItem -> TYPE_TASK
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val binding = ItemSectionHeaderBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                HeaderViewHolder(binding)
            }
            else -> {
                val binding = ItemCompletedTaskBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
                TaskViewHolder(binding)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (val item = getItem(position)) {
            is CompletedListItem.Header -> (holder as HeaderViewHolder).bind(item)
            is CompletedListItem.TaskItem -> (holder as TaskViewHolder).bind(item)
        }
    }

    class HeaderViewHolder(
        private val binding: ItemSectionHeaderBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CompletedListItem.Header) {
            binding.tvHeader.text = item.title
        }
    }

    class TaskViewHolder(
        private val binding: ItemCompletedTaskBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: CompletedListItem.TaskItem) {
            val task = item.taskWithSubTasks.task
            binding.tvTaskTitle.text = task.title
            binding.tvCompletedAt.text = task.completedAt?.let { 
                "Completed at ${DateUtils.formatTime(it)}" 
            } ?: ""
        }
    }

    class CompletedDiffCallback : DiffUtil.ItemCallback<CompletedListItem>() {
        override fun areItemsTheSame(oldItem: CompletedListItem, newItem: CompletedListItem): Boolean {
            return when {
                oldItem is CompletedListItem.Header && newItem is CompletedListItem.Header -> 
                    oldItem.title == newItem.title
                oldItem is CompletedListItem.TaskItem && newItem is CompletedListItem.TaskItem -> 
                    oldItem.taskWithSubTasks.task.id == newItem.taskWithSubTasks.task.id
                else -> false
            }
        }

        override fun areContentsTheSame(oldItem: CompletedListItem, newItem: CompletedListItem): Boolean {
            return oldItem == newItem
        }
    }
}
