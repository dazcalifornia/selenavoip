package com.synapes.selenvoip.managers

import android.content.Context
import android.util.Log
import android.widget.Toast
import com.synapes.selenvoip.CallActivity
import com.synapes.selenvoip.SipAccountData
import com.synapes.selenvoip.SipAccountTransport
import com.synapes.selenvoip.SipServiceCommand

class SipManager(private val context: Context) {

    private var mAccount: SipAccountData? = null
    private var mAccountId: String? = null
    private var isRegistrationComplete = false

    companion object {
//        private const val DEFAULT_SERVER = "synapes-pbx-poc-01.online"
//        private const val DEFAULT_ACCOUNT = "933933"
//        private const val DEFAULT_PASSWORD = "933933"
//        private const val DEFAULT_PORT = "5060"
        private const val TAG = "SipManager"
    }

    init {
//        autoLogin()
    }

    fun login(server: String, username: String, password: String, port: String) {
        try {
            createSipAccount(server, username, password, port)
            registerSipAccount()
        } catch (e: Exception) {
            handleLoginError(e.message ?: "Unknown error during login")
        }
    }


    fun audioCall(callNumber: String) {
        try {
            if (mAccountId.isNullOrEmpty()) {
                Log.e(TAG, "Account ID is null or empty")
                return
            }

            val formattedNumber = if (callNumber.contains("@")) {
                callNumber
            } else {
                "sip:$callNumber@synapes-pbx-poc-01.online"  // Use correct server
            }

            SipServiceCommand.makeCall(context, mAccountId!!, formattedNumber, false)
        } catch (e: Exception) {
            Log.e(TAG, "Audio call error", e)
        }
    }


//    fun audioCall(callNumber: String) {
//        try {
//            // Check if Account ID is set before calling
//            if (mAccountId.isNullOrEmpty()) {
//                Log.e(TAG, "Account ID is null or empty. Cannot proceed with the call.")
//                Toast.makeText(context, "SIP account is not registered", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            // Check if SIP registration is complete
//            if (!isRegistrationComplete) {
//                Log.e(TAG, "SIP registration is incomplete. Call cannot proceed.")
//                Toast.makeText(context, "SIP account is not registered yet", Toast.LENGTH_SHORT).show()
//                return
//            }
//
//            val formattedNumber = if (callNumber.contains("@")) {
//                callNumber
//            } else {
//                val DEFAULT_SERVER = ""  // Empty server string!
//                "$callNumber@$DEFAULT_SERVER"
//            }
//
//            // Log details before making the call
//            Log.d(TAG, """
//                Audio Call Details:
//                - Account ID: $mAccountId
//                - Raw Number: $callNumber
//                - Formatted Number: $formattedNumber
//            """.trimIndent())
//
//            // Attempt to make the call
//            Log.d(TAG, "makeCall triggered for number: $formattedNumber on account: $mAccountId")
//            SipServiceCommand.makeCall(context, mAccountId!!, formattedNumber, false, false)
//            Log.d(TAG, "Call request sent successfully.")
//
//        } catch (e: Exception) {
//            Log.e(TAG, "Audio call error", e)
//            Log.e(TAG, "Stack trace: ${e.stackTrace.joinToString("\n")}")
//            Toast.makeText(context, "Error initiating call", Toast.LENGTH_SHORT).show()
//        }
//    }

    private fun createSipAccount(server: String, username: String, password: String, port: String) {
        mAccount = SipAccountData().apply {
            host = server
            realm = "*"
            this.port = port.toInt()
            this.username = username
            this.password = password
            transport = SipAccountTransport.UDP

            // Additional SIP account configuration
            setAuthenticationType(SipAccountData.AUTH_TYPE_DIGEST)
            setRealm(server)
//            setRegExpirationTimeout(300) // 5 minutes registration timeout
        }
    }

    private fun registerSipAccount() {
        try {
            val sipAccountData = mAccount!!
            mAccountId = SipServiceCommand.setAccount(context, sipAccountData)

            Log.d(TAG, """
                **** Account Info:
                Registrar URI: ${sipAccountData.getRegistrarUri()}
                Auth Credentials: ${sipAccountData.getAuthCredInfo()}
                Username: ${sipAccountData.username}
                Password: ${sipAccountData.password}
                **** SIP URI: ${sipAccountData.getIdUri()}
                **** Proxy URI: ${sipAccountData.getProxyUri()}
            """.trimIndent())

            isRegistrationComplete = true

        } catch (e: Exception) {
            handleLoginError(e.message ?: "Unknown error during registration")
            throw e
        }
    }
    private fun handleLoginError(errorMessage: String) {
        Log.e(TAG, "Login failed: $errorMessage")
        Toast.makeText(context, "Login failed: $errorMessage", Toast.LENGTH_SHORT).show()
    }

    fun handleIncomingCall(accountID: String, callID: Int, displayName: String, remoteUri: String, isVideo: Boolean) {
        try {
            Log.d(TAG, """
                Handling incoming call:
                Account ID: $accountID
                Call ID: $callID
                Display Name: $displayName
                Remote URI: $remoteUri
                Is Video: $isVideo
            """.trimIndent())

            // Accept the incoming call
            SipServiceCommand.acceptIncomingCall(context, accountID, callID, isVideo)

            // Start the call activity
            CallActivity.startActivityIn(
                context,
                accountID,
                callID,
                displayName,
                remoteUri,
                isVideo
            )
        } catch (e: Exception) {
            Log.e(TAG, "Error handling incoming call", e)
            Toast.makeText(context, "Error handling incoming call", Toast.LENGTH_SHORT).show()
        }
    }

    fun getRegistrationStatus(): Boolean = isRegistrationComplete

    fun getAccountId(): String? = mAccountId

    fun getSipAccount(): SipAccountData? = mAccount

    fun reRegister() {
        Log.d(TAG, "Attempting to re-register SIP account...")
        isRegistrationComplete = false
    }

    fun logout() {
        try {
            mAccountId?.let { accountId ->
                SipServiceCommand.removeAccount(context, accountId)
                mAccountId = null
                mAccount = null
                isRegistrationComplete = false
                Log.d(TAG, "SIP account logged out successfully")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
        }
    }
}