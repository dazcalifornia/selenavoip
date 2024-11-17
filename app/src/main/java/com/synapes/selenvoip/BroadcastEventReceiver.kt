package com.synapes.selenvoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selenvoip.ObfuscationHelper.Companion.getValue

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
        val localIntent = Intent(LOCAL_BROADCAST_ACTION).apply {
            putExtra(EXTRA_ORIGINAL_ACTION, intent.action)
            putExtras(intent)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(localIntent)

        receiverContext = context

        when (intent.action) {
            // CUSTOMS
            ACTION_REGISTRATION_CHECK -> {
                Log.d(TAG, "Received REGISTRATION_CHECK broadcast")
            }

            ACTION_MAKE_CALL -> {
                Log.d(TAG, "Received MAKE_CALL broadcast")
                val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                if (phoneNumber != null) {
                    Log.d(TAG, "Making call to: $phoneNumber")
                } else {
                    Log.e(TAG, "No phone number provided for MAKE_CALL")
                }

            }

            // VOIPs
            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION) -> handleRegistration(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.INCOMING_CALL) -> handleIncomingCall(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CALL_STATE) -> handleCallState(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CALL_MEDIA_STATE) -> handleCallMediaState(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.OUTGOING_CALL) -> handleOutgoingCall(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.STACK_STATUS) -> handleStackStatus(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CODEC_PRIORITIES) -> handleCodecPriorities(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CODEC_PRIORITIES_SET_STATUS) -> handleCodecPrioritiesSetStatus(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.MISSED_CALL) -> handleMissedCall(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.VIDEO_SIZE) -> handleVideoSize(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CALL_STATS) -> handleCallStats(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.CALL_RECONNECTION_STATE) -> handleCallReconnectionState(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.SILENT_CALL_STATUS) -> handleSilentCallStatus(
                intent
            )

            BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.NOTIFY_TLS_VERIFY_STATUS_FAILED) -> onTlsVerifyStatusFailed()
            else -> {
                Log.d(TAG, "Unhandled broadcast: ${intent.action}")
            }
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

    open fun unregister(context: Context) {
        try {
            Log.i(TAG, "Unregistering BER: $this from context: $context")
            context.unregisterReceiver(this)
        } catch (e: Exception) {
            Log.e(TAG, "Error while unregistering BER", e)
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

    private fun handleIncomingCall(intent: Intent) {
        Log.d(TAG, "Received: onIncomingCall --> calling function onIncomingCall")
        onIncomingCall(
            intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
            intent.getStringExtra(SipServiceConstants.PARAM_DISPLAY_NAME),
            intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI),
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false)
        )
    }

    open fun onIncomingCall(
        accountID: String?,
        callID: Int,
        displayName: String?,
        remoteUri: String?,
        isVideo: Boolean
    ) {
        Log.d(
            TAG,
            "Received: onIncomingCall - accountID: $accountID, callID: $callID, displayName: $displayName, remoteUri: $remoteUri"
        )
        Log.d(TAG, " ** OBFUSCATE ** ${getValue(getReceiverContext(), accountID.toString())}")
    }

    private fun handleCallState(intent: Intent) {
        Log.d(TAG, "Received: onCallState")
        onCallState(
            intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_STATE, -1),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_STATUS, -1),
            intent.getLongExtra(SipServiceConstants.PARAM_CONNECT_TIMESTAMP, -1)
        )
    }

    open fun onCallState(
        accountID: String?,
        callID: Int,
        callStateCode: Int,
        callStatusCode: Int,
        connectTimestamp: Long
    ) {
        Log.d(
            TAG,
            "Received: onCallState - accountID: $accountID, callID: $callID, callStateCode: $callStateCode, callStatusCode: $callStatusCode, connectTimestamp: $connectTimestamp"
        )
    }

    private fun handleCallMediaState(intent: Intent) {
        Log.d(TAG, "Received: onCallMediaState")
        onCallMediaState(
            intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
            intent.getSerializableExtra(SipServiceConstants.PARAM_MEDIA_STATE_KEY) as MediaState,
            intent.getBooleanExtra(SipServiceConstants.PARAM_MEDIA_STATE_VALUE, false)
        )
    }

    open fun onCallMediaState(
        accountID: String?,
        callID: Int,
        stateType: MediaState,
        stateValue: Boolean
    ) {
        Log.d(
            TAG,
            "Received: onCallMediaState - accountID: $accountID, callID: $callID, mediaStateType: ${stateType.name}, mediaStateValue: $stateValue"
        )
    }

    private fun handleOutgoingCall(intent: Intent) {
        Log.d(TAG, "Received: onOutgoingCall")
        onOutgoingCall(
            intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
            intent.getStringExtra(SipServiceConstants.PARAM_NUMBER),
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false),
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO_CONF, false),
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_TRANSFER, false)
        )
    }

    open fun onOutgoingCall(
        accountID: String?,
        callID: Int,
        number: String?,
        isVideo: Boolean,
        isVideoConference: Boolean,
        isTransfer: Boolean
    ) {
        Log.d(TAG, "Received: onOutgoingCall - accountID: $accountID, callID: $callID")
    }

    private fun handleStackStatus(intent: Intent) {
        Log.d(TAG, "Received: onStackStatus")
        onStackStatus(intent.getBooleanExtra(SipServiceConstants.PARAM_STACK_STARTED, false))
    }

    open fun onStackStatus(started: Boolean) {
        Log.d(TAG, "Received: SIP service stack ${if (started) "started" else "stopped"}")
    }

    private fun handleCodecPriorities(intent: Intent) {
        Log.d(TAG, "Received: onReceivedCodecPriorities")
        val codecList =
            intent.getParcelableArrayListExtra<CodecPriority>(SipServiceConstants.PARAM_CODEC_PRIORITIES_LIST)
        codecList?.let { onReceivedCodecPriorities(it) }
    }

    open fun onReceivedCodecPriorities(codecPriorities: ArrayList<CodecPriority>) {
        Log.d(TAG, "Received: Received codec priorities: ${codecPriorities.joinToString()}")
    }

    private fun handleCodecPrioritiesSetStatus(intent: Intent) {
        Log.d(TAG, "Received: onCodecPrioritiesSetStatus")
        onCodecPrioritiesSetStatus(intent.getBooleanExtra(SipServiceConstants.PARAM_SUCCESS, false))
    }

    open fun onCodecPrioritiesSetStatus(success: Boolean) {
        Log.d(
            TAG,
            "Received: Codec priorities ${if (success) "successfully set" else "set error"}"
        )
    }

    private fun handleMissedCall(intent: Intent) {
        Log.d(TAG, "Received: onMissedCall")
        onMissedCall(
            intent.getStringExtra(SipServiceConstants.PARAM_DISPLAY_NAME),
            intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI)
        )
    }

    open fun onMissedCall(displayName: String?, uri: String?) {
        Log.d(TAG, "Received: Missed call from $displayName")
    }

    private fun handleVideoSize(intent: Intent) {
        Log.d(TAG, "Received: onVideoSize")
        onVideoSize(
            intent.getIntExtra(
                SipServiceConstants.PARAM_INCOMING_VIDEO_WIDTH,
                SipServiceConstants.H264_DEF_WIDTH
            ),
            intent.getIntExtra(
                SipServiceConstants.PARAM_INCOMING_VIDEO_HEIGHT,
                SipServiceConstants.H264_DEF_HEIGHT
            )
        )
    }

    open fun onVideoSize(width: Int, height: Int) {
        Log.d(TAG, "Received: Video resolution ${width}x$height")
    }

    private fun handleCallStats(intent: Intent) {
        Log.d(TAG, "Received: onCallStats")
        onCallStats(
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_STATS_DURATION, 0),
            intent.getStringExtra(SipServiceConstants.PARAM_CALL_STATS_AUDIO_CODEC),
            intent.getIntExtra(SipServiceConstants.PARAM_CALL_STATUS, -1),
            intent.getParcelableExtra(SipServiceConstants.PARAM_CALL_STATS_RX_STREAM),
            intent.getParcelableExtra(SipServiceConstants.PARAM_CALL_STATS_TX_STREAM)
        )
    }

    open fun onCallStats(
        callID: Int,
        duration: Int,
        audioCodec: String?,
        callStatusCode: Int,
        rx: RtpStreamStats?,
        tx: RtpStreamStats?
    ) {
        Log.d(TAG, "Received: Call Stats sent $duration $audioCodec")
    }

    private fun handleCallReconnectionState(intent: Intent) {
        Log.d(TAG, "Received: onCallReconnectionState")
        val state =
            intent.getSerializableExtra(SipServiceConstants.PARAM_CALL_RECONNECTION_STATE) as? CallReconnectionState
        state?.let { onCallReconnectionState(it) }
    }

    open fun onCallReconnectionState(state: CallReconnectionState) {
        Log.d(TAG, "Received: Call reconnection state ${state.name}")
    }

    private fun handleSilentCallStatus(intent: Intent) {
        Log.d(TAG, "Received: onSilentCallStatus")
        onSilentCallStatus(
            intent.getBooleanExtra(SipServiceConstants.PARAM_SILENT_CALL_STATUS, false),
            intent.getStringExtra(SipServiceConstants.PARAM_NUMBER)
        )
    }

    open fun onSilentCallStatus(success: Boolean, number: String?) {
        Log.d(TAG, "Received: Success: $success for silent call: $number")
    }

    open fun onTlsVerifyStatusFailed() {
        Log.d(TAG, "Received: TlsVerifyStatusFailed")
    }

    companion object {
        private val TAG: String = BroadcastEventReceiver::class.java.simpleName
        // customs
        const val ACTION_REGISTRATION_CHECK = "com.synapes.selenvoip.REGISTRATION_CHECK"
        const val ACTION_MAKE_CALL = "com.synapes.selenvoip.MAKE_CALL"
        const val EXTRA_PHONE_NUMBER = "phone_number"
        // local broadcasts
        const val LOCAL_BROADCAST_ACTION = "com.synapes.selenvoip.LOCAL_BROADCAST"
        const val EXTRA_ORIGINAL_ACTION = "original_action"
    }
}
