package com.geecee.escapelauncher.core.data.worker

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.geecee.escapelauncher.core.data.repository.LocalContent

class RecommendationWorker(
    private val context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {

    override suspend fun doWork(): Result {
        val currentHour = java.time.LocalTime.now().hour
        // Music logic (approx: 8 AM, 4 PM, 10 PM)
        if (currentHour in 7..9 || currentHour in 15..17 || currentHour in 21..23) {
            showMusicRecommendation(context)
        }
        
        // Movie logic (approx: 6 PM)
        if (currentHour in 17..19) {
            showMovieRecommendation(context)
        }
        
        return Result.success()
    }

    companion object {
        fun showRecommendationPreview(context: Context) {
            showMusicRecommendation(context)
            showMovieRecommendation(context)
        }
        
        fun showMusicRecommendation(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "recommendation_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Daily Recommendations",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Shows daily music and movie recommendations"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Music Recommendation
            val musicRec = LocalContent.parsedMusic.random()
            
            // Attempt to find Jukebox, fallback to general music intent
            val pm = context.packageManager
            val installedApps = pm.getInstalledApplications(android.content.pm.PackageManager.GET_META_DATA)
            val jukeboxApp = installedApps.find { it.packageName.contains("jukebox", ignoreCase = true) }
            
            val musicIntent = if (jukeboxApp != null) {
                pm.getLaunchIntentForPackage(jukeboxApp.packageName)?.apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                } ?: Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)
            } else {
                Intent.makeMainSelectorActivity(Intent.ACTION_MAIN, Intent.CATEGORY_APP_MUSIC)
            }
            
            val musicPendingIntent = PendingIntent.getActivity(
                context, 101, musicIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val musicNotification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(musicRec.title)
                .setContentText(musicRec.prompt)
                .setStyle(NotificationCompat.BigTextStyle().bigText(musicRec.prompt))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(musicPendingIntent)
                .setAutoCancel(true)
                .build()
                
            notificationManager.notify((2001..2010).random(), musicNotification)
        }

        fun showMovieRecommendation(context: Context) {
            val notificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            val channelId = "recommendation_channel"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channel = NotificationChannel(
                    channelId,
                    "Daily Recommendations",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Shows daily music and movie recommendations"
                }
                notificationManager.createNotificationChannel(channel)
            }

            // Movie Recommendation
            val movieRec = LocalContent.parsedMovies.random()
            val movieIntent = Intent(Intent.ACTION_VIEW).apply {
                data = Uri.parse("https://www.google.com/search?q=${Uri.encode("${movieRec.title} movie")}")
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val moviePendingIntent = PendingIntent.getActivity(
                context, 102, movieIntent,
                PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
            )

            val movieNotification = NotificationCompat.Builder(context, channelId)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContentTitle(movieRec.title)
                .setContentText(movieRec.prompt)
                .setStyle(NotificationCompat.BigTextStyle().bigText(movieRec.prompt))
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(moviePendingIntent)
                .setAutoCancel(true)
                .build()

            notificationManager.notify((2011..2020).random(), movieNotification)
        }
    }
}
