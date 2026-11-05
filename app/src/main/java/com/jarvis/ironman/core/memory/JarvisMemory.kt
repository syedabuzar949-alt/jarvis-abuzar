package com.jarvis.ironman.core.memory

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class JarvisMemory(context: Context) {

    private val prefs = context.getSharedPreferences(
        "jarvis_memory",
        Context.MODE_PRIVATE
    )

    companion object {
        private const val HISTORY = "conversation_history"
        private const val MAX_MESSAGES = 100
    }

    @Synchronized
    fun saveMessage(role: String, message: String) {
        if (message.isBlank()) return

        val history = getHistory()

        history.put(
            JSONObject()
                .put("role", role)
                .put("message", message)
                .put("time", System.currentTimeMillis())
        )

        val trimmed = JSONArray()
        val start = (history.length() - MAX_MESSAGES)
            .coerceAtLeast(0)

        for (i in start until history.length()) {
            trimmed.put(history.getJSONObject(i))
        }

        prefs.edit()
            .putString(HISTORY, trimmed.toString())
            .apply()
    }

    @Synchronized
    fun getHistory(): JSONArray {
        val saved = prefs.getString(HISTORY, "[]") ?: "[]"

        return try {
            JSONArray(saved)
        } catch (_: Exception) {
            JSONArray()
        }
    }

    fun getRecentMessages(limit: Int = 20): List<String> {
        val history = getHistory()
        val result = mutableListOf<String>()

        val start = (history.length() - limit)
            .coerceAtLeast(0)

        for (i in start until history.length()) {
            val item = history.optJSONObject(i) ?: continue

            val role = item.optString("role")
            val message = item.optString("message")

            result.add("$role: $message")
        }

        return result
    }

    fun clearMemory() {
        prefs.edit().remove(HISTORY).apply()
    }
}
