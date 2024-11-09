package com.synapes.selenvoip

import android.util.Log
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.OnIpChangeProgressParam
import org.pjsip.pjsua2.OnTransportStateParam
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.pj_constants_
import org.pjsip.pjsua2.pjsip_transport_type_e
import java.util.ArrayList


class SipEndpoint(private val service: SipService) : Endpoint() {

    override fun onTransportState(prm: OnTransportStateParam) {
        super.onTransportState(prm)

//        if (SharedPreferencesHelper.isVerifySipServerCert() &&
//            prm.type.equals("TLS", ignoreCase = true)
//        ) {
//            val verifyMsg = prm.getTlsInfo().verifyStatus
//            val binSuccessMsg = pj_ssl_cert_verify_flag_t.PJ_SSL_CERT_ESUCCESS
//            val binIdentityNotMatchMsg = pj_ssl_cert_verify_flag_t.PJ_SSL_CERT_EIDENTITY_NOT_MATCH
//            val isSuccess = verifyMsg == Int.toLong()
//            val isIdentityMismatch = verifyMsg == Int.toLong()
//            val host = SipAccountData.getHost()
//            if (!(isSuccess || (isIdentityMismatch && com.synapes.g9_voip.SipTlsUtils.isWildcardValid(
//                    getCertNames(prm) as ArrayList<*>,
//                    host.toString()
//                )))
//            ) {
//                Log.e(TAG, "The Sip Certificate is not valid")
//                BroadcastEventEmitter.notifyTlsVerifyStatusFailed()
//                Service.stopSelf()
//            } else {
//                Log.i(TAG, "The Sip Certificate verification succeeded")
//            }
//        }
    }

    override fun onIpChangeProgress(prm: OnIpChangeProgressParam) {
        super.onIpChangeProgress(prm)
//        if (prm.status == pj_constants_.PJ_SUCCESS) {
//            hangupAllCalls()
//            BroadcastEventEmitter.callReconnectionState(CallReconnectionState.FAILED)
//            return
//        }

//        if (prm.op == pjsua_ip_change_op.PJSUA_IP_CHANGE_OP_COMPLETED) {
//            BroadcastEventEmitter.callReconnectionState(CallReconnectionState.SUCCESS)
//        }
    }

    private fun getCertNames(prm: OnTransportStateParam): ArrayList<String?> {
        val certNames = ArrayList<String?>()
        certNames.add(prm.getTlsInfo().getRemoteCertInfo().subjectCn)
        for (name in prm.getTlsInfo().getRemoteCertInfo().getSubjectAltName()) {
            certNames.add(name.name)
        }
        return certNames
    }

    companion object {
        private val TAG: String = SipEndpoint::class.java.getSimpleName()
    }
}




