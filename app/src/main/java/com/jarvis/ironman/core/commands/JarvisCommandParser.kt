package com.jarvis.ironman.core.commands

class JarvisCommandParser {

    fun parse(input: String): JarvisCommand {
        val text = input.trim().lowercase()

        return when {
            text.contains("camera") ||
            text.contains("कैमरा") ||
            text.contains("photo") ||
            text.contains("फोटो") -> {
                JarvisCommand(
                    JarvisCommandType.OPEN_CAMERA,
                    input
                )
            }

            text.contains("alarm") ||
            text.contains("अलार्म") -> {
                JarvisCommand(
                    JarvisCommandType.SET_ALARM,
                    input
                )
            }

            text.contains("whatsapp") ||
            text.contains("व्हाट्सएप") -> {
                JarvisCommand(
                    JarvisCommandType.SEND_WHATSAPP,
                    input,
                    requiresConfirmation = true
                )
            }

            text.contains("call") ||
            text.contains("कॉल") ||
            text.contains("फोन लगाओ") -> {
                JarvisCommand(
                    JarvisCommandType.MAKE_CALL,
                    input,
                    requiresConfirmation = true
                )
            }

            text.contains("search") ||
            text.contains("सर्च") ||
            text.contains("खोजो") -> {
                JarvisCommand(
                    JarvisCommandType.WEB_SEARCH,
                    input
                )
            }

            else -> {
                JarvisCommand(
                    JarvisCommandType.GENERAL_AI,
                    input
                )
            }
        }
    }
}
