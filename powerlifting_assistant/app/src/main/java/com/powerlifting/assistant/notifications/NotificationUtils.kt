package com.powerlifting.assistant.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object NotificationUtils {
    const val CHANNEL_ID = "powerlifting_reminders"

    fun ensureChannel(context: Context) {
        val mgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val existing = mgr.getNotificationChannel(CHANNEL_ID)
        if (existing != null) return

        val channel = NotificationChannel(
            CHANNEL_ID,
            "Напоминания",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Напоминания о тренировках и питании"
        }
        mgr.createNotificationChannel(channel)
    }

    fun showReminder(context: Context, title: String, text: String, id: Int) {
        ensureChannel(context)

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(text)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            notify(id, notification)
        }
    }
}
