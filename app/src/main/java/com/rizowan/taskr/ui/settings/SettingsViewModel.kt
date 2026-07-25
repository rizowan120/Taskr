package com.rizowan.taskr.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rizowan.taskr.data.preferences.PreferencesManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.rizowan.taskr.data.preferences.ThemeMode

/**
 * Theme options.
 */
enum class ThemeOption(val displayName: String) {
    SYSTEM("System default"),
    LIGHT("Light"),
    DARK("Dark")
}

/**
 * UI State for Settings screen.
 */
data class SettingsUiState(
    val userName: String = "",
    val themeOption: ThemeOption = ThemeOption.SYSTEM,
    val notificationsEnabled: Boolean = true,
    val hapticsEnabled: Boolean = true
)

/**
 * ViewModel for Settings screen.
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesManager: PreferencesManager
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        preferencesManager.userName,
        preferencesManager.themeMode,
        preferencesManager.notificationsEnabled,
        preferencesManager.hapticsEnabled
    ) { userName, themeMode, notifications, haptics ->
        SettingsUiState(
            userName = userName,
            themeOption = when (themeMode) {
                ThemeMode.LIGHT -> ThemeOption.LIGHT
                ThemeMode.DARK -> ThemeOption.DARK
                ThemeMode.SYSTEM -> ThemeOption.SYSTEM
            },
            notificationsEnabled = notifications,
            hapticsEnabled = haptics
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )

    /**
     * Update user name.
     */
    fun setUserName(name: String) {
        viewModelScope.launch {
            preferencesManager.setUserName(name)
        }
    }

    /**
     * Update theme.
     */
    fun setTheme(option: ThemeOption) {
        viewModelScope.launch {
            val mode = when (option) {
                ThemeOption.LIGHT -> ThemeMode.LIGHT
                ThemeOption.DARK -> ThemeMode.DARK
                ThemeOption.SYSTEM -> ThemeMode.SYSTEM
            }
            preferencesManager.setThemeMode(mode)
        }
    }

    /**
     * Update notifications setting.
     */
    fun setNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setNotificationsEnabled(enabled)
        }
    }

    /**
     * Update haptics setting.
     */
    fun setHaptics(enabled: Boolean) {
        viewModelScope.launch {
            preferencesManager.setHapticsEnabled(enabled)
        }
    }
}
