package com.weartools.weekdayutccomp.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.media.AudioAttributes
import android.media.RingtoneManager
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.wear.watchface.complications.datasource.ComplicationDataSourceUpdateRequester
import com.weartools.weekdayutccomp.R
import com.weartools.weekdayutccomp.complication.TimerComplicationService

class TimerUpdateReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "${context.packageName}.UPDATE_TIMER") {
            // Update Complication
            Log.i("TimerUpdateReceiver", "Timer DONE! Updating complication")
            ComplicationDataSourceUpdateRequester.create(context, ComponentName(context, TimerComplicationService::class.java)).requestUpdateAll()

            // Post Notification
            postNotification(context)
        }
    }
    private fun postNotification(context: Context) {
        val channelId = "timer_done_channel"
        val notificationId = System.currentTimeMillis().toInt() // Unique ID for this notification

        // Create notification channel (required for Android 8.0+)
        val channel = NotificationChannel(
            channelId,
            "Timer Done",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for when the timer is done"
            enableVibration(true)
            vibrationPattern = longArrayOf(0, 500, 500, 500) // Custom vibration pattern
            setSound(
                RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION),
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION_EVENT)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
        }

        // Register the channel with the system
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)

        // Create the notification
        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_timer_3) // Replace with your app's drawable
            .setContentTitle("Timer Finished")
            //.setContentText("Your timer is complete!")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setOngoing(false)
            .setVibrate(longArrayOf(0, 500, 500, 500))
            .setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION))
            .build()

        // Show the notification
        notificationManager.notify(notificationId, notification)
    }
}