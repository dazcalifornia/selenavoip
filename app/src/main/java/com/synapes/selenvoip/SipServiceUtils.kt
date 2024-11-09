package com.synapes.selenvoip

import android.util.Log
import com.synapes.selenvoip.SipServiceConstants.Companion.H264_DEF_HEIGHT
import com.synapes.selenvoip.SipServiceConstants.Companion.H264_DEF_WIDTH
import com.synapes.selenvoip.SipServiceConstants.Companion.OPENH264_CODEC_ID
import com.synapes.selenvoip.SipServiceConstants.Companion.PROFILE_LEVEL_ID_HEADER
import com.synapes.selenvoip.SipServiceConstants.Companion.PROFILE_LEVEL_ID_JANUS_BRIDGE
import org.pjsip.pjsua2.CodecFmtpVector
import org.pjsip.pjsua2.CodecInfo
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.LogConfig
import org.pjsip.pjsua2.MediaFormatVideo
import org.pjsip.pjsua2.VidCodecParam
import java.lang.Exception
import java.lang.StringBuilder
import java.util.ArrayList
import kotlin.collections.indices
import kotlin.jvm.Throws

object SipServiceUtils {
    private const val TAG = "SipServiceUtils"
    var ENABLE_SIP_LOGGING: Boolean = false

    // Keeping the reference avoids the logger being garbage collected thus crashing the lib
    private var sipLogger: SipLogger? = null

    /**
     * Sets logger writer and decor flags on the endpoint config
     * Change flags as needed
     */
    fun setSipLogger(epConfig: EpConfig) {
        val logCfg: LogConfig = epConfig.getLogConfig()
        sipLogger = SipLogger()
        logCfg.writer = sipLogger
        logCfg.decor = sipLogger!!.getDecor().toLong()
        logCfg.level = (if (ENABLE_SIP_LOGGING) 5 else 0).toLong()
    }

    @Throws(Exception::class)
    fun setAudioCodecPriorities(
        codecPriorities: ArrayList<CodecPriority>?,
        sipEndpoint: SipEndpoint
    ) {
        if (codecPriorities != null) {
            Log.d(TAG, "Setting saved codec priorities...")
            val log = StringBuilder()
            log.append("Saved codec priorities set:\n")
            for (codecPriority in codecPriorities) {
                sipEndpoint.codecSetPriority(
                    codecPriority.getCodecId(),
                    codecPriority.getPriority().toShort()
                )
                log.append(codecPriority).append(",")
            }
            Log.d(TAG, log.toString())
        } else {
            sipEndpoint.codecSetPriority("OPUS", (CodecPriority.PRIORITY_MAX - 1).toShort())
            sipEndpoint.codecSetPriority("PCMA/8000", (CodecPriority.PRIORITY_MAX - 2).toShort())
            sipEndpoint.codecSetPriority("PCMU/8000", (CodecPriority.PRIORITY_MAX - 3).toShort())
            sipEndpoint.codecSetPriority("G729/8000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("speex/8000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("speex/16000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("speex/32000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("GSM/8000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("G722/16000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("G7221/16000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("G7221/32000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("ilbc/8000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("AMR-WB/16000", CodecPriority.PRIORITY_DISABLED.toShort())
            sipEndpoint.codecSetPriority("AMR/8000", CodecPriority.PRIORITY_DISABLED.toShort())
            Log.d(TAG, "Default codec priorities set!")
        }
    }

    @Throws(Exception::class)
    fun setVideoCodecPriorities(sipEndpoint: SipEndpoint) {
        sipEndpoint.videoCodecSetPriority(
            OPENH264_CODEC_ID,
            (CodecPriority.PRIORITY_MAX_VIDEO - 1).toShort()
        )

        for (codecInfo in sipEndpoint.videoCodecEnum2()) {
            if (OPENH264_CODEC_ID != codecInfo.codecId) {
                sipEndpoint.videoCodecSetPriority(
                    codecInfo.codecId,
                    CodecPriority.PRIORITY_DISABLED.toShort()
                )
            }
        }

        // Set H264 Parameters
        val vidCodecParam: VidCodecParam = sipEndpoint.getVideoCodecParam(OPENH264_CODEC_ID)
        val codecFmtpVector: CodecFmtpVector = vidCodecParam.getDecFmtp()
        val mediaFormatVideo: MediaFormatVideo = vidCodecParam.getEncFmt()
        mediaFormatVideo.width = H264_DEF_WIDTH.toLong()
        mediaFormatVideo.height = H264_DEF_HEIGHT.toLong()
        vidCodecParam.encFmt = mediaFormatVideo

        for (i in codecFmtpVector.indices) {
            if (PROFILE_LEVEL_ID_HEADER.equals(codecFmtpVector.get(i).getName())) {
                codecFmtpVector.get(i).setVal(PROFILE_LEVEL_ID_JANUS_BRIDGE)
                break
            }
        }
        vidCodecParam.decFmtp = codecFmtpVector
        sipEndpoint.setVideoCodecParam(OPENH264_CODEC_ID, vidCodecParam)
    }
}
