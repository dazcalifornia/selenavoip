package com.synapes.selenvoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

open class BroadcastEventReceiver : BroadcastReceiver(), SipServiceConstants {
    private lateinit var receiverContext: Context

    fun setReceiverContext(context: Context) {
        Log.d(TAG, "Setting receiver context to: $context")
        receiverContext = context
    }

    open fun getReceiverContext(): Context = receiverContext

    override fun onReceive(context: Context, intent: Intent) {
        Log.d(TAG, "Received broadcast: ${intent.action}")

        // Forward the broadcast to MainActivity
        val localIntent = Intent(intent.action).apply {
            putExtras(intent)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

        receiverContext = context

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

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION) -> handleRegistration(
                intent
            )
        }
    }

    open fun register(context: Context, receiverExported: Int = 4) { // 4 = RECEIVER_NOT_EXPORTED
        Log.i(TAG, "***** Registering receiver: $this from context: $context")
        val intentFilter = IntentFilter().apply {
            BroadcastEventEmitter.BroadcastAction.entries.forEach { action ->
                Log.d(TAG, "Adding action: ${BroadcastEventEmitter.getAction(action)}")
                addAction(BroadcastEventEmitter.getAction(action))
            }
        }

        // Actually register the receiver
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.registerReceiver(this, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        } else {
            context.registerReceiver(this, intentFilter)
        }
    }

    private fun handleRegistration(intent: Intent) {
        Log.d(TAG, "Received: onRegistration")
        val stateCode = intent.getIntExtra(SipServiceConstants.PARAM_REGISTRATION_CODE, -1)
        val accountID = intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID)
        onRegistration(accountID, stateCode)
    }

    open fun onRegistration(accountID: String?, registrationStateCode: Int) {
        Log.d(
            TAG,
            "Received: onRegistration - accountID: $accountID, registrationStateCode: $registrationStateCode"
        )
    }

    companion object {
        private val TAG: String = BroadcastEventReceiver::class.java.simpleName
        const val ACTION_REGISTRATION_CHECK = "com.synapes.selenvoip.REGISTRATION_CHECK"
        const val ACTION_MAKE_CALL = "com.synapes.selenvoip.MAKE_CALL"
        const val EXTRA_PHONE_NUMBER = "phone_number"
    }
}
