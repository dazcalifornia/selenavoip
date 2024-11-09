package com.synapes.selenvoip

import android.util.Log
import java.lang.Exception
import java.util.HashMap
import org.pjsip.pjsua2.Account
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.OnIncomingCallParam
import org.pjsip.pjsua2.OnRegStateParam
import org.pjsip.pjsua2.pjsip_status_code
import kotlin.jvm.Throws

class SipAccount(
    private val service: SipService,
    private val data: SipAccountData
) : Account() {
    private val activeCalls = HashMap<Int?, SipCall?>()
    private var isGuest = false

    fun getService(): SipService = service

    fun getData(): SipAccountData = data

    @Throws(Exception::class)
    fun create() {
        Log.d(TAG, "Creating account with SIP config ${data.getAccountConfig().sipConfig}")
        create(data.getAccountConfig())
    }

    @Throws(Exception::class)
    fun createGuest() {
        isGuest = true
        create(data.getGuestAccountConfig())
    }

    fun removeCall(callId: Int) {
        val call = activeCalls[callId]

        if (call != null) {
            Log.d(TAG, "Removing call with ID: $callId")
            activeCalls.remove(callId)
        }

        if (isGuest) {
            service.removeGuestAccount()
            delete()
        }
    }

    fun getCall(callId: Int): SipCall? {
        return activeCalls[callId]
    }

    fun getCallIDs(): MutableSet<Int?> {
        return activeCalls.keys
    }

    fun addIncomingCall(callId: Int): SipCall {
        val call = SipCall(this, callId)
        activeCalls[callId] = call
        Log.d(
            TAG, "Added incoming call with ID $callId to ${data.getIdUri()}"
        )
        return call
    }

    fun addOutgoingCall(
        numberToDial: String,
        isVideo: Boolean,
        isVideoConference: Boolean,
        isTransfer: Boolean
    ): SipCall? {
        var totalCalls = 0
        for (_sipAccount in SipService.getActiveSipAccounts().values) {
            totalCalls += _sipAccount!!.getCallIDs().size
        }

        if (totalCalls <= (if (isTransfer) 1 else 0)) {
            val call = SipCall(this)
            call.setVideoParams(isVideo, isVideoConference)

            val callOpParam = CallOpParam()
            try {
                if (numberToDial.startsWith("sip:")) {
                    call.makeCall(numberToDial, callOpParam)
                } else {
                    if ("*" == data.realm) {
                        call.makeCall("sip:$numberToDial", callOpParam)
                    } else {
                        call.makeCall(
                            "sip:$numberToDial@${data.realm}",
                            callOpParam
                        )
                    }
                }
                activeCalls[call.id] = call
                Log.d(TAG, "New outgoing call with ID: ${call.id}")

                return call
            } catch (exc: Exception) {
                Log.e(TAG, "Error while making outgoing call", exc)
                return null
            }
        }
        return null
    }

    fun addOutgoingCall(numberToDial: String): SipCall? {
        return addOutgoingCall(numberToDial, false, false, false)
    }

    override fun onRegState(prm: OnRegStateParam) {
        Log.i(
            TAG,
            "Sip Reg Info - Code: ${prm.code}, Reason: ${prm.reason}, Exp: ${prm.expiration}, Status: ${prm.status}"
        )
        service.getBroadcastEmitter()?.registrationState(
            data.getIdUri(),
            prm.code
        )
    }

    override fun onIncomingCall(prm: OnIncomingCallParam) {
        val call = addIncomingCall(prm.callId)

        if (service.isDND()) {
            try {
                val contactInfo = CallerInfo(call.getInfo())
                service.getBroadcastEmitter()
                    ?.missedCall(contactInfo.getDisplayName(), contactInfo.getRemoteUri())
                call.declineIncomingCall()
                Log.d(TAG, "DND - Decline call with ID: ${prm.callId}")
            } catch (ex: Exception) {
                Log.e(TAG, "Error while getting -missed because declined- call info", ex)
            }
            return
        }

        var totalCalls = 0
        for (_sipAccount in SipService.getActiveSipAccounts().values) {
            totalCalls += _sipAccount!!.getCallIDs().size
        }

        if (totalCalls > 1) {
            try {
                val contactInfo = CallerInfo(call.info)
                service.getBroadcastEmitter()
                    ?.missedCall(contactInfo.getDisplayName(), contactInfo.getRemoteUri())
                call.sendBusyHereToIncomingCall()
                Log.d(TAG, "Sending busy to call ID: ${prm.callId}")
            } catch (ex: Exception) {
                Log.e(TAG, "Error while getting missed call info", ex)
            }
            return
        }

        try {
            val callOpParam = CallOpParam()
            callOpParam.statusCode = pjsip_status_code.PJSIP_SC_RINGING
            call.answer(callOpParam)
            Log.d(TAG, "Sending 180 ringing")

            var displayName: String?
            var remoteUri: String?
            try {
                val contactInfo = CallerInfo(call.info)
                displayName = contactInfo.getDisplayName()
                remoteUri = contactInfo.getRemoteUri()
            } catch (ex: Exception) {
                Log.e(TAG, "Error while getting caller info", ex)
                throw ex
            }

            val callInfo = call.info
            val isVideo = (callInfo.remOfferer && callInfo.remVideoCount > 0)

            service.getBroadcastEmitter()?.incomingCall(
                data.getIdUri(), prm.callId,
                displayName, remoteUri, isVideo
            )
        } catch (ex: Exception) {
            Log.e(TAG, "Error while getting caller info", ex)
        }
    }

    companion object {
        private val TAG: String = SipAccount::class.java.simpleName
    }
}
