package com.geecee.escapelauncher.feature.widgets.host

import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetHostView
import android.appwidget.AppWidgetProviderInfo
import android.content.Context
import android.widget.Toast
import timber.log.Timber

/**
 * A generalized AppWidgetHost for the Escape Launcher that manages widget lifecycle,
 * robust error handling, and memory cleanup.
 */
class EscapeAppWidgetHost(
    context: Context,
    hostId: Int
) : AppWidgetHost(context, hostId) {

    override fun onCreateView(
        context: Context,
        appWidgetId: Int,
        appWidget: AppWidgetProviderInfo?
    ): AppWidgetHostView {
        return EscapeAppWidgetHostView(context).apply {
            // Future configuration for generic styling (corners, margins)
        }
    }

    override fun stopListening() {
        super.stopListening()
        clearViews() // Prevent memory leaks on activity stop
    }

    override fun onProviderChanged(appWidgetId: Int, appWidgetInfo: AppWidgetProviderInfo?) {
        super.onProviderChanged(appWidgetId, appWidgetInfo)
        Timber.d("Widget provider changed for ID $appWidgetId")
    }
}

/**
 * Custom AppWidgetHostView to intercept touches, enforce scaling, and handle crash isolation.
 */
class EscapeAppWidgetHostView(context: Context) : AppWidgetHostView(context) {

    init {
        // Enforce padding or background logic globally if needed.
    }

    override fun getErrorView(): android.view.View {
        // Generalized error view for all crashed widgets
        val tv = android.widget.TextView(context)
        tv.text = context.getString(com.geecee.escapelauncher.core.ui.R.string.khoj_widget_crashed)
        tv.setTextColor(android.graphics.Color.RED)
        tv.setBackgroundColor(android.graphics.Color.parseColor("#33FF0000"))
        tv.gravity = android.view.Gravity.CENTER
        return tv
    }
}
