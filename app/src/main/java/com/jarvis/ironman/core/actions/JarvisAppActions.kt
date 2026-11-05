package com.jarvis.ironman.core.actions

import android.content.Context
import android.content.Intent
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.Settings
import android.net.Uri

class JarvisAppActions(private val context: Context) {

    fun openCamera(): Boolean {
        return launch(Intent(MediaStore.INTENT_ACTION_STILL_IMAGE_CAMERA))
    }

    fun openSettings(): Boolean {
        return launch(Intent(Settings.ACTION_SETTINGS))
    }

    fun searchWeb(query: String): Boolean {
        if (query.isBlank()) return false

        val intent = Intent(
            Intent.ACTION_VIEW,
            Uri.parse("https://www.google.com/search?q=${Uri.encode(query)}")
        )

        return launch(intent)
    }

    fun setAlarm(hour: Int, minute: Int, message: String): Boolean {
        if (hour !in 0..23 || minute !in 0..59) return false

        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, hour)
            putExtra(AlarmClock.EXTRA_MINUTES, minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, message)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
        }

        return launch(intent)
    }

    private fun launch(intent: Intent): Boolean {
        return try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

            if (intent.resolveActivity(context.packageManager) == null) {
                false
            } else {
                context.startActivity(intent)
                true
            }
        } catch (_: Exception) {
            false
        }
    }
}
