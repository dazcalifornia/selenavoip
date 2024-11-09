package com.synapes.selenvoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

open class BroadcastEventReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")

        // Forward the broadcast to MainActivity
        val localIntent = Intent(intent.action).apply {
            putExtras(intent)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

        when (intent.action) {
            ACTION_REGISTRATION_CHECK -> {
                Log.d(TAG, "Received REGISTRATION_CHECK broadcast")
                // Implement your registration check logic here
            }
            ACTION_MAKE_CALL -> {
                Log.d(TAG, "Received MAKE_CALL broadcast")
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                if (phoneNumber != null) {
                    Log.d(TAG, "Making call to: $phoneNumber")
                    // Implement your make call logic here
                } else {
                    Log.e(TAG, "No phone number provided for MAKE_CALL")
                }
            }
        }
    }

    companion object {
        private val TAG: String = BroadcastEventReceiver::class.java.simpleName
        const val ACTION_REGISTRATION_CHECK = "com.synapes.selenvoip.REGISTRATION_CHECK"
        const val ACTION_MAKE_CALL = "com.synapes.selenvoip.MAKE_CALL"
        const val EXTRA_PHONE_NUMBER = "phone_number"
    }
}
