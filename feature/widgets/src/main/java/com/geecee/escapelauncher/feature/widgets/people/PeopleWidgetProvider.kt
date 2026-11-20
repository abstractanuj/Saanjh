package com.geecee.escapelauncher.feature.widgets.people

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.TypedValue
import android.widget.RemoteViews
import com.geecee.escapelauncher.core.data.repository.DirectCallManager
import com.geecee.escapelauncher.core.data.repository.PeopleRepository
import com.geecee.escapelauncher.feature.widgets.R

class PeopleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        updateWidget(context, appWidgetManager, appWidgetId, newOptions)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        when (intent.action) {
            ACTION_METRO_TILE_TAP -> {
                val phone = intent.getStringExtra(DirectCallManager.EXTRA_PHONE_NUMBER)?.trim().orEmpty()
                val name = intent.getStringExtra(DirectCallManager.EXTRA_CONTACT_NAME)?.trim().orEmpty()
                if (phone.isNotEmpty()) {
                    handleTileTap(context, phone, name)
                }
            }
            PeopleRepository.ACTION_REFRESH_PEOPLE_WIDGET -> {
                refreshAllWidgets(context)
            }
        }
    }

    private fun handleTileTap(context: Context, phone: String, name: String) {
        val prefs = context.getSharedPreferences(TAP_PREFS, Context.MODE_PRIVATE)
        val lastPhone = prefs.getString(KEY_LAST_TAP_PHONE, null)
        val lastTime = prefs.getLong(KEY_LAST_TAP_TIME, 0L)
        val now = System.currentTimeMillis()

        if (lastPhone == phone && (now - lastTime) in 1..DOUBLE_TAP_WINDOW_MS) {
            // Double-click / double-tap confirmed -> place call immediately
            prefs.edit().remove(KEY_LAST_TAP_PHONE).remove(KEY_LAST_TAP_TIME).apply()
            DirectCallManager.placeCallImmediately(
                context = context,
                phoneNumber = phone,
                contactName = name
            )
            refreshAllWidgets(context)
        } else {
            prefs.edit()
                .putString(KEY_LAST_TAP_PHONE, phone)
                .putLong(KEY_LAST_TAP_TIME, now)
                .apply()
        }
    }

    companion object {
        const val ACTION_METRO_TILE_TAP = "com.sareprne.saanjh.action.METRO_TILE_TAP"
        const val ACTION_OPEN_PEOPLE_SETUP = "com.sareprne.saanjh.action.OPEN_PEOPLE_SETUP"
        private const val TAP_PREFS = "saanjh_metro_tap_state"
        private const val KEY_LAST_TAP_PHONE = "last_tap_phone"
        private const val KEY_LAST_TAP_TIME = "last_tap_time"
        private const val DOUBLE_TAP_WINDOW_MS = 900L

        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int,
            options: Bundle? = null
        ) {
            val views = RemoteViews(context.packageName, R.layout.widget_people_metro)
            val currentOptions = options ?: appWidgetManager.getAppWidgetOptions(appWidgetId)
            val (widgetWidthDp, widgetHeightDp) = AtomCloudRenderer.resolveWidgetSizeDp(
                options = currentOptions,
                defaultWidthDp = 240f,
                defaultHeightDp = 150f
            )

            // Dynamically adapt column width to any user resize so it remains scrollable at any width/height
            val dynamicColWidthDp = when {
                widgetWidthDp < 150f -> (widgetWidthDp - 32f).coerceAtLeast(72f)
                widgetWidthDp < 270f -> ((widgetWidthDp - 36f) / 2f).coerceAtLeast(96f)
                else -> 118f
            }
            val colWidthPx = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dynamicColWidthDp,
                context.resources.displayMetrics
            ).toInt()
            views.setInt(R.id.people_metro_grid, "setColumnWidth", colWidthPx)

            // Render a size-aware Atom Cloud bitmap with a strict inner safety margin for the current widget size
            val cloudBitmap = AtomCloudRenderer.createCommonAtomCloudBitmap(
                widgetWidthDp = widgetWidthDp,
                widgetHeightDp = widgetHeightDp
            )
            views.setImageViewBitmap(R.id.people_common_cloud_bg, cloudBitmap)

            val serviceIntent = Intent(context, PeopleWidgetService::class.java).apply {
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            views.setRemoteAdapter(R.id.people_metro_grid, serviceIntent)
            views.setEmptyView(R.id.people_metro_grid, R.id.people_empty_view)

            // Template PendingIntent for double-tapping any contact inside the shared Atom Cloud
            val tapTemplateIntent = Intent(context, PeopleWidgetProvider::class.java).apply {
                action = ACTION_METRO_TILE_TAP
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                data = Uri.parse(toUri(Intent.URI_INTENT_SCHEME))
            }
            val tapPendingIntent = PendingIntent.getBroadcast(
                context,
                appWidgetId,
                tapTemplateIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
            views.setPendingIntentTemplate(R.id.people_metro_grid, tapPendingIntent)

            // Empty view click opens FavoriteContactsActivity so the user can choose contacts
            val setupIntent = Intent(ACTION_OPEN_PEOPLE_SETUP).apply {
                setPackage(context.packageName)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            val setupPendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId + 10000,
                setupIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.people_empty_view, setupPendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.people_metro_grid)
        }

        fun refreshAllWidgets(context: Context) {
            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PeopleWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                for (id in ids) {
                    updateWidget(context, manager, id)
                }
            }
        }
    }
}
