package com.rizowan.taskr.notification

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import com.rizowan.taskr.data.local.entity.Task
import com.rizowan.taskr.data.preferences.PreferencesManager
import com.rizowan.taskr.util.DateUtils
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Utility class for scheduling and canceling task notifications.
 */
@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val preferencesManager: PreferencesManager
) {
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

    /**
     * Schedule a notification for a task.
     * This is a suspend function to avoid blocking the main thread.
     */
    suspend fun scheduleNotification(task: Task) {
        // Check if notifications are enabled
        val notificationsEnabled = preferencesManager.notificationsEnabled.first()
        if (!notificationsEnabled) return

        // Task must have due date and time
        if (task.dueDate == null || task.dueTime == null) return

        val triggerTime = DateUtils.combineDateTime(task.dueDate, task.dueTime)
        
        // Don't schedule if time is in the past
        if (triggerTime <= System.currentTimeMillis()) return

        val intent = Intent(context, AlarmReceiver::class.java).apply {
            putExtra(AlarmReceiver.EXTRA_TASK_ID, task.id)
            putExtra(AlarmReceiver.EXTRA_TASK_TITLE, task.title)
        }

        // Use hashCode() to safely convert Long to Int without overflow risk
        val requestCode = task.id.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule exact alarm
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                if (alarmManager.canScheduleExactAlarms()) {
                    alarmManager.setExactAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                } else {
                    alarmManager.setAndAllowWhileIdle(
                        AlarmManager.RTC_WAKEUP,
                        triggerTime,
                        pendingIntent
                    )
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            } else {
                alarmManager.setExact(
                    AlarmManager.RTC_WAKEUP,
                    triggerTime,
                    pendingIntent
                )
            }
        } catch (e: SecurityException) {
            // Permission not granted, silently fail
        }
    }

    /**
     * Cancel a scheduled notification for a task.
     */
    fun cancelNotification(taskId: Long) {
        val intent = Intent(context, AlarmReceiver::class.java)
        // Use hashCode() for consistency with scheduleNotification
        val requestCode = taskId.hashCode()
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    /**
     * Cancel all scheduled notifications.
     * Called when notifications are disabled globally.
     */
    fun cancelAllNotifications(tasks: List<Task>) {
        tasks.forEach { task ->
            cancelNotification(task.id)
        }
    }
}
