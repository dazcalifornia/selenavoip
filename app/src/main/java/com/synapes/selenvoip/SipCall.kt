package com.synapes.selenvoip

import android.media.AudioManager
import android.media.ToneGenerator
import android.util.Log
import android.view.Surface
import org.pjsip.pjsua2.AudDevManager
import org.pjsip.pjsua2.AudioMedia
import org.pjsip.pjsua2.Call
import org.pjsip.pjsua2.CallInfo
import org.pjsip.pjsua2.CallMediaInfo
import org.pjsip.pjsua2.CallMediaInfoVector
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.CallSetting
import org.pjsip.pjsua2.CallVidSetStreamParam
import org.pjsip.pjsua2.Media
import org.pjsip.pjsua2.MediaFmtChangedEvent
import org.pjsip.pjsua2.OnCallMediaEventParam
import org.pjsip.pjsua2.OnCallMediaStateParam
import org.pjsip.pjsua2.OnCallStateParam
import org.pjsip.pjsua2.OnStreamDestroyedParam
import org.pjsip.pjsua2.RtcpStreamStat
import org.pjsip.pjsua2.VideoWindow
import org.pjsip.pjsua2.VideoPreview
import org.pjsip.pjsua2.StreamInfo
import org.pjsip.pjsua2.StreamStat
import org.pjsip.pjsua2.VideoPreviewOpParam
import org.pjsip.pjsua2.VideoWindowHandle
import org.pjsip.pjsua2.pjmedia_dir
import org.pjsip.pjsua2.pjmedia_event_type
import org.pjsip.pjsua2.pjmedia_rtcp_fb_type
import org.pjsip.pjsua2.pjmedia_type
import org.pjsip.pjsua2.pjsip_role_e
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code
import org.pjsip.pjsua2.pjsua2
import org.pjsip.pjsua2.pjsua_call_flag
import org.pjsip.pjsua2.pjsua_call_media_status
import org.pjsip.pjsua2.pjsua_call_vid_strm_op
import org.pjsip.pjsua2.pjsua_vid_req_keyframe_method
import java.lang.Exception
import kotlin.jvm.Throws
import kotlin.jvm.java
import kotlin.jvm.javaClass
import kotlin.let
import kotlin.plus
import kotlin.ranges.until
import kotlin.text.lowercase
import kotlin.text.startsWith

class SipCall : Call {
    private val account: SipAccount
    private var localHold = false
    private var localMute = false
    private var localVideoMute = false
    private var connectTimestamp: Long = 0
    private var toneGenerator: ToneGenerator? = null
    private var videoCall = false
    private var videoConference = false
    private var frontCamera = true

    private var mVideoWindow: VideoWindow? = null
    private var mVideoPreview: VideoPreview? = null

    private var streamInfo: StreamInfo? = null
    private var streamStat: StreamStat? = null

    /**
     * constructor for incoming and outgoing calls.
     * @param account the account which owns this call
     * @param callID the id of this call (optional, default is -1 for outgoing calls)
     */
    constructor(account: SipAccount, callID: Int = -1) : super(account, callID) {
        this.account = account
        mVideoPreview = null
        mVideoWindow = null
    }
//class SipCall(
//    private val account: SipAccount,
//    callID: Int = -1
//) : Call(account, callID) {
//    private var localHold = false
//    private var localMute = false
//    private var localVideoMute = false
//    private var connectTimestamp: Long = 0
//    private var toneGenerator: ToneGenerator? = null
//    private var videoCall = false
//    private var videoConference = false
//    private var frontCamera = true
//
//    private var mVideoWindow: VideoWindow? = null
//    private var mVideoPreview: VideoPreview? = null
//
//    private var streamInfo: StreamInfo? = null
//    private var streamStat: StreamStat? = null
//
//    init {
//        mVideoPreview = null
//        mVideoWindow = null
//    }

    fun getCurrentState(): Int {
        try {
            val info: CallInfo = getInfo()
            return info.state
        } catch (exc: Exception) {
            Log.e(javaClass.getSimpleName(), "Error while getting call Info", exc)
            return pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED
        }
    }

