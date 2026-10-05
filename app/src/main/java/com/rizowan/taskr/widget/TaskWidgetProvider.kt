package com.rizowan.taskr.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import com.rizowan.taskr.R
import com.rizowan.taskr.ui.MainActivity

class TaskWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        val views = RemoteViews(context.packageName, R.layout.widget_tasks)

        // Title intent (opens app)
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_title_bar, pendingIntent)

        // Set up the collection
        val serviceIntent = Intent(context, TaskWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
        }
        views.setRemoteAdapter(R.id.widget_list_view, serviceIntent)
        views.setEmptyView(R.id.widget_list_view, R.id.widget_empty_view)

        // Template for item clicks
        val clickIntentTemplate = Intent(context, MainActivity::class.java)
        val clickPendingIntentTemplate = PendingIntent.getActivity(
            context, 1, clickIntentTemplate,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.widget_list_view, clickPendingIntentTemplate)

        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}
