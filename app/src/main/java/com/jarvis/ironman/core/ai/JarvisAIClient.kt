package com.jarvis.ironman.core.ai

import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

enum class AIProvider {
    OPENAI,
    GEMINI
}

class JarvisAIClient {

    fun ask(
        provider: AIProvider,
        apiKey: String,
        question: String,
        conversationHistory: List<String> = emptyList()
    ): String {

        require(apiKey.isNotBlank()) {
            "API key is required"
        }

        require(question.isNotBlank()) {
            "Question cannot be empty"
        }

        return when (provider) {
            AIProvider.OPENAI ->
                askOpenAI(apiKey, question, conversationHistory)

            AIProvider.GEMINI ->
                askGemini(apiKey, question, conversationHistory)
        }
    }

    private fun askOpenAI(
        apiKey: String,
        question: String,
        history: List<String>
    ): String {

        val messages = JSONArray()

        messages.put(
            JSONObject()
                .put("role", "system")
                .put(
                    "content",
                    """
                    You are JARVIS, an intelligent personal assistant.
                    Reply in the language used by the user.
                    Support Hindi, Hinglish and English.
                    Be helpful, natural and concise.
                    Never claim to perform actions that were not executed.
                    """.trimIndent()
                )
        )

        history.takeLast(20).forEach { item ->
            val separator = item.indexOf(": ")

            if (separator > 0) {
                val role = item.substring(0, separator)
                val content = item.substring(separator + 2)

                if (role == "user" || role == "assistant") {
                    messages.put(
                        JSONObject()
                            .put("role", role)
                            .put("content", content)
                    )
                }
            }
        }

        messages.put(
            JSONObject()
                .put("role", "user")
                .put("content", question)
        )

        val body = JSONObject()
            .put("model", "gpt-4o-mini")
            .put("messages", messages)

        val response = postRequest(
            "https://api.openai.com/v1/chat/completions",
            apiKey,
            body,
            false
        )

        return response
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun askGemini(
        apiKey: String,
        question: String,
        history: List<String>
    ): String {

        val contents = JSONArray()

        history.takeLast(20).forEach { item ->
            val separator = item.indexOf(": ")

            if (separator > 0) {
                val role = item.substring(0, separator)
                val content = item.substring(separator + 2)

                if (role == "user" || role == "assistant") {
                    contents.put(
                        JSONObject()
                            .put(
                                "role",
                                if (role == "assistant") "model" else "user"
                            )
                            .put(
                                "parts",
                                JSONArray().put(
                                    JSONObject().put("text", content)
                                )
                            )
                    )
                }
            }
        }

        contents.put(
            JSONObject()
                .put("role", "user")
                .put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put("text", question)
                    )
                )
        )

        val body = JSONObject()
            .put("contents", contents)
            .put(
                "systemInstruction",
                JSONObject().put(
                    "parts",
                    JSONArray().put(
                        JSONObject().put(
                            "text",
                            "You are JARVIS. Reply naturally in Hindi, Hinglish or English according to the user."
                        )
                    )
                )
            )

        val encodedKey = URLEncoder.encode(apiKey, "UTF-8")

        val endpoint =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$encodedKey"

        val response = postRequest(
            endpoint,
            apiKey,
            body,
            true
        )

        return response
            .getJSONArray("candidates")
            .getJSONObject(0)
            .getJSONObject("content")
            .getJSONArray("parts")
            .getJSONObject(0)
            .getString("text")
    }

    private fun postRequest(
        endpoint: String,
        apiKey: String,
        body: JSONObject,
        isGemini: Boolean
    ): JSONObject {

        val connection =
            URL(endpoint).openConnection() as HttpURLConnection

        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 20000
            connection.readTimeout = 30000
            connection.doOutput = true

            connection.setRequestProperty(
                "Content-Type",
                "application/json"
            )

            if (!isGemini) {
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $apiKey"
                )
            }

            connection.outputStream.use {
                it.write(
                    body.toString().toByteArray(Charsets.UTF_8)
                )
            }

            val code = connection.responseCode

            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val responseText = stream.bufferedReader().use {
                it.readText()
            }

            if (code !in 200..299) {
                throw IllegalStateException(
                    "AI API Error $code: $responseText"
                )
            }

            return JSONObject(responseText)

        } finally {
            connection.disconnect()
        }
    }
}