    override fun onCallState(prm: OnCallStateParam?) {
        try {
            val info: CallInfo = getInfo()
            val callID: Int = info.id
            val callState: Int = info.state
            var callStatus: Int = pjsip_status_code.PJSIP_SC_NULL

            /*
             * From: http://www.pjsip.org/docs/book-latest/html/call.html#call-disconnection
             *
             * Call disconnection event is a special event since once the callback that
             * reports this event returns, the call is no longer valid and any operations
             * invoked to the call object will raise error exception.
             * Thus, it is recommended to delete the call object inside the callback.
             */
            try {
                callStatus = info.lastStatusCode
                account.getService().setLastCallStatus(callStatus)
            } catch (ex: Exception) {
                Log.e(TAG, "Error while getting call status", ex)
            }

            if (callState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                checkAndStopLocalRingBackTone()
                stopVideoFeeds()
                account.removeCall(callID)
                if (connectTimestamp > 0 && streamInfo != null && streamStat != null) {
                    try {
                        sendCallStats(callID, info.getConnectDuration().sec, callStatus)
                    } catch (ex: Exception) {
                        Log.e(TAG, "Error while sending call stats", ex)
                        throw ex
                    }
                }
            } else if (callState == pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED) {
                checkAndStopLocalRingBackTone()
                connectTimestamp = System.currentTimeMillis()
                if (videoCall) {
                    setVideoMute(false)
                }

                // check whether the 183 has arrived or not
            } else if (callState == pjsip_inv_state.PJSIP_INV_STATE_EARLY) {
                val statusCode: Int = info.lastStatusCode
                // check if 180 && call is outgoing (ROLE UAC)
                if (statusCode == pjsip_status_code.PJSIP_SC_RINGING && info.role == pjsip_role_e.PJSIP_ROLE_UAC) {

                    checkAndStopLocalRingBackTone()
                    toneGenerator = ToneGenerator(AudioManager.STREAM_VOICE_CALL, 100)
                    toneGenerator?.startTone(ToneGenerator.TONE_SUP_RINGTONE)
                    // check if 183
                } else if (statusCode == pjsip_status_code.PJSIP_SC_PROGRESS) {
                    checkAndStopLocalRingBackTone()

                }
            }

            account.getService().getBroadcastEmitter()!!.callState(
                account.getData().getIdUri(),
                callID,
                callState,
                callStatus,
                connectTimestamp
            )

            if (callState == pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED) {
                account.getService().setLastCallStatus(0)
                delete()
            }
        } catch (exc: Exception) {
            Log.e(TAG, "onCallState: error while getting call info", exc)
        }
    }

    override fun onCallMediaState(prm: OnCallMediaStateParam?) {
        var callInfo: CallInfo
        try {
            callInfo = info
        } catch (exc: Exception) {
            Log.e(TAG, "onCallMediaState: error while getting call info", exc)
            return
        }

        for (i in 0 until info.media.size) {
            val media: Media? = getMedia(i.toLong())
            val mediaInfo: CallMediaInfo = info.media[i]

            when {
                mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                        media != null &&
                        mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE -> {
                    handleAudioMedia(media)
                }

                mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                        mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE &&
                        mediaInfo.videoIncomingWindowId != pjsua2.INVALID_ID -> {
                    handleVideoMedia(mediaInfo)
                }
            }
        }
    }


    override fun onCallMediaEvent(prm: OnCallMediaEventParam) {
        val evType: Int = prm.getEv().type
        when (evType) {
            pjmedia_event_type.PJMEDIA_EVENT_FMT_CHANGED -> try {
                val callInfo: CallInfo = info
                val mediaInfo: CallMediaInfo = callInfo.getMedia()[prm.medIdx.toInt()]
                if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_VIDEO &&
                    mediaInfo.type == pjmedia_dir.PJMEDIA_DIR_DECODING
                ) {
                    val fmtEvent: MediaFmtChangedEvent = prm.getEv().getData().getFmtChanged()
                    Log.i(TAG, "Notify new video size")
                    account.getService().getBroadcastEmitter()!!.videoSize(
                        fmtEvent.newWidth.toInt(),
                        fmtEvent.newHeight.toInt()
                    )
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to get video dimensions", ex)
            }

            pjmedia_event_type.PJMEDIA_EVENT_RX_RTCP_FB -> {
                Log.d(TAG, "Keyframe request received")
                if (prm.ev.data?.rtcpFb != null &&
                    prm.ev.data.rtcpFb.fbType == pjmedia_rtcp_fb_type.PJMEDIA_RTCP_FB_NACK &&
                    prm.ev.data.rtcpFb.isParamLengthZero
                ) {
                    Log.i(TAG, "Sending new keyframe")
                    sendKeyFrame()
                }
            }
        }
        super.onCallMediaEvent(prm)
    }

