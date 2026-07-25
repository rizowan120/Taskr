package com.rizowan.taskr.util

import android.os.Build
import android.view.HapticFeedbackConstants
import android.view.View

/**
 * Utility class for haptic feedback operations.
 * Uses OEM-safe haptic feedback through View.performHapticFeedback().
 */
object HapticUtils {

    /**
     * Perform haptic feedback for task completion.
     * Uses CONFIRM constant when available (API 30+), falls back to VIRTUAL_KEY.
     */
    fun performConfirmFeedback(view: View, enabled: Boolean) {
        if (!enabled) return
        
        val feedbackConstant = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            HapticFeedbackConstants.CONFIRM
        } else {
            HapticFeedbackConstants.VIRTUAL_KEY
        }
        
        view.performHapticFeedback(
            feedbackConstant,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Perform haptic feedback for button/FAB press.
     * Uses KEYBOARD_TAP for a subtle tap feeling.
     */
    fun performTapFeedback(view: View, enabled: Boolean) {
        if (!enabled) return
        
        view.performHapticFeedback(
            HapticFeedbackConstants.KEYBOARD_TAP,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }

    /**
     * Perform haptic feedback for long press actions.
     */
    fun performLongPressFeedback(view: View, enabled: Boolean) {
        if (!enabled) return
        
        view.performHapticFeedback(
            HapticFeedbackConstants.LONG_PRESS,
            HapticFeedbackConstants.FLAG_IGNORE_GLOBAL_SETTING
        )
    }
}
