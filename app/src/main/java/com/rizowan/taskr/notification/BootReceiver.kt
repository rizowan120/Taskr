package com.rizowan.taskr.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.rizowan.taskr.data.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import javax.inject.Inject

/**
 * BroadcastReceiver for BOOT_COMPLETED to reschedule all alarms.
 * Uses goAsync() to extend receiver lifecycle for coroutine work.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var notificationScheduler: NotificationScheduler

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action == Intent.ACTION_BOOT_COMPLETED) {
            // Use goAsync() to extend BroadcastReceiver lifetime for coroutine work
            val pendingResult = goAsync()
            
            // Create a properly scoped coroutine with SupervisorJob for proper error handling
            val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
            scope.launch {
                try {
                    // Set a reasonable timeout to prevent hanging indefinitely
                    withTimeout(30_000L) {
                        val tasksWithReminders = taskRepository.getTasksWithReminders()
                        tasksWithReminders.forEach { task ->
                            notificationScheduler.scheduleNotification(task)
                        }
                    }
                } finally {
                    // Always finish the pending result to avoid ANR
                    pendingResult.finish()
                }
            }
        }
    }
}
