package com.geecee.escapelauncher.core.data.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

import com.geecee.escapelauncher.core.model.ActiveNotification

class EscapeNotificationListenerService : NotificationListenerService() {

    companion object {
        private val _notifications = MutableStateFlow<List<ActiveNotification>>(emptyList())
        val notifications: StateFlow<List<ActiveNotification>> = _notifications.asStateFlow()

        fun clearNotifications() {
            _notifications.value = emptyList()
        }
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        sbn?.let { processSbn(it) }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification?) {
        super.onNotificationRemoved(sbn)
        sbn?.let { removeSbn(it) }
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        try {
            activeNotifications?.forEach { processSbn(it) }
        } catch (e: Exception) {
            // Ignore if permission lost or system issue
        }
    }

    private fun processSbn(sbn: StatusBarNotification) {
        val notification = sbn.notification ?: return
        val extras = notification.extras ?: return

        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString() ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        if (title.isBlank() && text.isBlank()) return

        val packageName = sbn.packageName ?: ""
        
        // Filter out non-essential apps if needed, but keeping messaging/important apps
        val appName = try {
            packageManager.getApplicationLabel(packageManager.getApplicationInfo(packageName, 0)).toString()
        } catch (e: Exception) {
            packageName
        }

        val item = ActiveNotification(
            id = "${sbn.key}",
            packageName = packageName,
            appName = appName,
            title = title,
            text = text,
            postTime = sbn.postTime
        )

        val current = _notifications.value.filterNot { it.id == item.id }.toMutableList()
        current.add(0, item)
        _notifications.value = current.take(15) // Keep top 15
    }

    private fun removeSbn(sbn: StatusBarNotification) {
        _notifications.value = _notifications.value.filterNot { it.id == sbn.key }
    }
}
