package com.jarvis.ironman.core.calls

import android.telecom.Call
import android.telecom.CallScreeningService
import android.util.Log

class JarvisCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {

        val number = callDetails.handle?.schemeSpecificPart
            ?: "Unknown caller"

        Log.i("JARVIS_CALL", "Incoming call: $number")

        val response = CallResponse.Builder()
            .setDisallowCall(false)
            .setRejectCall(false)
            .setSkipCallLog(false)
            .setSkipNotification(false)
            .build()

        respondToCall(callDetails, response)
    }
}
