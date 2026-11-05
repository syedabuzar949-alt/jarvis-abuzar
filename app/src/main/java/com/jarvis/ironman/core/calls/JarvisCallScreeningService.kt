package com.jarvis.ironman.core.calls

import android.telecom.Call
import android.telecom.CallScreeningService
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import java.util.Locale

class JarvisCallScreeningService : CallScreeningService() {

    private var tts: TextToSpeech? = null

    override fun onScreenCall(callDetails: Call.Details) {

        val number = callDetails.handle
            ?.schemeSpecificPart
            ?: "Unknown caller"

        Log.i("JARVIS_CALL", "Incoming call: $number")

        // Call ko normal tarike se aane do.
        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        // Android ko response jaldi dena zaroori hai.
        respondToCall(callDetails, response)

        // Caller number ko awaaz mein announce karo.
        announceCaller(number)
    }

    private fun announceCaller(number: String) {

        if (tts != null) return

        tts = TextToSpeech(this) { result ->

            if (result == TextToSpeech.SUCCESS) {

                val engine = tts ?: return@TextToSpeech

                engine.language = Locale("hi", "IN")

                val message = if (
                    number == "Unknown caller"
                ) {
                    "JARVIS alert. Unknown number se call aa rahi hai."
                } else {
                    "JARVIS alert. Is number se call aa rahi hai. $number"
                }

                engine.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "incoming_call"
                )

            } else {
                Log.e("JARVIS_CALL", "Text to speech initialization failed")
            }
        }
    }

    override fun onDestroy() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        super.onDestroy()
    }
}
