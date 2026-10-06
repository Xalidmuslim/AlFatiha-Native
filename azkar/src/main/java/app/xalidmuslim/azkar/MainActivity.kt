package app.xalidmuslim.azkar

import android.app.Activity
import android.app.AlarmManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.ComponentName
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import kotlin.concurrent.thread

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Paint the same solid background as the reader before Compose creates
        // its first frame. Without this, some devices briefly exposed the
        // default black window surface between the host and Azkar activities.
        val sharedDark = getSharedPreferences("alfatiha_native", MODE_PRIVATE)
            .getBoolean("dark", false)
        val launchBackgroundRes = if (sharedDark) {
            R.color.azkar_window_background_dark
        } else {
            R.color.azkar_window_background
        }
        window.setBackgroundDrawableResource(launchBackgroundRes)
        val launchBackgroundColor = resources.getColor(launchBackgroundRes, theme)
        window.statusBarColor = launchBackgroundColor
        window.navigationBarColor = launchBackgroundColor
        window.decorView.setBackgroundColor(launchBackgroundColor)

        configureActivityTransitions()
        setContent {
            AzkarAppRoot()
        }

        // Never block the first frame with legacy cleanup.
        val bootPrefs = getSharedPreferences("azkar_bootstrap", MODE_PRIVATE)
        if (!bootPrefs.getBoolean("legacy_cleanup_done", false)) {
            thread(start = true, isDaemon = true, name = "azkar-legacy-cleanup") {
                runCatching { removeLegacyReminderNotifications() }
                bootPrefs.edit().putBoolean("legacy_cleanup_done", true).apply()
            }
        }
    }

    private fun configureActivityTransitions() {
        if (Build.VERSION.SDK_INT >= 34) {
            // Opening is already driven by ActivityOptions.makeCustomAnimation()
            // in the host activity. Registering a second OPEN transition here
            // made Android 14+ compose two window animations and exposed the
            // task background between frames. Keep only the close transition.
            overrideActivityTransition(
                Activity.OVERRIDE_TRANSITION_CLOSE,
                R.anim.azkar_return_enter,
                R.anim.azkar_return_exit,
            )
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
