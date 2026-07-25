package com.rizowan.taskr.ui.addedittask

import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.rizowan.taskr.data.local.entity.SubTask
import com.rizowan.taskr.databinding.ItemSubtaskEditBinding

/**
 * Adapter for editing subtasks in the Add/Edit Task screen.
 */
class SubtaskEditAdapter(
    private val onSubtaskCheckedChange: (Int, Boolean) -> Unit,
    private val onSubtaskTitleChange: (Int, String) -> Unit,
    private val onSubtaskDelete: (Int) -> Unit
) : ListAdapter<SubTask, SubtaskEditAdapter.SubtaskViewHolder>(SubtaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubtaskViewHolder {
        val binding = ItemSubtaskEditBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SubtaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SubtaskViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class SubtaskViewHolder(
        private val binding: ItemSubtaskEditBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        private var textWatcher: TextWatcher? = null

        fun bind(subTask: SubTask) {
            // Remove previous text watcher to prevent loops
            textWatcher?.let { binding.etSubtaskTitle.removeTextChangedListener(it) }

            binding.checkboxSubtask.isChecked = subTask.isCompleted
            binding.etSubtaskTitle.setText(subTask.title)

            binding.checkboxSubtask.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubtaskCheckedChange(position, !subTask.isCompleted)
                }
            }

            textWatcher = object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
                override fun afterTextChanged(s: Editable?) {
                    val position = bindingAdapterPosition
                    if (position != RecyclerView.NO_POSITION) {
                        onSubtaskTitleChange(position, s?.toString() ?: "")
                    }
                }
            }
            binding.etSubtaskTitle.addTextChangedListener(textWatcher)

            binding.btnDeleteSubtask.setOnClickListener {
                val position = bindingAdapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onSubtaskDelete(position)
                }
            }
        }
    }

    class SubtaskDiffCallback : DiffUtil.ItemCallback<SubTask>() {
        override fun areItemsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            // Use position-based comparison for new subtasks (id = 0)
            return if (oldItem.id == 0L && newItem.id == 0L) {
                oldItem === newItem
            } else {
                oldItem.id == newItem.id
            }
        }

        override fun areContentsTheSame(oldItem: SubTask, newItem: SubTask): Boolean {
            return oldItem == newItem
        }
    }
}
