package com.jarvis.ironman.core

import android.content.Context
import com.jarvis.ironman.core.memory.JarvisMemory
import com.jarvis.ironman.core.commands.JarvisCommandParser
import com.jarvis.ironman.core.actions.JarvisAppActions
import com.jarvis.ironman.core.features.JarvisFeatureRegistry
import com.jarvis.ironman.core.features.JarvisFeature
import com.jarvis.ironman.core.settings.JarvisSettings

class JarvisEngine(context: Context) {

    private val memory = JarvisMemory(context)
    private val settings = JarvisSettings(context)
    private val parser = JarvisCommandParser()
    private val actions = JarvisAppActions(context)

    val features = JarvisFeatureRegistry()

    init {
        registerDefaultFeatures()
    }

    private fun registerDefaultFeatures() {
        features.register(
            JarvisFeature(
                id = "ai_chat",
                name = "AI Chat",
                description = "ChatGPT and Gemini assistant",
                enabledByDefault = true
            )
        )

        features.register(
            JarvisFeature(
                id = "voice",
                name = "Voice Assistant",
                description = "Hindi and English voice"
            )
        )

        features.register(
            JarvisFeature(
                id = "memory",
                name = "Conversation Memory",
                description = "Save recent conversations",
                enabledByDefault = true
            )
        )

        features.register(
            JarvisFeature(
                id = "phone_control",
                name = "Phone Control",
                description = "Open supported phone features"
            )
        )

        features.register(
            JarvisFeature(
                id = "whatsapp",
                name = "WhatsApp Assistant",
                description = "WhatsApp integration"
            )
        )

        features.register(
            JarvisFeature(
                id = "call_assistant",
                name = "Incoming Call Assistant",
                description = "Call screening with user approval"
            )
        )

        features.register(
            JarvisFeature(
                id = "wake_word",
                name = "Hey Jarvis",
                description = "Optional wake word service"
            )
        )
    }

    fun processCommand(text: String): String {
        if (text.isBlank()) {
            return "Please tell me your command."
        }

        memory.saveMessage("user", text)

        val command = parser.parse(text)

        val response = when (command.type) {

            com.jarvis.ironman.core.commands.JarvisCommandType.OPEN_CAMERA -> {
                if (actions.openCamera()) {
                    "Camera opening."
                } else {
                    "Camera is unavailable."
                }
            }

            com.jarvis.ironman.core.commands.JarvisCommandType.WEB_SEARCH -> {
                if (actions.searchWeb(text)) {
                    "Opening web search."
                } else {
                    "Unable to open web search."
                }
            }

            com.jarvis.ironman.core.commands.JarvisCommandType.SET_ALARM -> {
                "Please provide an alarm time in a supported format."
            }

            com.jarvis.ironman.core.commands.JarvisCommandType.MAKE_CALL,
            com.jarvis.ironman.core.commands.JarvisCommandType.SEND_WHATSAPP,
            com.jarvis.ironman.core.commands.JarvisCommandType.ANSWER_CALL,
            com.jarvis.ironman.core.commands.JarvisCommandType.REJECT_CALL -> {
                "This action needs a separate permission and confirmation workflow."
            }

            else -> {
                "I am ready to help. AI response will be connected through the AI service."
            }
        }

        memory.saveMessage("assistant", response)

        return response
    }

    fun getRecentMemory(): List<String> {
        return memory.getRecentMessages()
    }

    fun clearMemory() {
        memory.clearMemory()
    }

    fun getAssistantLanguage(): String {
        return settings.language
    }

    fun getAssistantName(): String {
        return settings.assistantName
    }
}
