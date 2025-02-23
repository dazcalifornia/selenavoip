package com.synapes.selenvoip

import android.content.Intent
import android.media.AudioAttributes
import android.net.Uri
import org.pjsip.pjsua2.AudDevManager
import org.pjsip.pjsua2.*
import android.os.IBinder
import android.util.Log
import android.view.Surface
import com.synapes.selenvoip.SipServiceCommand.AGENT_NAME
import kotlinx.coroutines.Runnable
import org.pjsip.pjsua2.CallOpParam
import org.pjsip.pjsua2.CodecInfo
import org.pjsip.pjsua2.CodecInfoVector2
import org.pjsip.pjsua2.Endpoint
import org.pjsip.pjsua2.VidDevManager
import org.pjsip.pjsua2.EpConfig
import org.pjsip.pjsua2.TransportConfig
import org.pjsip.pjsua2.CallVidSetStreamParam
import org.pjsip.pjsua2.IpChangeParam
import org.pjsip.pjsua2.pjmedia_orient
import org.pjsip.pjsua2.pj_qos_type
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_transport_type_e
import org.pjsip.pjsua2.pjsua_call_vid_strm_op
import org.pjsip.pjsua2.pjsua_destroy_flag
import java.lang.RuntimeException
import java.util.concurrent.ConcurrentHashMap
import kotlin.jvm.java

open class SipService : SelenBackgroundService() {
    private lateinit var mConfiguredAccounts: MutableList<SipAccountData>
    private var mConfiguredGuestAccount: SipAccountData? = null
    private var mBroadcastEmitter: BroadcastEventEmitter? = null
    private var mEndpoint: SipEndpoint? = null

    fun getSharedPreferencesHelper(): SharedPrefsProvider {
        return mSharedPreferencesHelper
    }

    private lateinit var mSharedPreferencesHelper: SharedPrefsProvider

    @Volatile
    private var mStarted = false
    private var callStatus = 0

    /***   Service Lifecycle Callbacks     */
    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()

