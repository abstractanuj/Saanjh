package com.geecee.escapelauncher.core.data.repository

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

data class ActiveNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long
)

@Singleton
class NotificationRepository @Inject constructor() {
    private val _notifications = MutableStateFlow<List<ActiveNotification>>(emptyList())
    val notifications: StateFlow<List<ActiveNotification>> = _notifications.asStateFlow()

    fun addNotification(notification: ActiveNotification) {
        val current = _notifications.value.filterNot { it.id == notification.id }.toMutableList()
        current.add(0, notification)
        _notifications.value = current.take(20) // Keep latest 20
    }

    fun removeNotification(id: String) {
        _notifications.value = _notifications.value.filterNot { it.id == id }
    }

    fun clearAll() {
        _notifications.value = emptyList()
    }
}