    override fun onStreamDestroyed(prm: OnStreamDestroyedParam) {
        val idx: Long = prm.streamIdx
        try {
            val callInfo: CallInfo? = info
            if (callInfo!!.media[idx.toInt()].type == pjmedia_type.PJMEDIA_TYPE_AUDIO) {
                streamInfo = getStreamInfo(idx)
                streamStat = getStreamStat(idx)
            }
        } catch (ex: Exception) {
            Log.e(TAG, "onStreamDestroyed: error while getting call stats", ex)
        }
        super.onStreamDestroyed(prm)
    }

    /**
     * Get the total duration of the call.
     * @return the duration in milliseconds or 0 if the call is not connected.
     */
    fun getConnectTimestamp(): Long {
        return connectTimestamp
    }

    fun acceptIncomingCall() {
        val param: CallOpParam = CallOpParam()
        param.statusCode = pjsip_status_code.PJSIP_SC_OK
        setMediaParams(param)
        if (!videoCall) {
            val callSetting: CallSetting = param.getOpt()
            callSetting.flag = pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA.toLong()
        }
        try {
            answer(param)
        } catch (exc: Exception) {
            Log.e(TAG, "Failed to accept incoming call", exc)
        }
    }

    fun sendBusyHereToIncomingCall() {
        val param: CallOpParam = CallOpParam()
        param.statusCode = pjsip_status_code.PJSIP_SC_BUSY_HERE

        try {
            answer(param)
        } catch (exc: Exception) {
            Log.e(TAG, "Failed to send busy here", exc)
        }
    }

    fun declineIncomingCall() {
        val param: CallOpParam = CallOpParam()
        param.statusCode = pjsip_status_code.PJSIP_SC_DECLINE

        try {
            answer(param)
        } catch (exc: Exception) {
            Log.e(TAG, "Failed to decline incoming call", exc)
        }
    }

    fun hangUp() {
        val param: CallOpParam = CallOpParam()
        param.statusCode = pjsip_status_code.PJSIP_SC_DECLINE

        try {
            hangup(param)
        } catch (exc: Exception) {
            Log.e(TAG, "Failed to hangUp call", exc)
        }
    }

    /**
     * Utility method to mute/unmute the device microphone during a call.
     * @param mute true to mute the microphone, false to un-mute it
     */
    fun setMute(mute: Boolean) {
        // return immediately if we are not changing the current state
        if (localMute == mute) return

        var callInfo: CallInfo
        try {
            callInfo = info
        } catch (exc: Exception) {
            Log.e(TAG, "setMute: error while getting call info", exc)
            return
        }

        for (i in 0 until info.getMedia().size) {
            val media: Media? = getMedia(i.toLong())
            val mediaInfo: CallMediaInfo = info.getMedia()[i]

            if (mediaInfo.type == pjmedia_type.PJMEDIA_TYPE_AUDIO &&
                media != null &&
                mediaInfo.status == pjsua_call_media_status.PJSUA_CALL_MEDIA_ACTIVE
            ) {
                val audioMedia: AudioMedia? = AudioMedia.typecastFromMedia(media)

                // connect or disconnect the captured audio
                try {
                    val mgr: AudDevManager = account.getService().getAudDevManager()
                    if (mute) mgr.captureDevMedia.stopTransmit(audioMedia)
                    else mgr.captureDevMedia.startTransmit(audioMedia)
                    localMute = mute
                    account.getService().getBroadcastEmitter()!!.callMediaState(
                        account.getData().getIdUri(), id, MediaState.LOCAL_MUTE, localMute
                    )
                } catch (exc: Exception) {
                    Log.e(
                        TAG,
                        "setMute: error while connecting audio media to sound device",
                        exc
                    )
                }
            }
        }
    }

    fun isLocalMute(): Boolean {
        return localMute
    }

    fun toggleMute() {
        setMute(!localMute)
    }

    /**
     * Utility method to transfer a call to a number in the same realm as the account to
     * which this call belongs to. If you want to transfer the call to a different realm, you
     * have to pass the full string in this format: sip:NUMBER@REALM. E.g. sip:200@mycompany.com
     * @param destination destination to which to transfer the call.
     * @throws Exception if an error occurs during the call transfer
     */
//    @Throws(Exception::class)
//    fun transferTo(destination: String) {
//        var transferString: String?
//
//        if (destination.startsWith("sip:")) {
//            transferString = "<$destination>"
//        } else {
//            if ("*" == account.getData().getRealm()) {
//                transferString = "<sip:$destination>"
//            } else {
//                transferString =
//                    ("<sip:" + destination + "@" + account.getData().getRealm()).toString() + ">"
//            }
//        }
//
//        val param: CallOpParam = CallOpParam()
//
//        xfer(transferString, param)
//    }
    @Throws(Exception::class)
    fun transferTo(destination: String) {
        val transferString: String = when {
            destination.startsWith("sip:") -> "<$destination>"
            account.getData().realm == "*" -> "<sip:$destination>"
            else -> "<sip:$destination@${account.getData().realm}>"
        }

        val param = CallOpParam()
        xfer(transferString, param)
    }

