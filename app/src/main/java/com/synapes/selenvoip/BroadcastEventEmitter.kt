package com.synapes.selenvoip

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log
import java.util.ArrayList

/**
 * Emits the sip service broadcast intents.
 * @author gotev (Aleksandar Gotev)
 */
class BroadcastEventEmitter(context: Context) : SipServiceConstants {
    private val mContext: Context = context

    enum class BroadcastAction {
        REGISTRATION,
        INCOMING_CALL,
        CALL_STATE,
        CALL_MEDIA_STATE,
        OUTGOING_CALL,
        STACK_STATUS,
        CODEC_PRIORITIES,
        CODEC_PRIORITIES_SET_STATUS,
        MISSED_CALL,
        VIDEO_SIZE,
        CALL_STATS,
        CALL_RECONNECTION_STATE,
        SILENT_CALL_STATUS,
        NOTIFY_TLS_VERIFY_STATUS_FAILED
    }


    /**
     * Emit an incoming call broadcast intent.
     * @param accountID call's account IdUri
     * @param callID call ID number
     * @param displayName the display name of the remote party
     * @param remoteUri the IdUri of the remote party
     * @param isVideo whether the call has video or not
     */

    fun incomingCall(
        accountID: String?,
        callID: Int,
        displayName: String?,
        remoteUri: String?,
        isVideo: Boolean
    ) {
        val intent = Intent().apply {
            action = getAction(BroadcastAction.INCOMING_CALL)
            putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
            putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
            putExtra(SipServiceConstants.Companion.PARAM_DISPLAY_NAME, displayName)
            putExtra(SipServiceConstants.Companion.PARAM_REMOTE_URI, remoteUri)
            putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, isVideo)
            addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        // Log intent broadcast information
        Log.d(
            TAG,
            "+++++ Broadcasting incoming call intent: Action: ${intent.action}, AccountID: $accountID, CallID: $callID, DisplayName: $displayName, RemoteUri: $remoteUri, IsVideo: $isVideo"
        )

        sendExplicitBroadcast(intent)
    }


