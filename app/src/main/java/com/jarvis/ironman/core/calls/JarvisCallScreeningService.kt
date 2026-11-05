package com.jarvis.ironman.core.calls

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.ContactsContract
import android.telecom.Call
import android.telecom.CallScreeningService
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.core.content.ContextCompat
import java.util.Locale

class JarvisCallScreeningService : CallScreeningService() {

    private var tts: TextToSpeech? = null

    override fun onScreenCall(callDetails: Call.Details) {

        val number = callDetails.handle?.schemeSpecificPart
            ?: "Unknown caller"

        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)

        val contactName = getContactName(number)
        announceCaller(contactName, number)

        Log.i(
            "JARVIS_CALL",
            "Incoming call: $contactName ($number)"
        )
    }

    private fun getContactName(number: String): String {

        if (number == "Unknown caller") return number

        if (
            ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_CONTACTS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return "Unknown contact"
        }

        return try {
            val uri = Uri.withAppendedPath(
                ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                Uri.encode(number)
            )

            contentResolver.query(
                uri,
                arrayOf(ContactsContract.PhoneLookup.DISPLAY_NAME),
                null,
                null,
                null
            )?.use { cursor ->

                if (cursor.moveToFirst()) {
                    cursor.getString(0) ?: "Unknown contact"
                } else {
                    "Unknown contact"
                }

            } ?: "Unknown contact"

        } catch (e: Exception) {
            Log.e("JARVIS_CALL", "Contact lookup failed", e)
            "Unknown contact"
        }
    }

    private fun announceCaller(name: String, number: String) {

        if (tts != null) return

        tts = TextToSpeech(this) { result ->

            if (result == TextToSpeech.SUCCESS) {

                val engine = tts ?: return@TextToSpeech
                engine.language = Locale("hi", "IN")

                val message = when (name) {
                    "Unknown caller" ->
                        "JARVIS alert. Unknown number se call aa rahi hai."

                    "Unknown contact" ->
                        "JARVIS alert. Is number se call aa rahi hai. $number"

                    else ->
                        "JARVIS alert. $name ka phone aa raha hai."
                }

                engine.speak(
                    message,
                    TextToSpeech.QUEUE_FLUSH,
                    null,
                    "incoming_call"
                )

            } else {
                Log.e("JARVIS_CALL", "Speech initialization failed")
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
