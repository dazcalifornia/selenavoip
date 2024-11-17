package com.synapes.selenvoip

import java.io.Serializable

class Config {
    companion object {
        const val PERMISSION_REQUEST_CODE = 555
        const val NAMESPACE = "com.synapes.selenvoip"
        const val AGENT_NAME = "SelenVOIP"
        const val SHARED_PREFS_AUTHORITY = "com.synapes.selenvoip.provider"
        const val PREFS_URI = 1
        const val PREFS_FILENAME = "selen_voip_shared_preferences"
        const val PREFS_ENCRYPTED_FILE_NAME = "encrypted_selen_voip_shared_preferences"
        const val PREFS_KEY_ACCOUNTS = "accounts"
        const val PREFS_KEY_CODEC_PRIORITIES = "codec_priorities"
        const val PREFS_KEY_DND = "dnd_pref"
        const val PREFS_KEY_VERIFY_SIP_SERVER_CERT = "sip_server_cert_verification_enabled"
        const val PREFS_KEY_ENCRYPTION_ENABLED = "encryption_enabled"
        const val PREFS_KEY_KEYSTORE_ALIAS = "keystore_alias"
        const val PREFS_KEY_OBFUSCATION_ENABLED = "obfuscation_enabled"

        const val KEY_EXTENSION_NUMBER = "extension_number"
        const val KEY_EXTENSION_PASSWORD = "extension_password"
        const val KEY_DESTINATION_NUMBER = "destination_number"
        const val KEY_LAT = "lat"
        const val KEY_LON = "lon"
        const val KEY_IS_ONLINE = "is_online"
        const val KEY_MISSING_FIELDS = "missing_fields"
        const val KEY_NEEDS_ATTENTION = "needs_attention"
        const val KEY_PHONE_NUMBER = "phone_number"
        const val KEY_COMPANY_NAME = "company_name"
        const val KEY_LOCATION_DESCRIPTION = "location_description"
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