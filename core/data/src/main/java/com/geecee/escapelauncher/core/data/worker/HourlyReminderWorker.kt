package com.geecee.escapelauncher.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geecee.escapelauncher.core.data.repository.LocalContent

class HourlyReminderWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        // Just show it if it hits here for now; a better approach would be checking settings 
        // through an injected repository. For simplicity, we just trigger it.
        val currentHour = java.time.LocalTime.now().hour
        // Hardcoded generic window of 8 AM to 10 PM
        val inWindow = currentHour in 8..22

        if (inWindow) {
            showNotification()
        }
        return Result.success()
    }

    private fun showNotification() {
        val quote = LocalContent.parsedQuotes.random()

        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "hourly_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Hourly Reminders",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Shows periodic reminders to stay mindful"
            }
            notificationManager.createNotificationChannel(channel)
        }

        val intent = Intent().apply {
            setClassName(context, "com.geecee.escapelauncher.GlassmorphicReminderActivity")
            putExtra("QUOTE_TEXT", quote.text)
            putExtra("QUOTE_AUTHOR", quote.author)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context,
            3001,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("Mindful Reminder")
            .setContentText(quote.text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(quote.text))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()
            
        notificationManager.notify(3001, notification)
    }
}