        enqueueJob(Runnable {
            Log.d(
                TAG,
                "Enqueuing job --> Creating SipService with priority: " + Thread.currentThread().priority
            )
            loadNativeLibraries()
            mSharedPreferencesHelper = SharedPrefsProvider.getInstance(applicationContext)
            mBroadcastEmitter = BroadcastEventEmitter(applicationContext)
            loadConfiguredAccounts()
            addAllConfiguredAccounts()
            Log.d(TAG, "Libs loaded --> SipService successfully created!")
        })
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        enqueueJob(Runnable {
            if (intent == null) return@Runnable
            val action = intent.action
            if (action == null) return@Runnable

            Log.d(TAG, "Starting SipService with action: $action")
            Log.d("burb", "onStartCommand received action: $action")

            when (action) {
                SipServiceConstants.Companion.ACTION_SET_ACCOUNT -> handleSetAccount(intent)
                SipServiceConstants.Companion.ACTION_REMOVE_ACCOUNT -> handleRemoveAccount(intent)
                SipServiceConstants.Companion.ACTION_RESTART_SIP_STACK -> handleRestartSipStack()
                SipServiceConstants.Companion.ACTION_MAKE_CALL -> handleMakeCall(intent)
                SipServiceConstants.Companion.ACTION_HANG_UP_CALL -> handleHangUpCall(intent)
                SipServiceConstants.Companion.ACTION_HANG_UP_CALLS -> handleHangUpActiveCalls(intent)
                SipServiceConstants.Companion.ACTION_HOLD_CALLS -> handleHoldActiveCalls(intent)
                SipServiceConstants.Companion.ACTION_GET_CALL_STATUS -> handleGetCallStatus(intent)
                SipServiceConstants.Companion.ACTION_SEND_DTMF -> handleSendDTMF(intent)
                SipServiceConstants.Companion.ACTION_ACCEPT_INCOMING_CALL -> handleAcceptIncomingCall(
                    intent
                )

                SipServiceConstants.Companion.ACTION_DECLINE_INCOMING_CALL -> handleDeclineIncomingCall(
                    intent
                )

                SipServiceConstants.Companion.ACTION_SET_HOLD -> handleSetCallHold(intent)
                SipServiceConstants.Companion.ACTION_TOGGLE_HOLD -> handleToggleCallHold(intent)
                SipServiceConstants.Companion.ACTION_SET_MUTE -> handleSetCallMute(intent)
                SipServiceConstants.Companion.ACTION_TOGGLE_MUTE -> handleToggleCallMute(intent)
                SipServiceConstants.Companion.ACTION_TRANSFER_CALL -> handleTransferCall(intent)
                SipServiceConstants.Companion.ACTION_ATTENDED_TRANSFER_CALL -> handleAttendedTransferCall(
                    intent
                )

                SipServiceConstants.Companion.ACTION_GET_CODEC_PRIORITIES -> handleGetCodecPriorities()
                SipServiceConstants.Companion.ACTION_SET_CODEC_PRIORITIES -> handleSetCodecPriorities(
                    intent
                )

                SipServiceConstants.Companion.ACTION_GET_REGISTRATION_STATUS -> handleGetRegistrationStatus(
                    intent
                )

                SipServiceConstants.Companion.ACTION_REFRESH_REGISTRATION -> handleRefreshRegistration(
                    intent
                )

                SipServiceConstants.Companion.ACTION_SET_DND -> handleSetDND(intent)
                SipServiceConstants.Companion.ACTION_SET_INCOMING_VIDEO -> handleSetIncomingVideoFeed(
                    intent
                )

                SipServiceConstants.Companion.ACTION_SET_SELF_VIDEO_ORIENTATION -> handleSetSelfVideoOrientation(
                    intent
                )

                SipServiceConstants.Companion.ACTION_SET_VIDEO_MUTE -> handleSetVideoMute(intent)
                SipServiceConstants.Companion.ACTION_START_VIDEO_PREVIEW -> handleStartVideoPreview(
                    intent
                )

                SipServiceConstants.Companion.ACTION_STOP_VIDEO_PREVIEW -> handleStopVideoPreview(
                    intent
                )

                SipServiceConstants.Companion.ACTION_SWITCH_VIDEO_CAPTURE_DEVICE -> handleSwitchVideoCaptureDevice(
                    intent
                )

                SipServiceConstants.Companion.ACTION_MAKE_DIRECT_CALL -> handleMakeDirectCall(intent)
                SipServiceConstants.Companion.ACTION_RECONNECT_CALL -> handleReconnectCall()
                SipServiceConstants.Companion.ACTION_MAKE_SILENT_CALL -> handleMakeSilentCall(intent)
                else -> {}
            }
            // TODO: Remove this line after testing
            if (mConfiguredAccounts.isEmpty() && mConfiguredGuestAccount == null) {
                Log.d(TAG, "No more configured accounts. Shutting down service")
                stopSelf()
            }
        })
        return START_NOT_STICKY
    }

    override fun onDestroy() {
        enqueueJob(Runnable {
            Log.d(TAG, "Destroying SipService")
            stopStack()
        })
        super.onDestroy()
    }

    companion object {
        private val TAG: String = SipService::class.java.getSimpleName()
        private val mActiveSipAccounts = ConcurrentHashMap<String, SipAccount?>()
        fun getActiveSipAccounts(): ConcurrentHashMap<String, SipAccount?> {
            return mActiveSipAccounts
        }
    }

    /****************************************************************/
    /***   Sip Stack Management     */
    /****************************************************************/
    private fun loadNativeLibraries() {
        try {
            System.loadLibrary("c++_shared")
            Log.d(TAG, "libc++_shared loaded")
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Error while loading libc++_shared native library", error)
            throw RuntimeException(error)
        }

        try {
            System.loadLibrary("openh264")
            Log.d(TAG, "OpenH264 loaded")
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Error while loading OpenH264 native library", error)
            throw RuntimeException(error)
        }

        try {
            System.loadLibrary("pjsua2")
            Log.d(TAG, "PJSIP pjsua2 loaded")
        } catch (error: UnsatisfiedLinkError) {
            Log.e(TAG, "Error while loading PJSIP pjsua2 native library", error)
            throw RuntimeException(error)
        }
    }

    /****************************************************************/
    /***   Sip Stack     */
    /****************************************************************/
    private fun startStack() {
        if (mStarted) {
            Log.i(TAG, "SipService already started")
            return
        }
        try {
            Log.d(TAG, "StartStack() --> Starting PJSIP...")

            mEndpoint = SipEndpoint(this)
            mEndpoint?.libCreate()

            val epConfig: EpConfig = EpConfig()
            epConfig.getUaConfig().userAgent = AGENT_NAME
            epConfig.getMedConfig().hasIoqueue = true

            // ✅ Fix: Use 8000 Hz clock rate for better compatibility
            epConfig.getMedConfig().apply {
                clockRate = 16000  // Support wider range
                quality = 8        // Balanced quality
                ecOptions = 1
                ecTailLen = 200
                threadCnt = 2
                hasIoqueue = true
            }
            SipServiceUtils.setSipLogger(epConfig)
            mEndpoint?.libInit(epConfig)

            val udpTransport: TransportConfig = TransportConfig()
            val tcpTransport: TransportConfig = TransportConfig()
            val tlsTransport: TransportConfig = TransportConfig()

            // ✅ Fix: Try removing QoS settings
            // udpTransport.qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
            // tcpTransport.qosType = pj_qos_type.PJ_QOS_TYPE_VOICE
            // tlsTransport.qosType = pj_qos_type.PJ_QOS_TYPE_VOICE

            mEndpoint?.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_UDP, udpTransport)
            // mEndpoint?.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TCP, tcpTransport)

            // ✅ Fix: Enable TLS if the SIP server requires it
            // mEndpoint?.transportCreate(pjsip_transport_type_e.PJSIP_TRANSPORT_TLS, tlsTransport)

            mEndpoint?.libStart()

            // ✅ Fix: Ensure codec priorities are set
            val codecPriorities = getConfiguredCodecPriorities()
            Log.d(TAG, "Configured Codec Priorities: $codecPriorities")

            if (codecPriorities != null) {
                if (codecPriorities.isNotEmpty()) {
                    SipServiceUtils.setAudioCodecPriorities(codecPriorities, mEndpoint!!)
                    SipServiceUtils.setVideoCodecPriorities(mEndpoint!!)
                } else {
                    Log.e(TAG, "No codec priorities found! This might cause call failures.")
                }
            }

            Log.d(TAG, "PJSIP started!")
            Log.d("burb", "PJSIP Stack Started: $mStarted")

            mStarted = true
            mBroadcastEmitter?.stackStatus(true)

        } catch (e: Exception) {
            Log.e(TAG, "Error while starting PJSIP", e)
        }
    }


    private fun handleRestartSipStack() {
        Log.d(TAG, "Restarting SIP stack")
        stopStack()
        addAllConfiguredAccounts()
    }


    /**
     * Shuts down PJSIP Stack
     */
    private fun stopStack() {
        if (!mStarted) {
            Log.e(TAG, "SipService not started")
            return
        }

        try {
            Log.d(TAG, "Stopping PJSIP")

            /*
             * Do not remove accounts on service stop anymore
             * They should have already been removed (unregistered)
             * In case they have not, it is ok, it means app has been just killed
             * or service force stopped
             *
             * *************************************
             * removeAllActiveAccounts();
             * *************************************
             */

            /* Try to force GC to do its job before destroying the library
             * since it's recommended to do that by PJSUA examples
             */
            Runtime.getRuntime().gc()

            mEndpoint?.libDestroy(pjsua_destroy_flag.PJSUA_DESTROY_NO_NETWORK.toLong())
            mEndpoint?.delete()
            mEndpoint = null

            Log.d(TAG, "PJSIP stopped")
            mBroadcastEmitter?.stackStatus(false)
        } catch (exc: Exception) {
            Log.e(TAG, "Error while stopping PJSIP", exc)
        } finally {
            mStarted = false
            mEndpoint = null
        }
    }


    /****************************************************************/
    /***   Sip Account Management     */
    /****************************************************************/
    private fun handleRefreshRegistration(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val regExpTimeout =
            intent.getIntExtra(SipServiceConstants.Companion.PARAM_REG_EXP_TIMEOUT, 0)
        val regContactParams =
            intent.getStringExtra(SipServiceConstants.Companion.PARAM_REG_CONTACT_PARAMS)
        var refresh = true
        if (!mActiveSipAccounts.isEmpty() && mActiveSipAccounts.containsKey(accountID)) {
            try {
                val sipAccount = mActiveSipAccounts.get(accountID)
                if (sipAccount == null) return

                if (regExpTimeout != 0 && regExpTimeout != sipAccount.getData()
                        .getRegExpirationTimeout()
                ) {
                    sipAccount.getData().setRegExpirationTimeout(regExpTimeout)
                    Log.d(TAG, regExpTimeout.toString())
                    refresh = false
                }
                if (regContactParams != null && regContactParams != sipAccount.getData()
                        .getContactUriParams()
                ) {
                    Log.d(TAG, regContactParams.toString())
                    sipAccount.getData().setContactUriParams(regContactParams)
                    refresh = false
                    mActiveSipAccounts.put(accountID!!, sipAccount)
                    mConfiguredAccounts.clear()
                    mConfiguredAccounts.add(sipAccount.getData())
                    persistConfiguredAccounts()
                }
                if (refresh) {
                    sipAccount.setRegistration(true)
                } else {
                    sipAccount.modify(sipAccount.getData().getAccountConfig())
                    sipAccount.getData().setRegExpirationTimeout(100)
                }
            } catch (ex: Exception) {
                Log.e(TAG, "Error while refreshing registration")
                ex.printStackTrace()
            }
        } else {
            Log.d(
                TAG,
                "AccountID: $accountID not set"
            )
        }
    }

    private fun handleSetAccount(intent: Intent) {
        val data =
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
                intent.getParcelableExtra(
                    SipServiceConstants.Companion.PARAM_ACCOUNT_DATA,
                    SipAccountData::class.java
                )
            } else {
                @Suppress("DEPRECATION")
                intent.getParcelableExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_DATA) as? SipAccountData
            }
        if (data == null) {
            Log.e(TAG, "No account data provided")
            return
        } else {
            Log.d(
                TAG,
                "Account data provided --> Username: ${data.username}, Password: ${data.password}, host ${data.host}, port ${data.port}"
            )
        }

        val index = mConfiguredAccounts.indexOf(data)
        Log.d(TAG, "Index: $index")
        if (index == -1) {
            Log.d(TAG, "No sip account found in preconfigured accounts")
            handleResetAccounts()
            Log.d(TAG, "Adding: ${data.getIdUri()}")
            try {
                handleSetCodecPriorities(intent)
                addAccount(data)
                mConfiguredAccounts.add(data)
                persistConfiguredAccounts()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while adding ${data.getIdUri()}: Exception: ${e.message}"
                )
            }
        } else {
            Log.d(TAG, "Reconfiguring account - ${data.getIdUri()}")

            try {
                removeAccount(data.getIdUri());
                handleSetCodecPriorities(intent)
                addAccount(data)
                mConfiguredAccounts[index] = data
                persistConfiguredAccounts()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while reconfiguring ${data.getIdUri()}: Exception: ${e.message}"
                )

            }
        }
    }

    private fun handleRemoveAccount(intent: Intent) {
        val accountIDtoRemove =
            intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)

        Log.d(TAG, "Removing account ${accountIDtoRemove.toString()}")

        val iterator = mConfiguredAccounts.iterator()

        while (iterator.hasNext()) {
            val data = iterator.next()

            if (data.getIdUri() == accountIDtoRemove) {
                try {
                    removeAccount(accountIDtoRemove)
                    iterator.remove()
                    persistConfiguredAccounts()
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error while removing account $accountIDtoRemove - ${e.message}"
                    )
                }
                break
            }
        }
    }

    private fun handleSetDND(intent: Intent) {
        val dnd = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_DND, false)
        mSharedPreferencesHelper.setDND(dnd)
    }


    /****************************************************************/
    /***   Sip Call Management     */
    /****************************************************************/
    private fun handleGetRegistrationStatus(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)

        if (!mStarted || mActiveSipAccounts[accountID] == null) {
            mBroadcastEmitter!!.registrationState("", 400)
            return
        }

        try {
            val account = mActiveSipAccounts[accountID]
            mBroadcastEmitter!!.registrationState(accountID, account!!.info.regStatus)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error while getting registration status for - $accountID - ${e.message}"
            )
        }
    }

    fun setLastCallStatus(callStatus: Int) {
        this.callStatus = callStatus
    }

    private fun handleResetAccounts() {
        Log.d(TAG, "Removing all the configured accounts")

        val iterator = mConfiguredAccounts.iterator()

        while (iterator.hasNext()) {
            val data = iterator.next()

            try {
                removeAccount(data.getIdUri())
                iterator.remove()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while removing account ${data.getIdUri()} - ${e.message}"
                )
            }
        }
    }

    /**
     * Removes a SIP Account and performs un-registration.
     */
    private fun removeAccount(accountID: String) {
        val account = mActiveSipAccounts.remove(accountID)

        if (account == null) {
            Log.e(TAG, "No account for ID: $accountID")
            return
        }

        Log.d(TAG, "Removing SIP account:  $accountID")
        account.delete()
        Log.d(
            TAG,
            "SIP account: $accountID successfully removed"
        )
    }

    fun removeGuestAccount() {
        removeAccount(mConfiguredGuestAccount!!.getIdUri())
        mConfiguredGuestAccount = null
    }

    fun getBroadcastEmitter(): BroadcastEventEmitter? {
        return mBroadcastEmitter
    }

    private fun loadConfiguredAccounts() {
        try {
            mConfiguredAccounts = mSharedPreferencesHelper.retrieveConfiguredAccounts()
            if (mConfiguredAccounts.isEmpty()) {
                Log.w(TAG, "Retrieved configured accounts list is null, initializing empty list")
                mConfiguredAccounts = mutableListOf()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error loading configured accounts", e)
            mConfiguredAccounts = mutableListOf()
        }

        // TODO: get from shared prefs
        // This line is kept as per the original code, but you might want to remove or modify it
        // based on your actual requirements
        mConfiguredAccounts = arrayListOf()
    }

    private fun addAllConfiguredAccounts() {
        if (!mConfiguredAccounts.isEmpty()) {
            for (accountData in mConfiguredAccounts) {
                try {
                    addAccount(accountData)
                } catch (e: Exception) {
                    Log.e(
                        TAG,
                        "Error while adding ${accountData.getAccountConfig()} - ${e.message}"

                    )
                }
            }
        }
    }

    /**
     * Adds a new SIP Account and performs initial registration.
     * @param account SIP account to add
     */
    @Throws(Exception::class)
    private fun addAccount(account: SipAccountData) {
        val accountString = account.getIdUri()

        Log.d(TAG, "Adding SIP account: $accountString")

        val sipAccount = mActiveSipAccounts[accountString]

        if (sipAccount == null || !sipAccount.isValid || account != sipAccount.getData()) {
            if (mActiveSipAccounts.containsKey(accountString) && sipAccount != null) {
                sipAccount.delete()
            }
            startStack()
            val pjSipAndroidAccount = SipAccount(this, account)
            try {
                pjSipAndroidAccount.create()
                if (pjSipAndroidAccount.isValid) {
                    mActiveSipAccounts.put(accountString, pjSipAndroidAccount)
                    Log.d(TAG, "SIP account $accountString successfully created and added")
                } else {
                    Log.e(TAG, "Failed to create SIP account $accountString: Account is not valid")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error creating SIP account $accountString", e)
                throw e // Re-throw the exception to be handled by the caller
            }
            Log.d(
                TAG,
                "Finishing added SIP account ${account.getIdUri()}..."
            )
        } else {
            sipAccount.setRegistration(true)
        }
    }

    private fun getCodecPriorityList(): ArrayList<CodecPriority?>? {
        startStack()

        if (!mStarted) {
            Log.e(
                TAG, "Can't get codec priority list! The SIP Stack has not been " +
                        "initialized! Add an account first!"
            )
            return null
        }

        try {
            val codecs: CodecInfoVector2? = mEndpoint?.codecEnum2()
            if (codecs == null || codecs.size == 0) return null

            val codecPrioritiesList: ArrayList<CodecPriority?> =
                ArrayList<CodecPriority?>(codecs.size)

            for (i in codecs.indices) {
                val codecInfo: CodecInfo = codecs[i]
                val newCodec = CodecPriority(
                    codecInfo.codecId,
                    codecInfo.priority
                )
                if (!codecPrioritiesList.contains(newCodec)) codecPrioritiesList.add(newCodec)
                codecInfo.delete()
            }

            codecs.delete()

            codecPrioritiesList.sortWith(Comparator { a, b ->
                when {
                    a == null && b == null -> 0
                    a == null -> -1
                    b == null -> 1
                    else -> a.getPriority().compareTo(b.getPriority())
                }
            })

            return codecPrioritiesList
        } catch (exc: Exception) {
            Log.e(TAG, "Error while getting codec priority list!", exc)
            return null
        }
    }

    private fun handleGetCodecPriorities() {
        val codecs = getCodecPriorityList()

        if (codecs != null) {
            mBroadcastEmitter?.codecPriorities(codecs)
        }
    }


    private fun handleSetCodecPriorities(intent: Intent) {
        Log.d(TAG, " ---> handleSetCodecPriorities")

        val codecPriorities = listOf(
            CodecPriority("PCMU/8000", 255),  // Most compatible codec first
            CodecPriority("PCMA/8000", 254),
            CodecPriority("opus/48000", 253), // Add Opus support
            CodecPriority("G722/16000", 252),
            CodecPriority("iLBC/8000", 251)
        )

        Log.d(TAG, "🚀 Manually setting codec priorities: $codecPriorities")

        codecPriorities.forEach { codecPriority ->
            try {
                mEndpoint?.codecSetPriority(codecPriority.getCodecId(), codecPriority.getPriority().toShort())
                Log.d(TAG, "✅ Successfully set codec: ${codecPriority.getCodecId()} to priority ${codecPriority.getPriority()}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to set codec priority: ${codecPriority.getCodecId()}", e)
            }
        }
    }



    private fun persistConfiguredAccounts() {
        Log.d(TAG, " ----> Persisting configured accounts")
        mSharedPreferencesHelper.persistConfiguredAccounts(mConfiguredAccounts)

        // Log out the persisted accounts
        Log.d(TAG, "Persisted accounts:")
        mConfiguredAccounts.forEachIndexed { index, account ->
            Log.d(TAG, "Account $index: ${account.getIdUri()}")
        }

        if (mConfiguredAccounts.isEmpty()) {
            Log.d(TAG, "No accounts persisted")
        }
    }

    private fun persistConfiguredCodecPriorities(codecPriorities: ArrayList<CodecPriority?>) {
        Log.d(TAG, "Persisting configured codec priorities")
        mSharedPreferencesHelper.persistConfiguredCodecPriorities(codecPriorities)

        // Log each codec priority
        codecPriorities.forEachIndexed { index, codecPriority ->
            codecPriority?.let {
                Log.d(
                    TAG,
                    "Codec Priority $index: ID=${it.getCodecId()}, Priority=${it.getPriority()}"
                )
            } ?: Log.d(TAG, "Codec Priority $index: null")
        }

        Log.d(TAG, "Finished persisting ${codecPriorities.size} codec priorities")
    }

    private fun getConfiguredCodecPriorities(): ArrayList<CodecPriority>? {
        Log.d(TAG, " ---> getConfiguredCodecPriorities")
        val codecPriorities = mSharedPreferencesHelper.retrieveConfiguredCodecPriorities()
        codecPriorities?.forEach { codecPriority ->
            Log.d(
                TAG,
                "Codec Priority - Key: ${codecPriority.getCodecId()}, Value: ${codecPriority.getPriority()}"
            )
        }
        return codecPriorities
    }

//    private fun handleMakeCall(intent: Intent) {
//        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
//        val number = intent.getStringExtra(SipServiceConstants.Companion.PARAM_NUMBER)
//
//        // Add debug log for incoming number
//        Log.d(TAG, "Incoming number before formatting: $number")
//
//        // Strip any existing sip: prefix if present
//        val cleanNumber = number?.removePrefix("sip:")?.split("@")?.first() ?: ""
//
//        // Always format with full SIP URI
//        val formattedNumber = "sip:${cleanNumber}@synapes-pbx-poc-01.online"
//
//        Log.d(TAG, "Formatted destination number: $formattedNumber")
//
//        val isVideo = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, false)
//        var isVideoConference = true
//        var isTransfer = false
//
//        if (isVideo) {
//            isVideoConference = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO_CONF, false)
//        } else {
//            isTransfer = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_TRANSFER, false)
//        }
//
//        Log.d("burb", "Available accounts: ${mActiveSipAccounts.keys}")
//
//
//        try {
//            if (!mActiveSipAccounts.containsKey(accountID)) {
//                Log.e(TAG, "Account $accountID not found in active accounts")
//                mBroadcastEmitter?.outgoingCall(accountID, -1, formattedNumber, false, false, false)
//                return
//            }
//
//            Log.d("burb", "handleMakeCall: Making a call to $number")
//
//
//            val call = mActiveSipAccounts[accountID]!!.addOutgoingCall(formattedNumber, isVideo, isVideoConference, isTransfer)
//            call!!.setVideoParams(isVideo, isVideoConference)
//
//            Log.d(TAG, """
//            Call Details:
//            - Account ID: $accountID
//            - Call ID: ${call.id}
//            - Destination: $formattedNumber
//            - Video: $isVideo
//            - Video Conference: $isVideoConference
//            - Transfer: $isTransfer
//        """.trimIndent())
//
//            mBroadcastEmitter?.outgoingCall(
//                accountID,
//                call.id,
//                formattedNumber,
//                isVideo,
//                isVideoConference,
//                isTransfer
//            )
//        } catch (exc: Exception) {
//            Log.e(TAG, "Error while making outgoing call", exc)
//            Log.e(TAG, "Exception details: ${exc.message}")
//            Log.e(TAG, "Stack trace: ${exc.stackTrace.joinToString("\n")}")
//            mBroadcastEmitter?.outgoingCall(accountID, -1, formattedNumber, false, false, false)
//        }
//    }

    private fun handleMakeCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID)
        val number = intent.getStringExtra(SipServiceConstants.PARAM_NUMBER)

        Log.d(TAG, "Incoming number before formatting: $number")

        // Format the number as before
        val cleanNumber = number?.removePrefix("sip:")?.split("@")?.first() ?: ""
        val formattedNumber = "sip:${cleanNumber}@synapes-pbx-poc-01.online"

        Log.d(TAG, "cleanNumber destination number: $cleanNumber")
        Log.d(TAG, "Formatted destination number: $formattedNumber")

        val isVideo = intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false)
        val isVideoConference = if (isVideo) {
            Log.i(TAG, "VIDEO")
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO_CONF, false)
        } else false
        val isTransfer = if (!isVideo) {
            Log.i(TAG, "Not video")
            intent.getBooleanExtra(SipServiceConstants.PARAM_IS_TRANSFER, false)
        } else false

        try {
            // Check if account exists
            if (!mActiveSipAccounts.containsKey(accountID)) {
                Log.e(TAG, "Account $accountID not found in active accounts")
                mBroadcastEmitter?.outgoingCall(accountID, -1, formattedNumber, false, false, false)
                return
            }


            val account = mActiveSipAccounts[accountID]!!
            Log.d(TAG,"account:${account}")

            // Add to your call setup
            val audioAttributes = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build()

            // Create direct PJSIP call
            val call = createPJSIPCall(account, formattedNumber, isVideo, isVideoConference)

            if (call != null) {
                Log.d(TAG,"Call is not null ")
                // Store call in your existing management system
//                account.addOutgoingCall(formattedNumber, isVideo, isVideoConference, isTransfer)

                // Set video parameters if needed
                call.setVideoParams(isVideo, isVideoConference)

                // Broadcast the outgoing call event
                mBroadcastEmitter?.outgoingCall(
                    accountID,
                    call.id,
                    formattedNumber,
                    isVideo,
                    isVideoConference,
                    isTransfer
                )

                // Start CallActivity
                if (accountID != null) {
                    startCallActivity(accountID, call.id, formattedNumber, isVideo, isVideoConference)
                }
            } else {
                throw Exception("Failed to create PJSIP call")
            }

        } catch (exc: Exception) {
            Log.e(TAG, "Error while making outgoing call", exc)
            Log.e(TAG, "Exception details: ${exc.message}")
            Log.e(TAG, "Stack trace: ${exc.stackTrace.joinToString("\n")}")
            mBroadcastEmitter?.outgoingCall(accountID, -1, formattedNumber, false, false, false)
        }
    }

    // New function to create PJSIP call
    private fun createPJSIPCall(
        account: SipAccount,
        destination: String,
        isVideo: Boolean,
        isVideoConference: Boolean
    ): MyCall? {
        try {
            // Create new PJSIP call instance
            Log.i(TAG, "acc: ${account} , dest: ${destination}")
            val call = MyCall(account, -1)

            // Set up call parameters
            val callParams = CallOpParam(true)

            Log.d(TAG,"dest: ${destination}, callParams: ${callParams}")
            // Make the actual PJSIP call
            call.makeCall(destination, callParams)

            return call
        } catch (e: Exception) {
            Log.e(TAG, "Error creating PJSIP call", e)
            return null
        }
    }

    // Custom Call class to handle PJSIP call states
    class MyCall(acc: Account?, callId: Int) : Call(acc, callId) {
        private var videoParams: Pair<Boolean, Boolean>? = null

        fun setVideoParams(isVideo: Boolean, isVideoConference: Boolean) {
            videoParams = Pair(isVideo, isVideoConference)
        }

        override fun onCallState(prm: OnCallStateParam) {
            try {
                val callInfo = info
                when (callInfo.state) {
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                        Log.i(TAG, "Call disconnected: ${callInfo.lastStatusCode}")
                        // Handle call disconnection
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                        Log.i(TAG, "Call connected")
                        // Handle call connection
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_EARLY -> {
                        Log.i(TAG, "Call ringing")
                        // Handle call ringing state
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error in onCallState", e)
            }
        }
    }

    // Helper function to start CallActivity
    private fun startCallActivity(
        accountID: String,
        callID: Int,
        number: String,
        isVideo: Boolean,
        isVideoConference: Boolean
    ) {
        val intent = Intent(this, CallActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            putExtra("accountID", accountID)
            putExtra("callID", callID)
            putExtra("number", number)
            putExtra("isVideo", isVideo)
            putExtra("isVideoConference", isVideoConference)
            putExtra("type", CallActivity.TYPE_OUT_CALL)
        }
        startActivity(intent)
    }

    private fun handleMakeDirectCall(intent: Intent) {
        val bundle = intent.extras
        if (bundle == null) return
        val uri = bundle.getParcelable<Uri?>(SipServiceConstants.Companion.PARAM_DIRECT_CALL_URI)
        if (uri == null) return
        val sipServer =
            intent.getStringExtra(SipServiceConstants.Companion.PARAM_DIRECT_CALL_SIP_SERVER)
        val name = intent.getStringExtra(SipServiceConstants.Companion.PARAM_GUEST_NAME)
        val isVideo = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, false)
        var isVideoConference = false
        if (isVideo) {
            isVideoConference =
                intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO_CONF, false)
        }
        val transport = SipAccountTransport.getTransportByCode(
            intent.getIntExtra(SipServiceConstants.Companion.PARAM_DIRECT_CALL_TRANSPORT, 0)
        )

        Log.d(TAG, "Making call to ${uri.userInfo}")

        val accountID = "sip:" + name + "@" + uri.host
        val sipUri = "sip:" + uri.userInfo + "@" + uri.host

        try {
            startStack()
            val sipAccountData = SipAccountData()
                .setHost(if (sipServer != null) sipServer else uri.host)
                .setUsername(name)
                .setPort(if ((uri.port > 0)) uri.port else SipServiceConstants.Companion.DEFAULT_SIP_PORT)
                .setTransport(transport)
                .setRealm(uri.host)
            /* display name not yet implemented server side for direct calls */
            /* .setUsername("guest") */
            /* .setGuestDisplayName(name)*/
            val pjSipAndroidAccount = SipAccount(this, sipAccountData)
            pjSipAndroidAccount.createGuest()
            mConfiguredGuestAccount = pjSipAndroidAccount.getData()

            // Overwrite the old value if present
            mActiveSipAccounts.put(accountID, pjSipAndroidAccount)

            val call = mActiveSipAccounts.get(accountID)!!
                .addOutgoingCall(sipUri, isVideo, isVideoConference, false)
            if (call != null) {
                call.setVideoParams(isVideo, isVideoConference)
                mBroadcastEmitter?.outgoingCall(
                    accountID,
                    call.id,
                    uri.userInfo,
                    isVideo,
                    isVideoConference,
                    false
                )
            } else {
                Log.e(TAG, "Error while making a direct call as Guest")
                mBroadcastEmitter?.outgoingCall(
                    accountID,
                    -1,
                    uri.userInfo,
                    false,
                    false,
                    false
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while making a direct call as Guest. Exception: ${e.message}")
            mBroadcastEmitter?.outgoingCall(accountID, -1, uri.userInfo, false, false, false)
        }
    }

    private fun handleMakeSilentCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val number = intent.getStringExtra(SipServiceConstants.Companion.PARAM_NUMBER)

        Log.d(TAG, "Making silent call to extension# $number")

        try {
            mBroadcastEmitter?.silentCallStatus(
                mActiveSipAccounts[accountID]!!.addOutgoingCall(number!!) != null, number
            )
        } catch (exc: Exception) {
            mBroadcastEmitter?.silentCallStatus(false, number)
            Log.e(TAG, "Error while making silent call", exc)
        }
    }

    private fun handleReconnectCall() {
        try {
            getBroadcastEmitter()!!.callReconnectionState(CallReconnectionState.PROGRESS)
            mEndpoint?.handleIpChange(IpChangeParam())
            Log.i(TAG, "Call reconnection started")
        } catch (e: Exception) {
            Log.e(TAG, "Error while reconnecting the call. Exception: ${e.message}")
        }
    }

    private fun handleHangUpCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        try {
            hangupCall(accountID!!, callID)
        } catch (exc: Exception) {
            Log.e(TAG, "Error while hanging up call", exc)
            notifyCallDisconnected(accountID, callID)
        }
    }

    private fun handleGetCallStatus(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            var callStatusCode = callStatus
            try {
                callStatusCode = sipCall.info.lastStatusCode
            } catch (ex: Exception) {
                ex.printStackTrace()
            }

            mBroadcastEmitter?.callState(
                accountID,
                callID,
                sipCall.getCurrentState(),
                callStatusCode,
                sipCall.getConnectTimestamp()
            )
        }
    }

    private fun handleSendDTMF(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)
        val dtmf = intent.getStringExtra(SipServiceConstants.Companion.PARAM_DTMF)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            try {
                sipCall.dialDtmf(dtmf)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while dialing dtmf: $dtmf. AccountID: $accountID - CallID: $callID - Exception: ${e.message}"
                )
            }
        }
    }

    private fun handleAcceptIncomingCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val isVideo =
                intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_IS_VIDEO, false)
            try {
                sipCall.setVideoParams(isVideo, false)
                sipCall.acceptIncomingCall()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while accepting incoming call. AccountID: $accountID, CallID: $callID, Exception: ${e.message}"
                )
            }
        }
    }

    private fun handleDeclineIncomingCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            try {
                sipCall.declineIncomingCall()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while declining incoming call. AccountID: $accountID, CallID: $callID. Exception: ${e.message}"
                )
            }
        }
    }


    private fun handleHangUpActiveCalls(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)

        val account = mActiveSipAccounts[accountID]
        if (account == null) return

        val activeCallIDs = account.getCallIDs()

        if (activeCallIDs == null || activeCallIDs.isEmpty()) return

        for (callID in activeCallIDs) {
            try {
                hangupCall(accountID!!, callID!!)
            } catch (exc: Exception) {
                Log.e(TAG, "Error while hanging up call", exc)
                notifyCallDisconnected(accountID, callID!!)
            }
        }
    }

    private fun handleSetCallHold(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val hold = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_HOLD, false)
            try {
                sipCall.setHold(hold)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while setting hold. AccountID: $accountID, CallID: $callID. Exception: ${e.message}"
                )
            }
        }
    }


    private fun handleToggleCallHold(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            try {
                sipCall.toggleHold()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while toggling hold. AccountID: $accountID, CallID: $callID. Exception: ${e.message}"
                )
            }
        }
    }


    private fun handleHoldActiveCalls(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)

        val account = mActiveSipAccounts.get(accountID)
        if (account == null) return

        val activeCallIDs = account.getCallIDs()

        if (activeCallIDs == null || activeCallIDs.isEmpty()) return

        for (callID in activeCallIDs) {
            try {
                val sipCall = getCall(accountID!!, callID!!)
                sipCall?.setHold(true)
            } catch (exc: Exception) {
                Log.e(TAG, "Error while holding call", exc)
            }
        }
    }

    private fun handleSetCallMute(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val mute = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_MUTE, false)
            try {
                sipCall.setMute(mute)
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while setting mute. AccountID: $accountID, CallID: $callID, Exception: ${e.message}"
                )
            }
        }
    }

    private fun handleToggleCallMute(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            try {
                sipCall.toggleMute()
            } catch (e: Exception) {
                Log.e(
                    TAG,
                    "Error while toggling mute. AccountID: $accountID, CallID: $callID, Exception: ${e.message}"
                )
            }
        }
    }

    private fun handleTransferCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)
        val number = intent.getStringExtra(SipServiceConstants.Companion.PARAM_NUMBER)

        try {
            val sipCall = getCall(accountID!!, callID)
            sipCall?.transferTo(number!!)
        } catch (e: Exception) {
            Log.e(
                TAG,
                "Error while transferring call to extension# $number, Exception: ${e.message}"
            )
            notifyCallDisconnected(accountID, callID)
        }
    }

    private fun handleAttendedTransferCall(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callIdOrig = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        try {
            val sipCallOrig = getCall(accountID!!, callIdOrig)
            if (sipCallOrig != null) {
                val callIdDest =
                    intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID_DEST, 0)
                val sipCallDest = getCall(accountID, callIdDest)
                sipCallOrig.xferReplaces(sipCallDest, CallOpParam())
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error while finalizing attended transfer", e)
            notifyCallDisconnected(accountID, callIdOrig)
        }
    }

    private fun handleSetIncomingVideoFeed(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val bundle = intent.getExtras()
            if (bundle != null) {
                val surface =
                    bundle.getParcelable<Surface?>(SipServiceConstants.Companion.PARAM_SURFACE)
                sipCall.setIncomingVideoFeed(surface)
            }
        }
    }

    private fun handleSetSelfVideoOrientation(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipAccount = mActiveSipAccounts.get(accountID)
        if (sipAccount != null) {
            val sipCall = getCall(accountID!!, callID)
            if (sipCall != null) {
                val orientation =
                    intent.getIntExtra(SipServiceConstants.Companion.PARAM_ORIENTATION, -1)
                setSelfVideoOrientation(sipCall, orientation)
            }
        }
    }

    fun setSelfVideoOrientation(sipCall: SipCall, orientation: Int) {
        try {

            var pjmediaOrientation: Int = when (orientation) {
                Surface.ROTATION_0 -> pjmedia_orient.PJMEDIA_ORIENT_ROTATE_270DEG

                Surface.ROTATION_90 -> pjmedia_orient.PJMEDIA_ORIENT_NATURAL
                Surface.ROTATION_180 -> pjmedia_orient.PJMEDIA_ORIENT_ROTATE_90DEG

                Surface.ROTATION_270 -> pjmedia_orient.PJMEDIA_ORIENT_ROTATE_180DEG

                else -> pjmedia_orient.PJMEDIA_ORIENT_UNKNOWN
            }

            if (pjmediaOrientation != pjmedia_orient.PJMEDIA_ORIENT_UNKNOWN)  // set orientation to the correct current device
                getVidDevManager().setCaptureOrient(
                    if (sipCall.isFrontCamera())
                        SipServiceConstants.Companion.FRONT_CAMERA_CAPTURE_DEVICE
                    else
                        SipServiceConstants.Companion.BACK_CAMERA_CAPTURE_DEVICE,
                    pjmediaOrientation, true
                )
        } catch (e: Exception) {
            Log.e(TAG, "Error while changing video orientation. Exception: ${e.message}")
        }
    }

    private fun handleSetVideoMute(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val mute = intent.getBooleanExtra(SipServiceConstants.Companion.PARAM_VIDEO_MUTE, false)
            sipCall.setVideoMute(mute)
        }
    }

    private fun handleStartVideoPreview(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            val bundle = intent.getExtras()
            if (bundle != null) {
                val surface = intent.getExtras()!!
                    .getParcelable<Surface?>(SipServiceConstants.Companion.PARAM_SURFACE)
                sipCall.startPreviewVideoFeed(surface)
            }
        }
    }

    private fun handleStopVideoPreview(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            sipCall.stopPreviewVideoFeed()
        }
    }

    // Switch Camera
    private fun handleSwitchVideoCaptureDevice(intent: Intent) {
        val accountID = intent.getStringExtra(SipServiceConstants.Companion.PARAM_ACCOUNT_ID)
        val callID = intent.getIntExtra(SipServiceConstants.Companion.PARAM_CALL_ID, 0)

        val sipCall = getCall(accountID!!, callID)
        if (sipCall != null) {
            try {
                val callVidSetStreamParam: CallVidSetStreamParam = CallVidSetStreamParam()
                callVidSetStreamParam.capDev = if (sipCall.isFrontCamera())
                    SipServiceConstants.Companion.BACK_CAMERA_CAPTURE_DEVICE
                else
                    SipServiceConstants.Companion.FRONT_CAMERA_CAPTURE_DEVICE
                sipCall.setFrontCamera(!sipCall.isFrontCamera())
                sipCall.vidSetStream(
                    pjsua_call_vid_strm_op.PJSUA_CALL_VID_STRM_CHANGE_CAP_DEV,
                    callVidSetStreamParam
                )
            } catch (ex: Exception) {
                Log.e(TAG, "Error while switching capture device", ex)
            }
        }
    }

    /****************************************************************/
    /***   Sip Call Utilities     */
    /****************************************************************/
    fun isDND(): Boolean {
        return mSharedPreferencesHelper.isDND()
    }

    private fun hangupCall(accountID: String, callID: Int) {
        val sipCall = getCall(accountID, callID)
        sipCall?.hangUp()
    }

    private fun getCall(accountID: String, callID: Int): SipCall? {
        val account = mActiveSipAccounts.get(accountID)

        if (account == null) return null
        val sipCall = account.getCall(callID)
        if (sipCall != null) {
            return sipCall
        } else {
            notifyCallDisconnected(accountID, callID)
            return null
        }
    }

    private fun notifyCallDisconnected(accountID: String?, callID: Int) {
        mBroadcastEmitter?.callState(
            accountID, callID,
            pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED,
            callStatus, 0
        )
    }

    @Synchronized
    fun getAudDevManager(): AudDevManager {
        return mEndpoint!!.audDevManager()
    }

    @Synchronized
    fun getVidDevManager(): VidDevManager {
        return mEndpoint!!.vidDevManager()
    }


}
