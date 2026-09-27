package com.geecee.escapelauncher

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat

object LauncherNotificationManager {

    private const val CHANNEL_ID = "launcher_switcher_channel"
    private const val NOTIFICATION_ID = 1001

    fun showLauncherSwitcherNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Launcher Switcher",
                NotificationManager.IMPORTANCE_MIN // Low priority so it doesn't clutter status bar
            ).apply {
                description = "Provides a quick shortcut in the notification panel to switch launchers"
                setShowBadge(false)
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent(context, LauncherSwitcherActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_MULTIPLE_TASK
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_tile)
            .setContentTitle("Switch Launcher")
            .setContentText("Tap to change your default launcher")
            .setPriority(NotificationCompat.PRIORITY_MIN)
            .setOngoing(true) // Persistent
            .setContentIntent(pendingIntent)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun hideLauncherSwitcherNotification(context: Context) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(NOTIFICATION_ID)
    }
}
