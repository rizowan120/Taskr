package com.rizowan.taskr.ui.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.rizowan.taskr.BuildConfig
import com.rizowan.taskr.R
import com.rizowan.taskr.databinding.FragmentSettingsBinding
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Fragment for app settings.
 */
@AndroidEntryPoint
class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: SettingsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupClickListeners()
        observeUiState()
    }

    /**
     * Setup click listeners for settings items.
     */
    private fun setupClickListeners() {
        // Name setting
        binding.layoutName.setOnClickListener {
            showNameDialog()
        }

        // Theme setting
        binding.layoutTheme.setOnClickListener {
            showThemeDialog()
        }

        // Notifications switch
        binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setNotifications(isChecked)
        }

        // Haptics switch
        binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
            viewModel.setHaptics(isChecked)
        }

        // About
        binding.layoutAbout.setOnClickListener {
            findNavController().navigate(R.id.aboutFragment)
        }
    }

    /**
     * Show name input dialog.
     */
    private fun showNameDialog() {
        val editText = EditText(requireContext()).apply {
            setText(viewModel.uiState.value.userName)
            hint = getString(R.string.your_name)
            setSingleLine()
        }

        val container = FrameLayout(requireContext()).apply {
            val padding = resources.getDimensionPixelSize(R.dimen.spacing_lg)
            setPadding(padding, padding / 2, padding, 0)
            addView(editText)
        }

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.your_name)
            .setView(container)
            .setNegativeButton(R.string.cancel, null)
            .setPositiveButton(R.string.save) { _, _ ->
                viewModel.setUserName(editText.text.toString().trim())
            }
            .show()
    }

    /**
     * Show theme selection dialog.
     */
    private fun showThemeDialog() {
        val themes = ThemeOption.values()
        val currentIndex = themes.indexOf(viewModel.uiState.value.themeOption)
        val themeNames = themes.map { it.displayName }.toTypedArray()

        MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.theme)
            .setSingleChoiceItems(themeNames, currentIndex) { dialog, which ->
                val selected = themes[which]
                viewModel.setTheme(selected)
                applyTheme(selected)
                dialog.dismiss()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    /**
     * Apply theme immediately.
     */
    private fun applyTheme(option: ThemeOption) {
        val nightMode = when (option) {
            ThemeOption.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            ThemeOption.DARK -> AppCompatDelegate.MODE_NIGHT_YES
            ThemeOption.SYSTEM -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
        }
        AppCompatDelegate.setDefaultNightMode(nightMode)
    }

    /**
     * Observe UI state.
     */
    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    binding.tvUserName.text = state.userName.ifBlank { 
                        getString(R.string.not_set) 
                    }
                    binding.tvTheme.text = state.themeOption.displayName
                    
                    // Set switch states without triggering listeners
                    binding.switchNotifications.setOnCheckedChangeListener(null)
                    binding.switchHaptics.setOnCheckedChangeListener(null)
                    
                    binding.switchNotifications.isChecked = state.notificationsEnabled
                    binding.switchHaptics.isChecked = state.hapticsEnabled
                    
                    // Re-attach listeners
                    binding.switchNotifications.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.setNotifications(isChecked)
                    }
                    binding.switchHaptics.setOnCheckedChangeListener { _, isChecked ->
                        viewModel.setHaptics(isChecked)
                    }
                    
                    binding.tvVersion.text = getString(R.string.version, BuildConfig.VERSION_NAME)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
