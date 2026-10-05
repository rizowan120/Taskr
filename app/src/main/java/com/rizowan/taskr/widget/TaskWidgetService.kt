package com.rizowan.taskr.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.rizowan.taskr.R
import com.rizowan.taskr.data.local.TaskrDatabase
import com.rizowan.taskr.data.local.entity.Task
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import javax.inject.Inject

class TaskWidgetService : RemoteViewsService() {
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskWidgetFactory(this.applicationContext)
    }
}

class TaskWidgetFactory(private val context: Context) : RemoteViewsService.RemoteViewsFactory {
    
    // We can't inject easily into RemoteViewsFactory, so we use an EntryPoint or build the DB manually
    private lateinit var database: TaskrDatabase
    private var tasks: List<Task> = emptyList()

    override fun onCreate() {
        database = androidx.room.Room.databaseBuilder(
            context,
            TaskrDatabase::class.java,
            "taskr_database"
        ).addMigrations(TaskrDatabase.MIGRATION_1_2).build()
    }

    override fun onDataSetChanged() {
        runBlocking {
            val (startOfDay, endOfDay) = com.rizowan.taskr.util.DateUtils.getTodayRange()
            tasks = database.taskDao().getTasksDueTodaySync(startOfDay, endOfDay)
        }
    }

    override fun onDestroy() {}

    override fun getCount(): Int = tasks.size

    override fun getViewAt(position: Int): RemoteViews {
        val task = tasks[position]
        val views = RemoteViews(context.packageName, R.layout.widget_item_task)
        
        views.setTextViewText(R.id.widget_task_title, task.title)
        
        val fillInIntent = Intent().apply {
            putExtra("taskId", task.id)
        }
        views.setOnClickFillInIntent(R.id.widget_item_root, fillInIntent)
        
        return views
    }

    override fun getLoadingView(): RemoteViews? = null
    override fun getViewTypeCount(): Int = 1
    override fun getItemId(position: Int): Long = tasks[position].id
    override fun hasStableIds(): Boolean = true
}
