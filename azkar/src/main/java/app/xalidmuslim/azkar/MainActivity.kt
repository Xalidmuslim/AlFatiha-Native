package app.xalidmuslim.azkar

import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        removeLegacyReminderNotifications()
        setContent {
            AzkarAppRoot()
        }
    }

    private fun removeLegacyReminderNotifications() {
        val alarmManager = getSystemService(AlarmManager::class.java)
        val receiver = ComponentName(
            packageName,
            "app.xalidmuslim.azkar.notifications.AzkarReminderReceiver",
        )
        listOf(7101, 7102).forEach { requestCode ->
            val pendingIntent = PendingIntent.getBroadcast(
                this,
                requestCode,
                Intent().setComponent(receiver),
                PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE,
            )
            if (pendingIntent != null) {
                alarmManager?.cancel(pendingIntent)
                pendingIntent.cancel()
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            getSystemService(NotificationManager::class.java)
                ?.deleteNotificationChannel("azkar_daily_reminders")
        }
    }
}
