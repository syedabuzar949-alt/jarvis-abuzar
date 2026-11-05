package com.jarvis.ironman

import android.app.Activity
import android.os.Bundle
import android.speech.RecognizerIntent
import android.speech.tts.TextToSpeech
import android.content.Intent
import android.widget.*
import android.view.Gravity
import android.graphics.Color
import java.util.Locale

class MainActivity : Activity(), TextToSpeech.OnInitListener {

    private lateinit var tts: TextToSpeech
    private lateinit var result: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        tts = TextToSpeech(this, this)

        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(25, 25, 25, 25)
            setBackgroundColor(Color.rgb(5, 12, 30))
        }

        val title = TextView(this).apply {
            text = "J.A.R.V.I.S"
            textSize = 36f
            setTextColor(Color.CYAN)
        }

        result = TextView(this).apply {
            text = "Hello! I am Jarvis."
            textSize = 20f
            setTextColor(Color.WHITE)
            gravity = Gravity.CENTER
        }

        val speak = Button(this).apply {
            text = "TALK TO JARVIS"
            setOnClickListener {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                intent.putExtra(
                    RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                )
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "hi-IN")
                startActivityForResult(intent, 100)
            }
        }

        layout.addView(title)
        layout.addView(result)
        layout.addView(speak)
        setContentView(layout)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            tts.language = Locale("hi", "IN")
        }
    }

    @Deprecated("Deprecated in Android")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == 100 && resultCode == RESULT_OK) {
            val words = data?.getStringArrayListExtra(
                RecognizerIntent.EXTRA_RESULTS
            )
            val text = words?.firstOrNull() ?: return
            result.text = "You said: $text"
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, null, "jarvis")
        }
    }

    override fun onDestroy() {
        tts.stop()
        tts.shutdown()
        super.onDestroy()
    }
}
