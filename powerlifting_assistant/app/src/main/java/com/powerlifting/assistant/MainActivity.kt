package com.powerlifting.assistant

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.powerlifting.assistant.notifications.ReminderWorker
import com.powerlifting.assistant.presentation.navigation.RootNav
import com.powerlifting.assistant.presentation.theme.PowerliftingTheme
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val requestNotifications = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { /* no-op */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        maybeRequestNotifications()
        scheduleReminders()

        setContent {
            PowerliftingTheme {
                RootNav()
            }
        }
    }

    private fun maybeRequestNotifications() {
        if (Build.VERSION.SDK_INT < 33) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        val granted = ContextCompat.checkSelfPermission(this, permission) == android.content.pm.PackageManager.PERMISSION_GRANTED
        if (!granted) {
            requestNotifications.launch(permission)
        }
    }

    private fun scheduleReminders() {
        val work = PeriodicWorkRequestBuilder<ReminderWorker>(24, TimeUnit.HOURS)
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniquePeriodicWork(
                "daily_reminder",
                ExistingPeriodicWorkPolicy.UPDATE,
                work
            )
    }
}
