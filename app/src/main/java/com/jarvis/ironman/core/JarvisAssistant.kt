package com.jarvis.ironman.core

import android.content.Context
import com.jarvis.ironman.core.ai.AIProvider
import com.jarvis.ironman.core.ai.JarvisAIClient
import com.jarvis.ironman.core.memory.JarvisMemory
import com.jarvis.ironman.core.settings.JarvisSettings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class JarvisAssistant(context: Context) {

    private val appContext = context.applicationContext

    private val aiClient = JarvisAIClient()
    private val memory = JarvisMemory(appContext)
    private val settings = JarvisSettings(appContext)

    suspend fun ask(
        question: String,
        apiKey: String
    ): String = withContext(Dispatchers.IO) {

        require(question.isNotBlank()) {
            "Please enter a message."
        }

        require(apiKey.isNotBlank()) {
            "API key is missing."
        }

        val history = memory.getRecentMessages(20)

        val provider = when (
            settings.aiProvider.lowercase()
        ) {
            "gemini" -> AIProvider.GEMINI
            else -> AIProvider.OPENAI
        }

        val response = aiClient.ask(
            provider = provider,
            apiKey = apiKey,
            question = question,
            conversationHistory = history
        )

        memory.saveMessage("user", question)
        memory.saveMessage("assistant", response)

        response
    }

    fun getConversationHistory(): List<String> {
        return memory.getRecentMessages(50)
    }

    fun clearConversation() {
        memory.clearMemory()
    }

    fun setProvider(provider: String) {
        settings.aiProvider = provider
    }
}
