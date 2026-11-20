package com.geecee.escapelauncher.feature.widgets.greeting

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.TypedValue
import android.view.View
import android.widget.RemoteViews
import com.geecee.escapelauncher.feature.widgets.R

class AnujGreetingWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId, forceRandom = false)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId, options = newOptions, forceRandom = false)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_CYCLE_GREETING -> {
                val widgetId = intent.getIntExtra(
                    AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID
                )
                val manager = AppWidgetManager.getInstance(context)
                if (widgetId != AppWidgetManager.INVALID_APPWIDGET_ID) {
                    updateWidget(context, manager, widgetId, forceRandom = true)
                } else {
                    refreshAll(context, forceRandom = true)
                }
            }
            Intent.ACTION_TIME_CHANGED,
            Intent.ACTION_TIMEZONE_CHANGED,
            Intent.ACTION_DATE_CHANGED,
            Intent.ACTION_USER_PRESENT -> {
                refreshAll(context, forceRandom = false)
            }
        }
    }

    companion object {
        const val ACTION_CYCLE_GREETING = "com.sareprne.saanjh.action.CYCLE_ANUJ_GREETING"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null,
            forceRandom: Boolean = false
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_anuj_greeting)
            val currentOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
            val minW = currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, 220)
            val minH = currentOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, 70)

            val greeting = AnujGreetingEngine.nextGreeting(
                context = context,
                widgetKey = "minimal_$appWidgetId",
                forceNewRandom = forceRandom
            )

            // Dynamically adapt typography & phase visibility to any user resize
            val textSizeSp = when {
                minH < 58 || minW < 140 -> 15f
                minH < 88 || minW < 210 -> 19f
                minH < 140 -> 23f
                else -> 27f
            }
            val showPhase = minH >= 64 && minW >= 140

            views.setViewVisibility(
                R.id.anuj_greeting_phase,
                if (showPhase) View.VISIBLE else View.GONE
            )
            views.setTextViewText(R.id.anuj_greeting_phase, greeting.phaseLabel)
            views.setTextViewText(R.id.anuj_greeting_text, greeting.message)
            views.setTextViewTextSize(
                R.id.anuj_greeting_text,
                TypedValue.COMPLEX_UNIT_SP,
                textSizeSp
            )

            // Tap to randomize to another pleasant greeting for the current hour
            val tapIntent = Intent(context, AnujGreetingWidgetProvider::class.java).apply {
                action = ACTION_CYCLE_GREETING
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.anuj_greeting_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAll(context: Context, forceRandom: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, AnujGreetingWidgetProvider::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id, forceRandom = forceRandom)
            }
        }
    }
}