    fun setHold(hold: Boolean) {
        // return immediately if we are not changing the current state
        if (localHold == hold) return

        val param: CallOpParam = CallOpParam()

        try {
            if (hold) {
                Log.d(TAG, "holding call with ID $id")
                setHold(param)
            } else {
                // http://lists.pjsip.org/pipermail/pjsip_lists.pjsip.org/2015-March/018246.html
                Log.d(TAG, "un-holding call with ID $id")
                setMediaParams(param)
                val opt: CallSetting = param.getOpt()
                opt.flag = pjsua_call_flag.PJSUA_CALL_UNHOLD.toLong()
                reinvite(param)
            }
            localHold = hold
            account.getService().getBroadcastEmitter()!!.callMediaState(
                account.getData().getIdUri(),
                id,
                MediaState.LOCAL_HOLD,
                localHold
            )
        } catch (exc: Exception) {
            val operation = if (hold) "hold" else "unhold"
            Log.e(TAG, "Error while trying to $operation call", exc)
        }
    }

    fun toggleHold() {
        setHold(!localHold)
    }

    fun isLocalHold(): Boolean {
        return localHold
    }

    // check if Local RingBack Tone has started, if so, stop it.
    private fun checkAndStopLocalRingBackTone() {
        if (toneGenerator != null) {
            toneGenerator!!.stopTone()
            toneGenerator!!.release()
            toneGenerator = null
        }
    }

    // disable video programmatically
    @Throws(Exception::class)
    override fun makeCall(dst_uri: String?, prm: CallOpParam) {
        setMediaParams(prm)
        if (!videoCall) {
            val callSetting: CallSetting = prm.getOpt()
            callSetting.flag = pjsua_call_flag.PJSUA_CALL_INCLUDE_DISABLED_MEDIA.toLong()
        }
        super.makeCall(dst_uri, prm)
    }

    private fun handleAudioMedia(media: Media?) {
        val audioMedia: AudioMedia? = AudioMedia.typecastFromMedia(media)

        // connect the call audio media to sound device
        try {
            val audDevManager: AudDevManager = account.getService().getAudDevManager()
            if (audioMedia != null) {
                try {
                    audioMedia.adjustRxLevel(1.5.toFloat())
                    audioMedia.adjustTxLevel(1.5.toFloat())
                } catch (exc: Exception) {
                    Log.e(TAG, "Error while adjusting levels", exc)
                }

                audioMedia.startTransmit(audDevManager.playbackDevMedia)
                audDevManager.captureDevMedia.startTransmit(audioMedia)
            }
        } catch (exc: Exception) {
            Log.e(TAG, "Error while connecting audio media to sound device", exc)
        }
    }

    private fun handleVideoMedia(mediaInfo: CallMediaInfo) {
        if (mVideoWindow != null) {
            mVideoWindow!!.delete()
        }
        if (mVideoPreview != null) {
            mVideoPreview!!.delete()
        }
        if (!videoConference) {
            // Since 2.9 pjsip will not start capture device if autoTransmit is false
            // thus mediaInfo.getVideoCapDev() always returns -3 -> NULL
            // mVideoPreview = new VideoPreview(mediaInfo.getVideoCapDev());
            mVideoPreview =
                VideoPreview(SipServiceConstants.Companion.FRONT_CAMERA_CAPTURE_DEVICE)
        }
        mVideoWindow = VideoWindow(mediaInfo.videoIncomingWindowId)
    }

    fun getVideoWindow(): VideoWindow? {
        return mVideoWindow
    }

    fun setVideoWindow(mVideoWindow: VideoWindow?) {
        this.mVideoWindow = mVideoWindow
    }

    fun getVideoPreview(): VideoPreview? {
        return mVideoPreview
    }

    fun setVideoPreview(mVideoPreview: VideoPreview?) {
        this.mVideoPreview = mVideoPreview
    }

    private fun stopVideoFeeds() {
        stopIncomingVideoFeed()
        stopPreviewVideoFeed()
    }

