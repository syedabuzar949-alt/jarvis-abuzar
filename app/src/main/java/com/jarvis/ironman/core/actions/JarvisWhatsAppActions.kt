package com.jarvis.ironman.core.actions

import android.content.Context
import android.content.Intent
import android.net.Uri

class JarvisWhatsAppActions(
    private val context: Context
) {

    fun openChat(
        phoneNumber: String,
        message: String = ""
    ): Boolean {

        val number = phoneNumber.filter { it.isDigit() }

        if (number.length < 10 || message.length > 4000) {
            return false
        }

        return try {
            val uri = Uri.parse(
                "https://wa.me/$number?text=${Uri.encode(message)}"
            )

            val intent = Intent(
                Intent.ACTION_VIEW,
                uri
            ).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

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
