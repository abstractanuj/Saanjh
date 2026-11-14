package com.geecee.escapelauncher.core.model

data class ActiveNotification(
    val id: String,
    val packageName: String,
    val appName: String,
    val title: String,
    val text: String,
    val postTime: Long
)
