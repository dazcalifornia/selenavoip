package com.synapes.selenvoip

import java.io.Serializable

class Config {
    companion object {
        const val PERMISSION_REQUEST_CODE = 555
        const val NAMESPACE = "com.synapes.selenvoip"
        const val AGENT_NAME = "SelenVOIP"

    }
}

enum class MediaState: Serializable {
    LOCAL_HOLD,
    LOCAL_MUTE,
    LOCAL_VIDEO_MUTE
}

enum class SipAccountTransport {
    UDP,
    TCP,
    TLS;

    companion object {
        fun getTransportByCode(code: Int): SipAccountTransport {
            when (code) {
                0 -> return UDP
                1 -> return TCP
                2 -> return TLS
                else -> return UDP
            }
        }
    }
}

enum class CallReconnectionState: Serializable {
    FAILED,
    PROGRESS,
    SUCCESS
}

data class DeviceInfo(
    val device_id: String,
    val extension_number: String,
    val extension_password: String,
    val destination_number: String,
    val phone_number: String,
    val company_name: String,
    val location_description: String,
    val lat: String,
    val lon: String,
    val is_online: Boolean,
    val missing_fields: List<String>,
    val needs_attention: Boolean
)

enum class Transformation(val type: String) {
    ASYMMETRIC("RSA/ECB/PKCS1Padding"),
    SYMMETRIC("AES/CBC/PKCS7Padding")
}