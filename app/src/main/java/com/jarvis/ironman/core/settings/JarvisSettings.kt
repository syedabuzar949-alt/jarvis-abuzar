package com.jarvis.ironman.core.settings

import android.content.Context

class JarvisSettings(context: Context) {

    private val prefs = context.getSharedPreferences(
        "jarvis_preferences",
        Context.MODE_PRIVATE
    )

    var language: String
        get() = prefs.getString("language", "hi-IN") ?: "hi-IN"
        set(value) {
            prefs.edit().putString("language", value).apply()
        }

    var aiProvider: String
        get() = prefs.getString("ai_provider", "openai") ?: "openai"
        set(value) {
            prefs.edit().putString("ai_provider", value).apply()
        }

    var assistantName: String
        get() = prefs.getString("assistant_name", "Jarvis") ?: "Jarvis"
        set(value) {
            prefs.edit().putString("assistant_name", value).apply()
        }

    var voiceEnabled: Boolean
        get() = prefs.getBoolean("voice_enabled", true)
        set(value) {
            prefs.edit().putBoolean("voice_enabled", value).apply()
        }

    var wakeWordEnabled: Boolean
        get() = prefs.getBoolean("wake_word_enabled", false)
        set(value) {
            prefs.edit().putBoolean("wake_word_enabled", value).apply()
        }
}
