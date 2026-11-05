package com.jarvis.ironman.core.security

enum class ActionRisk {
    SAFE,
    SENSITIVE,
    HIGH_RISK
}

data class ActionRequest(
    val actionId: String,
    val description: String,
    val risk: ActionRisk
)

class JarvisActionGuard {

    fun requiresConfirmation(request: ActionRequest): Boolean {
        return when (request.risk) {
            ActionRisk.SAFE -> false
            ActionRisk.SENSITIVE -> true
            ActionRisk.HIGH_RISK -> true
        }
    }

    fun confirmationMessage(request: ActionRequest): String {
        return when (request.risk) {
            ActionRisk.SAFE ->
                "क्या मैं यह काम करूँ?"

            ActionRisk.SENSITIVE ->
                "क्या आप अनुमति देते हैं: ${request.description}?"

            ActionRisk.HIGH_RISK ->
                "यह संवेदनशील काम है। कृपया स्पष्ट रूप से पुष्टि करें: ${request.description}?"
        }
    }

    fun isValidConfirmation(
        expectedActionId: String,
        confirmedActionId: String,
        userConfirmed: Boolean
    ): Boolean {
        return userConfirmed &&
            expectedActionId.isNotBlank() &&
            expectedActionId == confirmedActionId
    }
}
