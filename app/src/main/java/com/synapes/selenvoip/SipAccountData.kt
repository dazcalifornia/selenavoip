package com.synapes.selenvoip

import android.os.Parcel
import android.os.Parcelable
import org.pjsip.pjsua2.AccountConfig
import org.pjsip.pjsua2.AuthCredInfo
import org.pjsip.pjsua2.pj_constants_
import org.pjsip.pjsua2.pj_qos_type
import org.pjsip.pjsua2.pjmedia_srtp_use

class SipAccountData : Parcelable {
    private val sessionTimerExpireSec = 600

    var username: String? = null
    var password: String? = null
    var realm: String? = null
    var host: String? = null
    var port: Int = SipServiceConstants.DEFAULT_SIP_PORT

    private var tcpTransport = false
    private var authenticationType: String? = AUTH_TYPE_DIGEST
    private var contactUriParams: String? = ""
    private var regExpirationTimeout = 300 // 300s
    private var guestDisplayName: String? = ""
    private var callId: String? = ""
    private var srtpUse: Int = pjmedia_srtp_use.PJMEDIA_SRTP_OPTIONAL
    private var srtpSecureSignalling = 0 // not required
    var transport: SipAccountTransport = SipAccountTransport.UDP

    constructor()

    constructor(parcel: Parcel) {
        username = parcel.readString()
        password = parcel.readString()
        realm = parcel.readString()
        host = parcel.readString()
        port = parcel.readInt()
        tcpTransport = parcel.readByte().toInt() == 1
        authenticationType = parcel.readString()
        contactUriParams = parcel.readString()
        regExpirationTimeout = parcel.readInt()
        guestDisplayName = parcel.readString()
        callId = parcel.readString()
        srtpUse = parcel.readInt()
        srtpSecureSignalling = parcel.readInt()
        transport = SipAccountTransport.entries.toTypedArray()[parcel.readInt()]
    }

    fun setUsername(username: String?): SipAccountData {
        this.username = username
        return this
    }

    fun setPassword(password: String?): SipAccountData {
        this.password = password
        return this
    }

    fun setRealm(realm: String?): SipAccountData {
        this.realm = realm
        return this
    }

    fun setHost(host: String?): SipAccountData {
        this.host = host
        return this
    }

    fun setPort(port: Int): SipAccountData {
        this.port = port
        return this
    }

    fun setTransport(transport: SipAccountTransport): SipAccountData {
        this.transport = transport
        this.tcpTransport = false // Cancel all tcpTransport usefulness
        return this
    }

    fun setAuthenticationType(authenticationType: String?): SipAccountData {
        this.authenticationType = authenticationType
        return this
    }

    fun setContactUriParams(contactUriParams: String?): SipAccountData {
        this.contactUriParams = contactUriParams
        return this
    }

    fun getContactUriParams(): String? {
        return contactUriParams
    }

    fun setRegExpirationTimeout(regExpirationTimeout: Int): SipAccountData {
        this.regExpirationTimeout = regExpirationTimeout
        return this
    }

    fun getRegExpirationTimeout(): Int {
        return this.regExpirationTimeout
    }

    fun setGuestDisplayName(guestDisplayName: String?): SipAccountData {
        this.guestDisplayName = guestDisplayName
        return this
    }

    fun setCallId(callId: String?): SipAccountData {
        this.callId = callId
        return this
    }

    fun setSrtpUse(srtpUse: Int): SipAccountData {
        this.srtpUse = srtpUse
        return this
    }

    fun setSrtpSecureSignalling(srtpSecureSignalling: Int): SipAccountData {
        this.srtpSecureSignalling = srtpSecureSignalling
        return this
    }

    fun getAuthCredInfo(): AuthCredInfo {
        return AuthCredInfo(authenticationType, realm, username, 0, password)
    }

    fun getIdUri(): String {
        return if (realm == "*" || realm.isNullOrEmpty()) {
            "sip:$username"
        } else {
            "sip:$username@$realm"
        }
    }

    fun getProxyUri(): String {
        return "sip:$host:$port${getTransportString()}"
    }

    fun getRegistrarUri(): String {
        return "sip:$host:$port"
    }

    private fun getTransportString(): String {
        return when (transport) {
            SipAccountTransport.TCP -> ";transport=tcp"
            SipAccountTransport.TLS -> ";transport=tls"
            SipAccountTransport.UDP -> {
                if (tcpTransport) ";transport=tcp" else ""
            }
        }
    }

    fun isValid(): Boolean {
        return !username.isNullOrEmpty() &&
                !password.isNullOrEmpty() &&
                !host.isNullOrEmpty() &&
                !realm.isNullOrEmpty()
    }

