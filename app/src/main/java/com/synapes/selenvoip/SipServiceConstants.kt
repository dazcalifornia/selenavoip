package com.synapes.selenvoip

interface SipServiceConstants {
    companion object {
        /*
     * Intent Actions for Sip Service
     */
        const val ACTION_RESTART_SIP_STACK: String = "restartSipStack"
        const val ACTION_SET_ACCOUNT: String = "setAccount"
        const val ACTION_REMOVE_ACCOUNT: String = "removeAccount"
        const val ACTION_MAKE_CALL: String = "makeCall"
        const val ACTION_HANG_UP_CALL: String = "hangUpCall"
        const val ACTION_HANG_UP_CALLS: String = "hangUpCalls"
        const val ACTION_HOLD_CALLS: String = "holdCalls"
        const val ACTION_GET_CALL_STATUS: String = "getCallStatus"
        const val ACTION_SEND_DTMF: String = "sendDtmf"
        const val ACTION_ACCEPT_INCOMING_CALL: String = "acceptIncomingCall"
        const val ACTION_DECLINE_INCOMING_CALL: String = "declineIncomingCall"
        const val ACTION_SET_HOLD: String = "callSetHold"
        const val ACTION_SET_MUTE: String = "callSetMute"
        const val ACTION_TOGGLE_HOLD: String = "callToggleHold"
        const val ACTION_TOGGLE_MUTE: String = "callToggleMute"
        const val ACTION_TRANSFER_CALL: String = "callTransfer"
        const val ACTION_ATTENDED_TRANSFER_CALL: String = "callAttendedTransfer"
        const val ACTION_GET_CODEC_PRIORITIES: String = "codecPriorities"
        const val ACTION_SET_CODEC_PRIORITIES: String = "setCodecPriorities"
        const val ACTION_GET_REGISTRATION_STATUS: String = "getRegistrationStatus"
        const val ACTION_REFRESH_REGISTRATION: String = "refreshRegistration"
        const val ACTION_SET_DND: String = "setDND"
        const val ACTION_SET_INCOMING_VIDEO: String = "setIncomingVideo"
        const val ACTION_SET_SELF_VIDEO_ORIENTATION: String = "setSelfVideoOrientation"
        const val ACTION_SET_VIDEO_MUTE: String = "setVideoMute"
        const val ACTION_START_VIDEO_PREVIEW: String = "startVideoPreview"
        const val ACTION_STOP_VIDEO_PREVIEW: String = "stopVideoPreview"
        const val ACTION_SWITCH_VIDEO_CAPTURE_DEVICE: String = "switchVideoCaptureDevice"
        const val ACTION_MAKE_DIRECT_CALL: String = "makeDirectCall"
        const val ACTION_RECONNECT_CALL: String = "reconnectCall"
        const val ACTION_MAKE_SILENT_CALL: String = "makeSilentCall"

        /*
     * Generic Parameters
     */
        const val PARAM_ACCOUNT_DATA: String = "accountData"
        const val PARAM_ACCOUNT_ID: String = "accountID"
        const val PARAM_NUMBER: String = "number"
        const val PARAM_CALL_ID: String = "callId"
        const val PARAM_CALL_ID_DEST: String = "callIdDest"
        const val PARAM_DTMF: String = "dtmf"
        const val PARAM_HOLD: String = "hold"
        const val PARAM_MUTE: String = "mute"
        const val PARAM_CODEC_PRIORITIES: String = "codecPriorities"
        const val PARAM_REG_EXP_TIMEOUT: String = "regExpTimeout"
        const val PARAM_REG_CONTACT_PARAMS: String = "regContactParams"
        const val PARAM_DND: String = "dnd"
        const val PARAM_IS_VIDEO: String = "isVideo"
        const val PARAM_IS_VIDEO_CONF: String = "isVideoConference"
        const val PARAM_SURFACE: String = "surface"
        const val PARAM_ORIENTATION: String = "orientation"
        const val PARAM_GUEST_NAME: String = "guestName"
        const val PARAM_DIRECT_CALL_URI: String = "sipUri"
        const val PARAM_DIRECT_CALL_SIP_SERVER: String = "sipServer"
        const val PARAM_DIRECT_CALL_TRANSPORT: String = "directTransport"
        const val PARAM_IS_TRANSFER: String = "isTransfer"

        /**
         * Specific Parameters passed in the broadcast intents.
         */
        const val PARAM_REGISTRATION_CODE: String = "registrationCode"
        const val PARAM_REMOTE_URI: String = "synapes-pbx-poc-01.online"
        const val PARAM_DISPLAY_NAME: String = "displayName"
        const val PARAM_CALL_STATE: String = "callState"
        const val PARAM_CALL_STATUS: String = "callStatus"
        const val PARAM_CONNECT_TIMESTAMP: String = "connectTimestamp"
        const val PARAM_STACK_STARTED: String = "stackStarted"
        const val PARAM_CODEC_PRIORITIES_LIST: String = "codecPrioritiesList"
        const val PARAM_MEDIA_STATE_KEY: String = "mediaStateKey"
        const val PARAM_MEDIA_STATE_VALUE: String = "mediaStateValue"
        const val PARAM_VIDEO_MUTE: String = "videoMute"
        const val PARAM_SUCCESS: String = "success"
        const val PARAM_INCOMING_VIDEO_WIDTH: String = "incomingVideoWidth"
        const val PARAM_INCOMING_VIDEO_HEIGHT: String = "incomingVideoHeight"
        const val PARAM_CALL_RECONNECTION_STATE: String = "callReconnectionState"
        const val PARAM_SILENT_CALL_STATUS: String = "silentCallStatus"

        /**
         * Specific Parameters passed in the broadcast intents for call stats.
         */
        const val PARAM_CALL_STATS_DURATION: String = "callStatsDuration"
        const val PARAM_CALL_STATS_AUDIO_CODEC: String = "callStatsAudioCodec"
        const val PARAM_CALL_STATS_CALL_STATUS: String = "callStatsCallStatus"
        const val PARAM_CALL_STATS_RX_STREAM: String = "callStatsRxStream"
        const val PARAM_CALL_STATS_TX_STREAM: String = "callStatsTxStream"

        /**
         * Video Configuration Params
         */
        const val FRONT_CAMERA_CAPTURE_DEVICE: Int = 1 // Front Camera idx
        const val BACK_CAMERA_CAPTURE_DEVICE: Int = 2 // Back Camera idx
        const val DEFAULT_RENDER_DEVICE: Int = 0 // OpenGL Render
        const val OPENH264_CODEC_ID: String = "H264/97"
        const val H264_DEF_WIDTH: Int = 640
        const val H264_DEF_HEIGHT: Int = 360
        const val ANDROID_H264_CODEC_ID: String = "H264/99"
        const val ANDROID_VP8_CODEC_ID: String = "VP8/103"
        const val ANDROID_VP9_CODEC_ID: String = "VP9/106"

        /**
         * Janus Bridge call specific parameters.
         */
        const val PROFILE_LEVEL_ID_HEADER: String = "profile-level-id"
        const val PROFILE_LEVEL_ID_LOCAL: String = "42e01e"
        const val PROFILE_LEVEL_ID_JANUS_BRIDGE: String = "42e01f"

        /**
         * Generic Constants
         */
        const val DELAYED_JOB_DEFAULT_DELAY: Int = 5000

        /**
         * SIP DEFAULT PORTS
         */
        const val DEFAULT_SIP_PORT: Int = 5060

        /**
         * PJSIP TLS VERIFY PEER ERROR
         */
        const val PJSIP_TLS_ECERTVERIF: Int = 171173
    }
}
