package com.jarvis.ironman.core.commands

enum class JarvisCommandType {
    OPEN_APP,
    MAKE_CALL,
    ANSWER_CALL,
    REJECT_CALL,
    SEND_WHATSAPP,
    OPEN_CAMERA,
    SET_ALARM,
    SET_REMINDER,
    WEB_SEARCH,
    GENERAL_AI,
    UNKNOWN
}

data class JarvisCommand(
    val type: JarvisCommandType,
    val originalText: String,
    val arguments: Map<String, String> = emptyMap(),
    val requiresConfirmation: Boolean = false
)

sealed class JarvisCommandResult {

    data class Success(
        val message: String
    ) : JarvisCommandResult()

    data class NeedsPermission(
        val permission: String
    ) : JarvisCommandResult()

    data class NeedsConfirmation(
        val action: String
    ) : JarvisCommandResult()

    data class Failure(
        val reason: String
    ) : JarvisCommandResult()
}
