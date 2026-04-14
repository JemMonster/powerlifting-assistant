package com.powerlifting.assistant.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        // Simple reminders (MVP)
        NotificationUtils.showReminder(
            context = applicationContext,
            title = "Ассистент пауэрлифтера",
            text = "Не забудь отметить питание и проверить календарь тренировок.",
            id = 1001
        )
        return Result.success()
    }
}