    /**
     * Emit a registration state broadcast intent.
     * @param accountID account IdUri
     * @param registrationStateCode SIP registration status code
     */
    fun registrationState(accountID: String?, registrationStateCode: Int) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.REGISTRATION))
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(
            SipServiceConstants.Companion.PARAM_REGISTRATION_CODE,
            registrationStateCode
        )

        Log.d(
            TAG,
            "+++++ Broadcasting Registration intent: Action: ${intent.action}, AccountID: $accountID"
        )

        mContext.sendBroadcast(intent)
    }

    /**
     * Emit a call state broadcast intent.
     * @param accountID call's account IdUri
     * @param callID call ID number
     * @param callStateCode SIP call state code
     * @param callStateStatus SIP call state status
     * @param connectTimestamp call start timestamp
     */
    @Synchronized
    fun callState(
        accountID: String?,
        callID: Int,
        callStateCode: Int,
        callStateStatus: Int,
        connectTimestamp: Long
    ) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.CALL_STATE))
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_STATE, callStateCode)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_STATUS, callStateStatus)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CONNECT_TIMESTAMP, connectTimestamp)

        Log.d(
            TAG,
            "+++++ Broadcasting Call State intent: Action: ${intent.action}, AccountID: $accountID, CallID: $callID, CallStateCode: $callStateCode, CallStateStatus: $callStateStatus"
        )

        mContext.sendBroadcast(intent)
    }

    /**
     * Emit a call state broadcast intent.
     * @param accountID call's account IdUri
     * @param callID call ID number
     * @param state MediaState state updated
     * @param value call media state update value
     */
    @Synchronized
    fun callMediaState(accountID: String?, callID: Int, state: MediaState?, value: Boolean) {
        val intent = Intent()
            .setAction(getAction(BroadcastAction.CALL_MEDIA_STATE))
            .putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
            .putExtra(SipServiceConstants.Companion.PARAM_MEDIA_STATE_KEY, state)
            .putExtra(SipServiceConstants.Companion.PARAM_MEDIA_STATE_VALUE, value)

        Log.d(
            TAG,
            "+++++ Broadcasting Call Media State intent: Action: ${intent.action}, AccountID: $accountID, CallID: $callID, State: $state, Value: $value"
        )

        mContext.sendBroadcast(intent)
    }

    fun outgoingCall(
        accountID: String?,
        callID: Int,
        number: String?,
        isVideo: Boolean,
        isVideoConference: Boolean,
        isTransfer: Boolean
    ) {
        val intent = Intent()
            .setAction(getAction(BroadcastAction.OUTGOING_CALL))
            .putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
            .putExtra(SipServiceConstants.Companion.PARAM_NUMBER, number)
            .putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, isVideo)
            .putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO_CONF, isVideoConference)
            .putExtra(SipServiceConstants.Companion.PARAM_IS_TRANSFER, isTransfer)

        Log.d(
            TAG,
            "+++++ Broadcasting Outgoing Call intent: Action: ${intent.action}, AccountID: $accountID, CallID: $callID, Number: $number, IsVideo: $isVideo, IsVideoConference: $isVideoConference, IsTransfer: $isTransfer"
        )

        sendExplicitBroadcast(intent)
    }

    fun stackStatus(started: Boolean) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.STACK_STATUS))
        intent.putExtra(SipServiceConstants.Companion.PARAM_STACK_STARTED, started)

        Log.d(
            TAG,
            "+++++ Broadcasting Stack Status intent: Action: ${intent.action}, Started: $started"
        )

        mContext.sendBroadcast(intent)
    }

    fun codecPriorities(codecPriorities: ArrayList<CodecPriority?>?) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES))
        intent.putParcelableArrayListExtra(
            SipServiceConstants.Companion.PARAM_CODEC_PRIORITIES_LIST,
            codecPriorities
        )

        Log.d(
            TAG,
            "+++++ Broadcasting Codec Priorities intent: Action: ${intent.action}, CodecPriorities: $codecPriorities"
        )

        mContext.sendBroadcast(intent)
    }

    fun codecPrioritiesSetStatus(success: Boolean) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.CODEC_PRIORITIES_SET_STATUS))
        intent.putExtra(SipServiceConstants.Companion.PARAM_SUCCESS, success)

        Log.d(
            TAG,
            "+++++ Broadcasting Codec Priorities Set Status intent: Action: ${intent.action}, Success: $success"
        )

        mContext.sendBroadcast(intent)
    }

    fun missedCall(displayName: String?, uri: String?) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.MISSED_CALL))
        intent.putExtra(SipServiceConstants.Companion.PARAM_DISPLAY_NAME, displayName)
        intent.putExtra(SipServiceConstants.Companion.PARAM_REMOTE_URI, uri)

        Log.d(
            TAG,
            "+++++ Broadcasting Missed Call intent: Action: ${intent.action}, DisplayName: $displayName, Uri: $uri"
        )

        sendExplicitBroadcast(intent)
    }

    fun videoSize(width: Int, height: Int) {
        val intent = Intent()

        intent.setAction(getAction(BroadcastAction.VIDEO_SIZE))
        intent.putExtra(SipServiceConstants.Companion.PARAM_INCOMING_VIDEO_WIDTH, width)
        intent.putExtra(SipServiceConstants.Companion.PARAM_INCOMING_VIDEO_HEIGHT, height)

        Log.d(
            TAG,
            "+++++ Broadcasting Video Size intent: Action: ${intent.action}, Width: $width, Height: $height"
        )

        mContext.sendBroadcast(intent)

    }

    fun callStats(
        callID: Int,
        duration: Int,
        audioCodec: String?,
        callStateStatus: Int,
        rx: RtpStreamStats?,
        tx: RtpStreamStats?
    ) {
        val intent = Intent()
            .setAction(getAction(BroadcastAction.CALL_STATS))
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_STATS_DURATION, duration)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_STATS_AUDIO_CODEC, audioCodec)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_STATS_CALL_STATUS, callStateStatus)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_STATS_RX_STREAM, rx)
            .putExtra(SipServiceConstants.Companion.PARAM_CALL_STATS_TX_STREAM, tx)

        Log.d(
            TAG,
            "+++++ Broadcasting Call Stats intent: Action: ${intent.action}, CallID: $callID, Duration: $duration, AudioCodec: $audioCodec, CallStateStatus: $callStateStatus, Rx: $rx, Tx: $tx"
        )

        mContext.sendBroadcast(intent)
    }

    fun callReconnectionState(state: CallReconnectionState?) {
        val intent = Intent()
        intent.setAction(getAction(BroadcastAction.CALL_RECONNECTION_STATE))
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_RECONNECTION_STATE, state)

        Log.d(
            TAG,
            "+++++ Broadcasting Call Reconnection State intent: Action: ${intent.action}, State: $state"
        )

        mContext.sendBroadcast(intent)
    }

    fun silentCallStatus(status: Boolean, number: String?) {
        val intent = Intent()
        intent.setAction(getAction(BroadcastAction.SILENT_CALL_STATUS))
        intent.putExtra(SipServiceConstants.Companion.PARAM_SILENT_CALL_STATUS, status)
        intent.putExtra(SipServiceConstants.Companion.PARAM_NUMBER, number)

        Log.d(
            TAG,
            "+++++ Broadcasting Silent Call Status intent: Action: ${intent.action}, Status: $status, Number: $number"
        )

        sendExplicitBroadcast(intent)
    }

    fun notifyTlsVerifyStatusFailed() {
        val intent = Intent()
        intent.setAction(getAction(BroadcastAction.NOTIFY_TLS_VERIFY_STATUS_FAILED))

        Log.d(
            TAG,
            "+++++ Broadcasting Notify TLS Verify Status Failed intent: Action: ${intent.action}"
        )

        sendExplicitBroadcast(intent)
    }

    private fun sendExplicitBroadcast(intent: Intent) {
        val pm = mContext.packageManager
        val matches = pm.queryBroadcastReceivers(intent, 0)

        for (resolveInfo in matches) {
            val cn =
                ComponentName(
                    resolveInfo.activityInfo.applicationInfo.packageName,
                    resolveInfo.activityInfo.name
                )

            intent.setComponent(cn)
            intent.addFlags(Intent.FLAG_RECEIVER_FOREGROUND)
        }

        Log.d(TAG, "+++++ Broadcasting EXPLICIT intent: Action: ${intent.action}")

        mContext.sendBroadcast(intent)
    }


    companion object {
        var NAMESPACE: String = Config.NAMESPACE
        private val TAG: String = BroadcastEventEmitter::class.java.simpleName
        fun getAction(action: BroadcastAction?): String {
            return "$NAMESPACE.$action"
        }
    }
}
