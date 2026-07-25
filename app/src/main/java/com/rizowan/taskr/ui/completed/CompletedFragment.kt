package com.rizowan.taskr.ui.completed

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rizowan.taskr.R
import com.rizowan.taskr.databinding.FragmentCompletedBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment displaying completed tasks grouped by date.
 */
@AndroidEntryPoint
class CompletedFragment : Fragment() {

    private var _binding: FragmentCompletedBinding? = null
    private val binding get() = _binding!!

    private val viewModel: CompletedViewModel by viewModels()
    private lateinit var completedAdapter: CompletedAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCompletedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupClearAllButton()
        observeUiState()
    }

    /**
     * Setup RecyclerView.
     */
    private fun setupRecyclerView() {
        completedAdapter = CompletedAdapter()
        val context = requireContext()
        binding.rvCompletedTasks.apply {
            adapter = completedAdapter
            // Apply subtle layout animation
            layoutAnimation = android.view.animation.AnimationUtils.loadLayoutAnimation(context, R.anim.layout_animation_fall_down)
        }
    }

    /**
     * Setup clear all button.
     */
    private fun setupClearAllButton() {
        binding.btnClearAll.setOnClickListener {
            showClearConfirmation()
        }
    }

    /**
     * Show clear all confirmation dialog.
     */
    private fun showClearConfirmation() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.clear_completed_confirm)
            .setMessage(R.string.clear_completed_message)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.clear_all) { _, _ ->
                viewModel.clearAllCompletedTasks()
            }
            .show()
    }

    /**
     * Observe UI state.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    completedAdapter.submitList(state.items)

                    // Show/hide clear all button
                    binding.btnClearAll.visibility = if (state.hasCompletedTasks) View.VISIBLE else View.GONE

                    // Show/hide empty state
                    if (state.items.isEmpty()) {
                        binding.rvCompletedTasks.visibility = View.GONE
                        binding.emptyState.visibility = View.VISIBLE
                    } else {
                        binding.rvCompletedTasks.visibility = View.VISIBLE
                        binding.emptyState.visibility = View.GONE
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