    fun setIncomingVideoFeed(surface: Surface?) {
        if (mVideoWindow != null) {
            val videoWindowHandle: VideoWindowHandle = VideoWindowHandle()
            videoWindowHandle.getHandle().setWindow(surface)
            try {
                mVideoWindow!!.setWindow(videoWindowHandle)
                account.getService().getBroadcastEmitter()!!.videoSize(
                    mVideoWindow!!.info.getSize().w.toInt(),
                    mVideoWindow!!.info.getSize().h.toInt()
                )

                // start video again if not mute
                setVideoMute(localVideoMute)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to setup Incoming Video Feed", ex)
            }
        }
    }

    fun startPreviewVideoFeed(surface: Surface?) {
        if (mVideoPreview != null) {
            val videoWindowHandle: VideoWindowHandle = VideoWindowHandle()
            videoWindowHandle.getHandle().setWindow(surface)
            val videoPreviewOpParam: VideoPreviewOpParam = VideoPreviewOpParam()
            videoPreviewOpParam.window = videoWindowHandle
            try {
                mVideoPreview!!.start(videoPreviewOpParam)
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to start Video Preview", ex)
            }
        }
    }

    fun stopIncomingVideoFeed() {
        val videoWindow: VideoWindow? = getVideoWindow()
        if (videoWindow != null) {
            try {
                videoWindow.delete()
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to stop remote video feed", ex)
            }
        }
    }

    fun stopPreviewVideoFeed() {
        val videoPreview: VideoPreview? = getVideoPreview()
        if (videoPreview != null) {
            try {
                videoPreview.stop()
            } catch (ex: Exception) {
                Log.e(TAG, "Unable to stop preview video feed", ex)
            }
        }
    }

    fun isVideoCall(): Boolean {
        return videoCall
    }

    fun isVideoConference(): Boolean {
        return videoConference
    }

    fun setVideoParams(videoCall: Boolean, videoConference: Boolean) {
        this.videoCall = videoCall
        this.videoConference = videoConference
    }

    private fun setMediaParams(param: CallOpParam) {
        val callSetting: CallSetting = param.getOpt()
        callSetting.audioCount = 1
        callSetting.videoCount = if (videoCall) 1 else 0
        callSetting.reqKeyframeMethod =
            pjsua_vid_req_keyframe_method.PJSUA_VID_REQ_KEYFRAME_RTCP_PLI.toLong()
    }

    fun setVideoMute(videoMute: Boolean) {
        try {
            vidSetStream(
                if (videoMute)
                    pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_STOP_TRANSMIT
                else
                    pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_START_TRANSMIT,
                CallVidSetStreamParam()
            )
            localVideoMute = videoMute
            account.getService().getBroadcastEmitter()!!.callMediaState(
                account.getData().getIdUri(), id, MediaState.LOCAL_VIDEO_MUTE, localVideoMute
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Error while toggling video transmission", ex)
        }
    }

    fun isLocalVideoMute(): Boolean {
        return localVideoMute
    }

    fun isFrontCamera(): Boolean {
        return frontCamera
    }

    fun setFrontCamera(frontCamera: Boolean) {
        this.frontCamera = frontCamera
    }

    private fun sendKeyFrame() {
        try {
            vidSetStream(
                pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_SEND_KEYFRAME,
                CallVidSetStreamParam()
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Error sending keyframe", ex)
        }
    }

    private fun sendCallStats(callID: Int, duration: Int, callStatus: Int) {
        val audioCodec: String =
            streamInfo?.codecName?.lowercase() + "_" + streamInfo?.codecClockRate

        val rxStat: RtcpStreamStat? = streamStat?.getRtcp()?.getRxStat()
        val txStat: RtcpStreamStat? = streamStat?.getRtcp()?.getTxStat()

        val rxJitter: Jitter? = rxStat?.getJitterUsec()?.let { jitter ->
            Jitter(
                jitter.max,
                jitter.mean,
                jitter.min
            )
        }

        val txJitter: Jitter? = txStat?.getJitterUsec()?.let { jitter ->
            Jitter(
                jitter.max,
                jitter.mean,
                jitter.min
            )
        }

        val rx: RtpStreamStats? = rxStat?.let { stat ->
            RtpStreamStats(
                stat.pkt,
                stat.discard,
                stat.loss,
                stat.reorder,
                stat.dup,
                rxJitter
            )
        }

        val tx: RtpStreamStats? = txStat?.let { stat ->
            RtpStreamStats(
                stat.pkt,
                stat.discard,
                stat.loss,
                stat.reorder,
                stat.dup,
                txJitter
            )
        }

        account.getService().getBroadcastEmitter()!!
            .callStats(callID, duration, audioCodec, callStatus, rx, tx)
        streamInfo = null
        streamStat = null
    }

    companion object {
        internal val TAG = SipCall::class.java.simpleName
    }
}

