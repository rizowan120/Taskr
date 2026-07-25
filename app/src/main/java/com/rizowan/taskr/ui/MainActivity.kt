package com.rizowan.taskr.ui

import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.rizowan.taskr.R
import com.rizowan.taskr.data.preferences.PreferencesManager
import com.rizowan.taskr.data.preferences.ThemeMode
import com.rizowan.taskr.databinding.ActivityMainBinding
import com.rizowan.taskr.util.HapticUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Main Activity - Single activity architecture.
 * Hosts the NavHostFragment and manages bottom navigation with FAB.
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController

    @Inject
    lateinit var preferencesManager: PreferencesManager

    private var hapticsEnabled = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Apply theme before setContentView
        applyTheme()
        
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enableEdgeToEdge()
        setupWindowInsets()
        setupNavigation()
        setupFab()
        observePreferences()
    }

    /**
     * Apply theme based on saved preference.
     * Must be called synchronously before setContentView to avoid flicker.
     */
    private fun applyTheme() {
        // Read theme synchronously - this is acceptable here because:
        // 1. DataStore reads are fast for cached values
        // 2. This must complete before setContentView to avoid theme flicker
        val themeMode = kotlinx.coroutines.runBlocking {
            kotlinx.coroutines.withTimeoutOrNull(1000L) {
                preferencesManager.themeMode.first()
            } ?: ThemeMode.SYSTEM
        }
        when (themeMode) {
            ThemeMode.LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeMode.DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeMode.SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    /**
     * Setup window insets for edge-to-edge display.
     */
    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(binding.coordinatorLayout) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(0, systemBars.top, 0, 0)
            insets
        }
    }

    /**
     * Setup navigation with BottomNavigationView.
     */
    private fun setupNavigation() {
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.navHostFragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup bottom navigation without the placeholder item
        binding.bottomNavigation.setupWithNavController(navController)

        // Disable the placeholder item (center space for FAB)
        binding.bottomNavigation.menu.findItem(R.id.navigation_placeholder)?.isEnabled = false

        // Handle destination changes - show/hide bottom nav and FAB
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.addEditTaskFragment, R.id.aboutFragment -> {
                    binding.bottomAppBar.visibility = View.GONE
                    binding.fabAddTask.hide()
                }
                else -> {
                    binding.bottomAppBar.visibility = View.VISIBLE
                    binding.fabAddTask.show()
                }
            }
        }
    }

    /**
     * Setup FAB click listener.
     */
    private fun setupFab() {
        binding.fabAddTask.setOnClickListener {
            HapticUtils.performTapFeedback(it, hapticsEnabled)
            navController.navigate(R.id.addEditTaskFragment)
        }
    }

    /**
     * Observe preferences for haptics setting.
     */
    private fun observePreferences() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                preferencesManager.hapticsEnabled.collect { enabled ->
                    hapticsEnabled = enabled
                }
            }
        }
    }

    /**
     * Handle navigation up with NavController.
     */
    override fun onSupportNavigateUp(): Boolean {
        return navController.navigateUp() || super.onSupportNavigateUp()
    }
}