    fun getAccountConfig(): AccountConfig {
//        val accountConfig = AccountConfig()
//        accountConfig.idUri = getIdUri()
//
//        if (!callId.isNullOrEmpty()) {
//            accountConfig.regConfig.callID = callId
//        }
//        accountConfig.regConfig.registrarUri = getRegistrarUri()
//        accountConfig.regConfig.timeoutSec = regExpirationTimeout.toLong()
//        accountConfig.regConfig.contactUriParams = contactUriParams
//
//        accountConfig.sipConfig.authCreds.add(getAuthCredInfo())
//        accountConfig.sipConfig.proxies.add(getProxyUri())
//
//        accountConfig.natConfig.sdpNatRewriteUse = pj_constants_.PJ_TRUE
//        accountConfig.natConfig.viaRewriteUse = pj_constants_.PJ_TRUE
//
//        accountConfig.mediaConfig.transportConfig.qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
//        accountConfig.mediaConfig.srtpUse = srtpUse
//        accountConfig.mediaConfig.srtpSecureSignaling = srtpSecureSignalling
//        setVideoConfig(accountConfig)
//
//        accountConfig.callConfig.timerSessExpiresSec = sessionTimerExpireSec.toLong()
//
//        return accountConfig
        val accountConfig = AccountConfig()
        accountConfig.idUri = getIdUri()
//        println("IdUri: ${accountConfig.idUri}")

        if (!callId.isNullOrEmpty()) {
            accountConfig.regConfig.callID = callId
//            println("CallID: ${accountConfig.regConfig.callID}")
        }
        accountConfig.regConfig.registrarUri = getRegistrarUri()
//        println("RegistrarUri: ${accountConfig.regConfig.registrarUri}")
        accountConfig.regConfig.timeoutSec = regExpirationTimeout.toLong()
//        println("TimeoutSec: ${accountConfig.regConfig.timeoutSec}")
        accountConfig.regConfig.contactUriParams = contactUriParams
//        println("ContactUriParams: ${accountConfig.regConfig.contactUriParams}")

        accountConfig.sipConfig.authCreds.add(getAuthCredInfo())
//        println("AuthCreds: ${accountConfig.sipConfig.authCreds}")
        accountConfig.sipConfig.proxies.add(getProxyUri())
//        println("Proxies: ${accountConfig.sipConfig.proxies}")

        accountConfig.natConfig.sdpNatRewriteUse = pj_constants_.PJ_TRUE
//        println("SdpNatRewriteUse: ${accountConfig.natConfig.sdpNatRewriteUse}")
        accountConfig.natConfig.viaRewriteUse = pj_constants_.PJ_TRUE
//        println("ViaRewriteUse: ${accountConfig.natConfig.viaRewriteUse}")

        accountConfig.mediaConfig.transportConfig.qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
//        println("QosType: ${accountConfig.mediaConfig.transportConfig.qosType}")
        accountConfig.mediaConfig.srtpUse = srtpUse
//        println("SrtpUse: ${accountConfig.mediaConfig.srtpUse}")
        accountConfig.mediaConfig.srtpSecureSignaling = srtpSecureSignalling
//        println("SrtpSecureSignaling: ${accountConfig.mediaConfig.srtpSecureSignaling}")

        // Set video config
        accountConfig.videoConfig.autoTransmitOutgoing = false
//        println("AutoTransmitOutgoing: ${accountConfig.videoConfig.autoTransmitOutgoing}")
        accountConfig.videoConfig.autoShowIncoming = true
//        println("AutoShowIncoming: ${accountConfig.videoConfig.autoShowIncoming}")
        accountConfig.videoConfig.defaultCaptureDevice =
            SipServiceConstants.FRONT_CAMERA_CAPTURE_DEVICE
//        println("DefaultCaptureDevice: ${accountConfig.videoConfig.defaultCaptureDevice}")
        accountConfig.videoConfig.defaultRenderDevice = SipServiceConstants.DEFAULT_RENDER_DEVICE
//        println("DefaultRenderDevice: ${accountConfig.videoConfig.defaultRenderDevice}")

        accountConfig.callConfig.timerSessExpiresSec = sessionTimerExpireSec.toLong()
//        println("TimerSessExpiresSec: ${accountConfig.callConfig.timerSessExpiresSec}")

        return accountConfig
    }

    fun getGuestAccountConfig(): AccountConfig {
        val accountConfig = AccountConfig()
        accountConfig.mediaConfig.transportConfig.qosType = pj_qos_type.PJ_QOS_TYPE_VIDEO
        val idUri =
            if (guestDisplayName.isNullOrEmpty()) getIdUri() else "\"$guestDisplayName\" <${getIdUri()}>"
        accountConfig.idUri = idUri
        accountConfig.sipConfig.proxies.add(getProxyUri())
        accountConfig.regConfig.registerOnAdd = false
        accountConfig.callConfig.timerSessExpiresSec = sessionTimerExpireSec.toLong()
        setVideoConfig(accountConfig)
        return accountConfig
    }

    private fun setVideoConfig(accountConfig: AccountConfig) {
        accountConfig.videoConfig.autoTransmitOutgoing = false
        accountConfig.videoConfig.autoShowIncoming = true
        accountConfig.videoConfig.defaultCaptureDevice =
            SipServiceConstants.FRONT_CAMERA_CAPTURE_DEVICE
        accountConfig.videoConfig.defaultRenderDevice = SipServiceConstants.DEFAULT_RENDER_DEVICE
    }
    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(username)
        parcel.writeString(password)
        parcel.writeString(realm)
        parcel.writeString(host)
        parcel.writeInt(port)
        parcel.writeByte(if (tcpTransport) 1 else 0)
        parcel.writeString(authenticationType)
        parcel.writeString(contactUriParams)
        parcel.writeInt(regExpirationTimeout)
        parcel.writeString(guestDisplayName)
        parcel.writeString(callId)
        parcel.writeInt(srtpUse)
        parcel.writeInt(srtpSecureSignalling)
        parcel.writeInt(transport.ordinal)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        const val AUTH_TYPE_DIGEST: String = "digest"

        @JvmField
        val CREATOR: Parcelable.Creator<SipAccountData> =
            object : Parcelable.Creator<SipAccountData> {
                override fun createFromParcel(parcel: Parcel): SipAccountData {
                    return SipAccountData(parcel)
                }

                override fun newArray(size: Int): Array<SipAccountData?> {
                    return arrayOfNulls(size)
                }
            }
    }
}

