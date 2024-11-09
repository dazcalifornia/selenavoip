package com.synapes.selenvoip

import android.content.Context
import android.content.Intent
import android.hardware.camera2.CameraManager
import android.net.Uri
import android.util.Log
import android.view.Surface
import com.synapes.selenvoip.SharedPrefsProvider.Companion.getInstance
import org.pjsip.PjCameraInfo2
import kotlin.jvm.java
import kotlin.text.startsWith

/**
 * Triggers sip service commands.
 * @author gotev (Aleksandar Gotev)
 */
@Suppress("unused")
object SipServiceCommand : SipServiceConstants {
    /**
     * This should be changed on the app side
     * to reflect app version/name/... or whatever might be useful for debugging
     */
    var AGENT_NAME: String = Config.AGENT_NAME

    /**
     * Enables pjsip logging (valid only for debug builds)
     */
    fun enableSipDebugLogging(enable: Boolean) {
        SipServiceUtils.ENABLE_SIP_LOGGING = enable
    }

    /**
     * Adds a new SIP account.
     * @param context application context
     * @param sipAccount sip account data
     * @return sip account ID uri as a string
     */
    fun setAccount(context: Context, sipAccount: SipAccountData): String {
        requireNotNull(sipAccount) { "sipAccount MUST not be null!" }

        val accountID = sipAccount.getIdUri()
        Log.d(
            "SipServiceCommand",
            "setAccount: $accountID, sipAccount username: ${sipAccount.username}, password: ${sipAccount.password}, realm: ${sipAccount.realm}, host: ${sipAccount.host}, port: ${sipAccount.port}"
        )
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_ACCOUNT)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_DATA, sipAccount)
        context.startService(intent)

        return accountID
    }

    /**
     * Adds a new SIP account and changes the sip stack codec priority settings.
     * This is handy to set an account plus the global codec priority configuration with
     * just a single call.
     * @param context application context
     * @param sipAccount sip account data
     * @param codecPriorities list with the codec priorities to set
     * @return sip account ID uri as a string
     */
    fun setAccountWithCodecs(
        context: Context, sipAccount: SipAccountData,
        codecPriorities: ArrayList<CodecPriority?>?
    ): String {
        requireNotNull(sipAccount) { "sipAccount MUST not be null!" }

        val accountID = sipAccount.getIdUri()
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_ACCOUNT)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_DATA, sipAccount)
        intent.putParcelableArrayListExtra(
            SipServiceConstants.Companion.PARAM_CODEC_PRIORITIES,
            codecPriorities
        )
        context.startService(intent)

        return accountID
    }

    /**
     * Remove a SIP account.
     * @param context application context
     * @param accountID account ID uri
     */
    fun removeAccount(context: Context, accountID: String) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_REMOVE_ACCOUNT)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        context.startService(intent)
    }

    /**
     * Starts the SIP service.
     * @param context application context
     */
    fun start(context: Context) {
        context.startService(Intent(context, SipService::class.java))
    }

    /**
     * Stops the SIP service.
     * @param context application context
     */
    fun stop(context: Context) {
        context.stopService(Intent(context, SipService::class.java))
    }

    /**
     * Restarts the SIP stack without restarting the service.
     * @param context application context
     */
    fun restartSipStack(context: Context) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_RESTART_SIP_STACK)
        context.startService(intent)
    }

    /**
     * Makes a call.
     * @param context application context
     * @param accountID account ID used to make the call
     * @param numberToCall number to call
     * @param isVideo whether the call has video or not
     * @param isVideoConference whether the call is video conference or not
     * @param isTransfer whether this (second) call will eventually be transferred to the current
     */
    @JvmOverloads
    fun makeCall(
        context: Context,
        accountID: String,
        numberToCall: String?,
        isVideo: Boolean = false,
        isVideoConference: Boolean = false,
        isTransfer: Boolean = false
    ) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_MAKE_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_NUMBER, numberToCall)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, isVideo)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO_CONF, isVideoConference)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_TRANSFER, isTransfer)
        context.startService(intent)
    }

    fun makeTransferCall(
        context: Context,
        accountID: String,
        numberToCall: String?,
        isTransfer: Boolean
    ) {
        makeCall(context, accountID, numberToCall, false, false, isTransfer)
    }

    /**
     * Makes a silent call, i.e. the outgoing call event is sent through broadcast
     * [BroadcastEventEmitter.silentCallStatus]
     * Instead of [BroadcastEventEmitter.outgoingCall]
     * Useful when the calls only enables/disables features/services on the pbx via feature codes
     * E.g. enable dnd, join/leave queue, ...
     * @param context application context
     * @param accountID account ID used to make the call
     * @param numberToCall number to call
     */
    fun makeSilentCall(context: Context, accountID: String, numberToCall: String?) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_MAKE_SILENT_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_NUMBER, numberToCall)
        context.startService(intent)
    }

    /**
     * Makes a Direct call.
     * @param context application context
     * @param guestName name to display when making guest calls
     * @param host sip host
     * @param sipUri sip uri to call in the format: sip:number@realm:port
     * @param isVideo whether the call has video or not
     * @param isVideoConference whether the call is video conference or not
     * @param transport transport to be configured on guest account
     */
    @JvmOverloads
    fun makeDirectCall(
        context: Context,
        guestName: String?,
        sipUri: Uri?,
        host: String?,
        isVideo: Boolean,
        isVideoConference: Boolean,
        transport: SipAccountTransport = SipAccountTransport.UDP
    ) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_MAKE_DIRECT_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_GUEST_NAME, guestName)
        intent.putExtra(SipServiceConstants.Companion.PARAM_DIRECT_CALL_URI, sipUri)
        intent.putExtra(SipServiceConstants.Companion.PARAM_DIRECT_CALL_SIP_SERVER, host)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, isVideo)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO_CONF, isVideoConference)
        intent.putExtra(
            SipServiceConstants.Companion.PARAM_DIRECT_CALL_TRANSPORT,
            transport.ordinal
        )
        context.startService(intent)
    }

    /**
     * Checks the status of a call. You will receive the result in
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID used to make the call
     * @param callID call ID
     */
    fun getCallStatus(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_GET_CALL_STATUS)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Hangs up an active call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID to hang up
     */
    fun hangUpCall(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_HANG_UP_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Hangs up active calls.
     * @param context application context
     * @param accountID account ID
     */
    fun hangUpActiveCalls(context: Context, accountID: String) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_HANG_UP_CALLS)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        context.startService(intent)
    }

    /**
     * Hangs up active calls.
     * @param context application context
     * @param accountID account ID
     */
    fun holdActiveCalls(context: Context, accountID: String) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_HOLD_CALLS)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        context.startService(intent)
    }

    /**
     * Send DTMF. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID to hang up
     * @param dtmfTone DTMF tone to send (e.g. number from 0 to 9 or # or *).
     * You can send only one DTMF at a time.
     */
    fun sendDTMF(context: Context, accountID: String, callID: Int, dtmfTone: String?) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SEND_DTMF)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_DTMF, dtmfTone)
        context.startService(intent)
    }

    /**
     * Accept an incoming call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID to hang up
     * @param isVideo video call or not
     */
    @JvmOverloads
    fun acceptIncomingCall(
        context: Context,
        accountID: String,
        callID: Int,
        isVideo: Boolean = false
    ) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_ACCEPT_INCOMING_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, isVideo)
        context.startService(intent)
    }

    /**
     * Decline an incoming call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID to hang up
     */
    fun declineIncomingCall(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_DECLINE_INCOMING_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Blind call transfer. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param number number to which to transfer the call
     */
    fun transferCall(context: Context, accountID: String, callID: Int, number: String?) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_TRANSFER_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_NUMBER, number)
        context.startService(intent)
    }

    /**
     * Attended call transfer. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callIdOrig call ID of the original call
     * @param callIdDest call ID of the destination call
     */
    fun attendedTransferCall(
        context: Context,
        accountID: String,
        callIdOrig: Int,
        callIdDest: Int
    ) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_ATTENDED_TRANSFER_CALL)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callIdOrig)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID_DEST, callIdDest)
        context.startService(intent)
    }

    /**
     * Sets hold status for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param hold true to hold the call, false to un-hold it
     */
    fun setCallHold(context: Context, accountID: String, callID: Int, hold: Boolean) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_HOLD)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_HOLD, hold)
        context.startService(intent)
    }

    /**
     * Toggle hold status for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     */
    fun toggleCallHold(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_TOGGLE_HOLD)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Sets mute status for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param mute true to mute the call, false to un-mute it
     */
    fun setCallMute(context: Context, accountID: String, callID: Int, mute: Boolean) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_MUTE)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_MUTE, mute)
        context.startService(intent)
    }

    /**
     * Toggle mute status for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     */
    fun toggleCallMute(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_TOGGLE_MUTE)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Requests the codec priorities. This is going to return results only if the sip stack has
     * been started, otherwise you will see an error message in LogCat.
     * @param context application context
     */
    fun getCodecPriorities(context: Context) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_GET_CODEC_PRIORITIES)
        context.startService(intent)
    }

    /**
     * Set codec priorities. this is going to work only if the sip stack has
     * been started, otherwise you will see an error message in LogCat.
     * @param context application context
     * @param codecPriorities list with the codec priorities to set
     */
    fun setCodecPriorities(context: Context, codecPriorities: ArrayList<CodecPriority?>?) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_CODEC_PRIORITIES)
        intent.putParcelableArrayListExtra(
            SipServiceConstants.Companion.PARAM_CODEC_PRIORITIES,
            codecPriorities
        )
        context.startService(intent)
    }

    private fun checkAccount(accountID: String) {
        Log.d(
            "SipServiceCommand",
            "checkAccount: $accountID, URI Correct?: ${accountID.startsWith("sip:")}"
        )
        require(accountID.startsWith("sip:")) { "Invalid accountID! Example: sip:user@domain" }
    }

    /**
     * Gets the registration status for an account.
     * @param context application context
     * @param accountID sip account data
     */
    fun getRegistrationStatus(context: Context, accountID: String) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_GET_REGISTRATION_STATUS)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        context.startService(intent)
    }

    fun refreshRegistration(
        context: Context,
        accountID: String,
        regExpTimeout: Int,
        regContactParams: String?
    ) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_REFRESH_REGISTRATION)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_REG_EXP_TIMEOUT, regExpTimeout)
        intent.putExtra(SipServiceConstants.Companion.PARAM_REG_CONTACT_PARAMS, regContactParams)
        context.startService(intent)
    }

    fun setDND(context: Context, dnd: Boolean) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_DND)
        intent.putExtra(SipServiceConstants.Companion.PARAM_DND, dnd)
        context.startService(intent)
    }

    /**
     * Sets up the incoming video feed. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param surface surface on which to render the incoming video
     */
    fun setupIncomingVideoFeed(
        context: Context,
        accountID: String,
        callID: Int,
        surface: Surface?
    ) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_INCOMING_VIDEO)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_SURFACE, surface)
        context.startService(intent)
    }

    /**
     * Mutes and Un-Mutes video for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param mute whether to mute or un-mute the video
     */
    fun setVideoMute(context: Context, accountID: String, callID: Int, mute: Boolean) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_VIDEO_MUTE)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_VIDEO_MUTE, mute)
        context.startService(intent)
    }

    /**
     * Starts the preview for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param surface surface on which to render the preview
     */
    fun startVideoPreview(context: Context, accountID: String, callID: Int, surface: Surface?) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_START_VIDEO_PREVIEW)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_SURFACE, surface)
        context.startService(intent)
    }

    /**
     * Rotates the transmitting video (heads up always), according to the device orientation.
     * If the call does not exist or has been terminated, a disconnected state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     * @param orientation call ID
     */
    fun changeVideoOrientation(context: Context, accountID: String, callID: Int, orientation: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SET_SELF_VIDEO_ORIENTATION)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ORIENTATION, orientation)
        context.startService(intent)
    }

    /**
     * Stops the preview for a call. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     */
    fun stopVideoPreview(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_STOP_VIDEO_PREVIEW)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Switches between front and back camera. If the call does not exist or has been terminated, a disconnected
     * state will be sent to
     * [BroadcastEventReceiver.onCallState]
     * @param context application context
     * @param accountID account ID
     * @param callID call ID
     */
    fun switchVideoCaptureDevice(context: Context, accountID: String, callID: Int) {
        checkAccount(accountID)

        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_SWITCH_VIDEO_CAPTURE_DEVICE)
        intent.putExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID, accountID)
        intent.putExtra(SipServiceConstants.Companion.PARAM_CALL_ID, callID)
        context.startService(intent)
    }

    /**
     * Depending on the configuration (accountConfig.setIpChangeConfig) the reconnection process may differ
     * By default it will try to recover an existing call if present by
     * restarting the transport
     * registering
     * updating via & contact
     *
     * Before calling this you should listen to network connection/disconnection events.
     * As soon as the connection comes back after a disconnection event you can call this
     * to try to reconnect the ongoing call
     * @param context the context
     */
    fun reconnectCall(context: Context) {
        val intent = Intent(context, SipService::class.java)
        intent.setAction(SipServiceConstants.Companion.ACTION_RECONNECT_CALL)
        context.startService(intent)
    }

    /**
     * Sets the camera manager within the PjCamera2Info class
     * it is used to enumerate the video devices without the CAMERA permission
     * @param cm CameraManager retrieved with [Context.getSystemService]}
     */
    fun setCameraManager(cm: CameraManager?) {
        PjCameraInfo2.SetCameraManager(cm)
    }

    /**
     *
     * @param context the context
     * @param verify enables and disables the sip server certificate verification
     */
    fun setVerifySipServerCert(context: Context, verify: Boolean) {
        getInstance(context).setVerifySipServerCert(verify)
    }
}
