package com.jarvis.ironman

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.widget.*
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.jarvis.ironman.core.calls.JarvisCallRoleManager
import kotlinx.coroutines.*
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.util.Locale

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var status: TextView
    private lateinit var prompt: EditText
    private lateinit var answer: TextView
    private lateinit var apiKey: EditText
    private lateinit var provider: Spinner
    private lateinit var callScreeningButton: Button

    private var tts: TextToSpeech? = null
    private lateinit var callRoleManager: JarvisCallRoleManager

    private val scope =
        CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val prefs by lazy {
        getSharedPreferences("jarvis_settings", MODE_PRIVATE)
    }

    private val callRoleLauncher =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) {
            updateCallScreeningStatus()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        status = findViewById(R.id.status)
        prompt = findViewById(R.id.prompt)
        answer = findViewById(R.id.answer)
        apiKey = findViewById(R.id.apiKey)
        provider = findViewById(R.id.provider)
        callScreeningButton = findViewById(R.id.callScreening)

        callRoleManager = JarvisCallRoleManager(this)

        tts = TextToSpeech(this, this)

        provider.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            listOf("ChatGPT OpenAI", "Google Gemini")
        )

        provider.setSelection(prefs.getInt("provider", 0))

        loadApiKey()

        provider.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {

                override fun onNothingSelected(
                    parent: AdapterView<*>?
                ) {}

                override fun onItemSelected(
                    parent: AdapterView<*>?,
                    view: android.view.View?,
                    position: Int,
                    id: Long
                ) {
                    prefs.edit()
                        .putInt("provider", position)
                        .apply()

                    loadApiKey()
                }
            }

        findViewById<Button>(R.id.saveKey).setOnClickListener {
            val key = apiKey.text.toString().trim()

            if (key.isEmpty()) {
                status.text = "API KEY REQUIRED"
                return@setOnClickListener
            }

            val name = if (provider.selectedItemPosition == 0)
                "openai" else "gemini"

            prefs.edit().putString(name, key).apply()

            status.text = "API KEY SAVED"
        }

        findViewById<Button>(R.id.listen).setOnClickListener {
            startVoiceInput()
        }

        findViewById<Button>(R.id.send).setOnClickListener {
            askJarvis()
        }

        callScreeningButton.setOnClickListener {
            requestCallScreeningPermission()
        }

        updateCallScreeningStatus()

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
        }
    }

    private fun requestCallScreeningPermission() {

        if (!callRoleManager.isCallScreeningAvailable()) {
            status.text = "CALL SCREENING NOT AVAILABLE"
            return
        }

        if (callRoleManager.isCallScreeningEnabled()) {
            status.text = "CALL ASSISTANT ALREADY ENABLED"
            updateCallScreeningStatus()
            return
        }

        val permissionIntent =
            callRoleManager.createPermissionRequest()

        if (permissionIntent == null) {
            status.text = "CALL PERMISSION REQUEST UNAVAILABLE"
            return
        }

        callRoleLauncher.launch(permissionIntent)
    }

    private fun updateCallScreeningStatus() {

        val enabled = callRoleManager.isCallScreeningEnabled()

        callScreeningButton.text = if (enabled) {
            "CALL ASSISTANT ENABLED"
        } else {
            "ENABLE CALL ASSISTANT"
        }

        if (enabled) {
            status.text = "CALL SCREENING PERMISSION ENABLED"
        }
    }

    override fun onResume() {
        super.onResume()

        if (::callRoleManager.isInitialized) {
            updateCallScreeningStatus()
        }
    }

    private fun loadApiKey() {
        val name = if (provider.selectedItemPosition == 0)
            "openai" else "gemini"

        apiKey.setText(prefs.getString(name, ""))
    }

    private fun startVoiceInput() {

        if (!SpeechRecognizer.isRecognitionAvailable(this)) {
            status.text = "VOICE SERVICE UNAVAILABLE"
            return
        }

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                101
            )
            return
        }

        val intent = Intent(
            RecognizerIntent.ACTION_RECOGNIZE_SPEECH
        ).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE,
                "hi-IN"
            )
            putExtra(
                RecognizerIntent.EXTRA_PROMPT,
                "Jarvis ko command dein"
            )
        }

        try {
            startActivityForResult(intent, 201)
        } catch (e: Exception) {
            status.text = "VOICE INPUT ERROR"
        }
    }

    @Deprecated("Legacy activity result")
    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?
    ) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 201 && resultCode == Activity.RESULT_OK) {

            val words = data
                ?.getStringArrayListExtra(
                    RecognizerIntent.EXTRA_RESULTS
                )?.firstOrNull()

            if (!words.isNullOrBlank()) {
                prompt.setText(words)
                askJarvis()
            }
        }
    }

    private fun askJarvis() {

        val question = prompt.text.toString().trim()
        val key = apiKey.text.toString().trim()

        if (question.isEmpty()) {
            status.text = "COMMAND LIKHIYE"
            return
        }

        if (key.isEmpty()) {
            status.text = "API KEY SAVE KAREIN"
            return
        }

        val selected = provider.selectedItemPosition

        status.text = "JARVIS THINKING..."

        findViewById<Button>(R.id.send).isEnabled = false

        scope.launch {

            try {

                val result = withContext(Dispatchers.IO) {

                    if (selected == 0) {
                        callOpenAI(key, question)
                    } else {
                        callGemini(key, question)
                    }
                }

                answer.text = result
                status.text = "JARVIS ONLINE"

                tts?.speak(
                    result,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "jarvis_reply"
                )

            } catch (e: Exception) {

                status.text = "CONNECTION ERROR"

                answer.text =
                    e.message ?: "AI request failed"

            } finally {

                findViewById<Button>(R.id.send).isEnabled = true
            }
        }
    }

    private fun callOpenAI(
        key: String,
        question: String
    ): String {

        val body = JSONObject()
            .put("model", "gpt-4o-mini")
            .put(
                "messages",
                JSONArray()
                    .put(
                        JSONObject()
                            .put("role", "system")
                            .put(
                                "content",
                                """
                                You are JARVIS, an intelligent personal assistant.
                                Speak naturally in Hindi or English according
                                to the user's language.
                                Be helpful, concise and respectful.
                                Never claim you performed phone actions
                                unless those actions were actually executed.
                                """.trimIndent()
                            )
                    )
                    .put(
                        JSONObject()
                            .put("role", "user")
                            .put("content", question)
                    )
            )

        val response = request(
            "https://api.openai.com/v1/chat/completions",
            key,
            body,
            false
        )

        return response
            .getJSONArray("choices")
            .getJSONObject(0)
            .getJSONObject("message")
            .getString("content")
    }

    private fun callGemini(
        key: String,
        question: String
    ): String {

        val encodedKey = URLEncoder.encode(key, "UTF-8")

        val body = JSONObject()
            .put(
                "contents",
                JSONArray().put(
                    JSONObject().put(
                        "parts",
                        JSONArray().put(
                            JSONObject().put(
                                "text",
                                """
                                You are JARVIS, an intelligent personal assistant.
                                Reply naturally in Hindi or English.
                                Be helpful and concise.
                                $question
                                """.trimIndent()
                            )
                        )
                    )
                )
            )

        val response = request(
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=$encodedKey",
            key,
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

    private fun request(
        endpoint: String,
        key: String,
        body: JSONObject,
        gemini: Boolean
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

            if (!gemini) {
                connection.setRequestProperty(
                    "Authorization",
                    "Bearer $key"
                )
            }

            connection.outputStream.use {
                it.write(body.toString().toByteArray(Charsets.UTF_8))
            }

            val code = connection.responseCode

            val stream = if (code in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val result = stream.bufferedReader().use {
                it.readText()
            }

            if (code !in 200..299) {
                throw Exception("API Error $code: $result")
            }

            return JSONObject(result)

        } finally {
            connection.disconnect()
        }
    }

    override fun onInit(result: Int) {

        if (result == TextToSpeech.SUCCESS) {
            tts?.language = Locale("hi", "IN")
            status.text = "JARVIS SYSTEM READY"
        }
    }

    override fun onDestroy() {

        scope.cancel()

        tts?.stop()
        tts?.shutdown()

        super.onDestroy()
    }
}
