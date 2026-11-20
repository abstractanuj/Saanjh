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
import com.geecee.escapelauncher.feature.widgets.people.AtomCloudRenderer

class AnujCloudGreetingWidgetProvider : AppWidgetProvider() {

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
            ACTION_CYCLE_CLOUD_GREETING -> {
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
        const val ACTION_CYCLE_CLOUD_GREETING = "com.sareprne.saanjh.action.CYCLE_ANUJ_CLOUD_GREETING"

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null,
            forceRandom: Boolean = false
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_anuj_cloud_greeting)
            val currentOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
            val (widgetWidthDp, widgetHeightDp) = AtomCloudRenderer.resolveWidgetSizeDp(
                options = currentOptions,
                defaultWidthDp = 240f,
                defaultHeightDp = 110f
            )

            val greeting = AnujGreetingEngine.nextGreeting(
                context = context,
                widgetKey = "cloud_$appWidgetId",
                forceNewRandom = forceRandom
            )

            // Render a size-aware Atom Cloud bitmap with a strict inner safety margin for the current widget size
            val cloudBitmap = AtomCloudRenderer.createCommonAtomCloudBitmap(
                widgetWidthDp = widgetWidthDp,
                widgetHeightDp = widgetHeightDp
            )
            views.setImageViewBitmap(R.id.anuj_greeting_cloud_bg, cloudBitmap)

            val textSizeSp = when {
                widgetHeightDp < 64f || widgetWidthDp < 140f -> 14f
                widgetHeightDp < 96f || widgetWidthDp < 210f -> 17.5f
                widgetHeightDp < 150f -> 21f
                else -> 25f
            }
            val showPhase = widgetHeightDp >= 72f && widgetWidthDp >= 140f

            views.setViewVisibility(
                R.id.anuj_cloud_greeting_phase,
                if (showPhase) View.VISIBLE else View.GONE
            )
            views.setTextViewText(R.id.anuj_cloud_greeting_phase, greeting.phaseLabel)
            views.setTextViewText(R.id.anuj_cloud_greeting_text, greeting.message)
            views.setTextViewTextSize(
                R.id.anuj_cloud_greeting_text,
                TypedValue.COMPLEX_UNIT_SP,
                textSizeSp
            )

            val tapIntent = Intent(context, AnujCloudGreetingWidgetProvider::class.java).apply {
                action = ACTION_CYCLE_CLOUD_GREETING
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId + 5000,
                tapIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.anuj_cloud_greeting_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }

        fun refreshAll(context: Context, forceRandom: Boolean) {
            val manager = AppWidgetManager.getInstance(context)
            val ids = manager.getAppWidgetIds(
                ComponentName(context, AnujCloudGreetingWidgetProvider::class.java)
            )
            for (id in ids) {
                updateWidget(context, manager, id, forceRandom = forceRandom)
            }
        }
    }
}
