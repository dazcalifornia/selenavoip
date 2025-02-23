package com.synapes.selenvoip.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selenvoip.BroadcastEventEmitter
import com.synapes.selenvoip.BroadcastEventReceiver
import com.synapes.selenvoip.CallActivity
import com.synapes.selenvoip.SipServiceCommand
import com.synapes.selenvoip.SipServiceConstants

class BroadcastManager(private val context: Context) {
    private val TAG = "BroadcastManager"

    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val originalAction = intent.getStringExtra(BroadcastEventReceiver.EXTRA_ORIGINAL_ACTION)
            Log.d(TAG, "===LOCAL BROADCAST RECEIVED==== Original action: $originalAction")

            when (originalAction) {
                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION) -> {
                    handleRegistration(intent)
                }
                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.OUTGOING_CALL) -> {
                    handleOutgoingCall(intent)
                }
                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.INCOMING_CALL) -> {
                    handleIncomingCall(intent)
                }
                else -> {
                    Log.d(TAG, "Unhandled broadcast: $originalAction")
                }
            }
        }
    }

    private fun handleRegistration(intent: Intent) {
        Log.d(TAG, "Handling registration broadcast")
        // Handle registration status changes
        // You can add specific registration handling logic here
    }

    private fun handleOutgoingCall(intent: Intent) {
        Log.d(TAG, "Outgoing call broadcast received with params:")
        val accountId = intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID)
        val callId = intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1)
        val remoteUri = intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI)
        val isVideo = intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false)

        Log.d(TAG, """
            Call Details:
            Account ID: $accountId
            Call ID: $callId
            Remote URI: $remoteUri
        """.trimIndent())

        CallActivity.startActivityOut(
            context,
            accountId.toString(),
            callId,
            remoteUri ?: "",
            isVideo,
            false
        )
        Log.d(TAG, "-------- SHOWN CALL ACTIVITY SCREEN OUTGOING CALL")
    }

    private fun handleIncomingCall(intent: Intent) {
        Log.d(TAG, "Handling incoming call broadcast")
        val accountId = intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID)
        val callId = intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1)
        val displayName = intent.getStringExtra(SipServiceConstants.PARAM_DISPLAY_NAME)
        val remoteUri = intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI)
        val isVideo = intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false)

        Log.d(TAG, """
            Incoming Call Details:
            Account ID: $accountId
            Call ID: $callId
            Display Name: $displayName
            Remote URI: $remoteUri
            Is Video: $isVideo
        """.trimIndent())

        // Accept incoming call
        SipServiceCommand.acceptIncomingCall(context, accountId.toString(), callId, isVideo)

        // Start call activity
        CallActivity.startActivityIn(
            context,
            accountId.toString(),
            callId,
            displayName.toString(),
            remoteUri.toString(),
            isVideo
        )
    }

    fun onStart() {
        // Register the localBroadcastReceiver to receive local broadcasts
        val filter = IntentFilter(BroadcastEventReceiver.LOCAL_BROADCAST_ACTION)
        LocalBroadcastManager.getInstance(context).registerReceiver(localBroadcastReceiver, filter)
    }

    fun onStop() {
        // Clean up if needed
    }

    fun onDestroy() {
        // Unregister the broadcast receiver
        LocalBroadcastManager.getInstance(context).unregisterReceiver(localBroadcastReceiver)
    }
}