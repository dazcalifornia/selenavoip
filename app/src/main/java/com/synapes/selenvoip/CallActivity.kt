package com.synapes.selenvoip


import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.synapes.selenvoip.databinding.ActivityCallBinding
import org.pjsip.pjsua2.pjsip_inv_state
import org.pjsip.pjsua2.pjsip_status_code

class CallActivity : AppCompatActivity(), SurfaceHolder.Callback {
    private lateinit var binding: ActivityCallBinding

    private var mAccountID: String? = null
    private var mDisplayName: String? = null
    private var mRemoteUri: String? = null
    private var mCallID = 0
    private var mIsVideo = false
    private var mType = 0
    private var mNumber: String? = null
    private var mIsVideoConference = false
    private var micMute = false
    private lateinit var mContext: Context
    private lateinit var mReceiver: BroadcastEventReceiver

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        binding = ActivityCallBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initializeBroadcastReceiver()
        registerReceiver()
        initData()
        setupClickListeners()

    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag", "InlinedApi")
    private fun registerReceiver() {
        val intentFilter = IntentFilter().apply {
            // FIXME: only spec what this activity needs
            addAction("com.synapes.selenvoip.SELEN_VOIP_APP_START") // <!-- OLD -->
            addAction("com.synapes.selenvoip.SELEN_VOIP_REGISTRATION_STATE") // <!-- OLD -->
            addAction("com.synapes.selenvoip.SELEN_VOIP_CALL_STATE") // <!-- OLD -->
            addAction("com.synapes.selenvoip.SELEN_ALARM_BOX_CALL_HQ") // <!-- OLD -->
            addAction("com.synapes.selenvoip.LOCAL_CALL_HQ") // <!-- OLD -->
            addAction("com.synapes.selenvoip.CHECK_REGISTRATION") // <!-- OLD -->
            addAction("com.synapes.selenvoip.LOCAL_CHECK_REGISTRATION") // <!-- OLD -->
            addAction("com.synapes.selenvoip.SERVER_URL_CHANGED") // <!-- OLD -->
            addAction("com.synapes.selenvoip.REGISTRATION")
            addAction("com.synapes.selenvoip.INCOMING_CALL")
            addAction("com.synapes.selenvoip.CALL_STATE")
            addAction("com.synapes.selenvoip.CALL_MEDIA_STATE")
            addAction("com.synapes.selenvoip.OUTGOING_CALL")
            addAction("com.synapes.selenvoip.STACK_STATUS")
            addAction("com.synapes.selenvoip.CODEC_PRIORITIES")
            addAction("com.synapes.selenvoip.CODEC_PRIORITIES_SET_STATUS")
            addAction("com.synapes.selenvoip.MISSED_CALL")
            addAction("com.synapes.selenvoip.VIDEO_SIZE")
            addAction("com.synapes.selenvoip.CALL_STATS")
            addAction("com.synapes.selenvoip.CALL_RECONNECTION_STATE")
            addAction("com.synapes.selenvoip.SILENT_CALL_STATUS")
            addAction("com.synapes.selenvoip.NOTIFY_TLS_VERIFY_STATUS_FAILED")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            registerReceiver(mReceiver, intentFilter, RECEIVER_NOT_EXPORTED)
        } else {
            registerReceiver(mReceiver, intentFilter)
        }
    }

    private fun initData() {
        mAccountID = intent.getStringExtra("accountID")
        mCallID = intent.getIntExtra("callID", -1)
        mType = intent.getIntExtra("type", -1)
        mDisplayName = intent.getStringExtra("displayName")
        mRemoteUri = intent.getStringExtra("remoteUri")
        mNumber = intent.getStringExtra("number")
        mIsVideo = intent.getBooleanExtra("isVideo", false)
        mIsVideoConference = intent.getBooleanExtra("isVideoConference", false)

        showLayout(mType)

        binding.textViewPeer.text = String.format("%s\n%s", mRemoteUri, mDisplayName)
        binding.tvOutCallInfo.text = String.format("Calling %s", mNumber)

        val localHolder: SurfaceHolder = binding.svLocal.holder
        localHolder.addCallback(this)
        Log.d(TAG, "Call type received: $mType")

        binding.svRemote.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}

            override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
                SipServiceCommand.setupIncomingVideoFeed(
                    this@CallActivity,
                    mAccountID.toString(),
                    mCallID,
                    surfaceHolder.surface
                )
            }

            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
                SipServiceCommand.setupIncomingVideoFeed(
                    this@CallActivity,
                    mAccountID.toString(),
                    mCallID,
                    null
                )
            }
        })
    }

    private fun setupClickListeners() {
        binding.buttonAccept.setOnClickListener {
            SipServiceCommand.acceptIncomingCall(this, mAccountID.toString(), mCallID, mIsVideo)
        }
        binding.buttonHangup.setOnClickListener {
            SipServiceCommand.declineIncomingCall(this, mAccountID.toString(), mCallID)
            finish()
        }
        binding.btnCancel.setOnClickListener {
            SipServiceCommand.hangUpActiveCalls(this, mAccountID.toString())
            finish()
        }
        binding.btnMuteMic.setOnClickListener {
            micMute = !micMute
            SipServiceCommand.setCallMute(this, mAccountID.toString(), mCallID, micMute)
            binding.btnMuteMic.isSelected = micMute
        }
        binding.btnHangUp.setOnClickListener {
            SipServiceCommand.hangUpCall(this, mAccountID.toString(), mCallID)
            finish()
        }
        binding.btnSwitchCamera.setOnClickListener {
            SipServiceCommand.switchVideoCaptureDevice(this, mAccountID.toString(), mCallID)
        }
    }

    private fun showLayout(type: Int) {
        Log.d(TAG,"Call type recieve",)
        when (type) {
            TYPE_INCOMING_CALL -> {
                Log.d(TAG, "---> Showing incoming call layout")
                binding.layoutIncomingCall.visibility = View.VISIBLE
                binding.layoutOutCall.visibility = View.GONE
                binding.layoutConnected.visibility = View.GONE
            }

            TYPE_OUT_CALL -> {
                Log.d(TAG, "---> Showing outgoing call layout")
                binding.layoutIncomingCall.visibility = View.GONE
                binding.layoutOutCall.visibility = View.VISIBLE
                binding.layoutConnected.visibility = View.GONE
                Log.d(TAG, "---> After setting visibility: ${binding.layoutOutCall.visibility}")
            }

            TYPE_CALL_CONNECTED -> {
                Log.d(TAG, "---> Showing connected call layout")
                binding.layoutIncomingCall.visibility = View.GONE
                binding.layoutOutCall.visibility = View.GONE
                binding.layoutConnected.visibility = View.VISIBLE
            }

            else -> {
                Log.e(TAG, "Unknown call type: $type")
                Toast.makeText(this, "ERROR~~~~~~~~~~~~~", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            unregisterReceiver(mReceiver)
        } catch (e: IllegalArgumentException) {
            // Receiver was not registered
            Log.e(TAG, "Receiver was not registered: ${e.message}")
        }
    }


    private fun initializeBroadcastReceiver() {
        mReceiver = object : BroadcastEventReceiver() {
            override fun setReceiverContext(context: Context) {
                super.setReceiverContext(context)
                mContext = context
            }
//            override fun setReceiverContext(context: Context) {
//                super.setReceiverContext(this@CallActivity)
//                mContext = getReceiverContext()
//            }

            override fun onIncomingCall(
                accountID: String?,
                callID: Int,
                displayName: String?,
                remoteUri: String?,
                isVideo: Boolean
            ) {
                super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo)
                Log.d(TAG, "----CALLACTIVITY---- onIncomingCall: accountID=$accountID, callID=$callID, displayName=$displayName, remoteUri=$remoteUri, isVideo=$isVideo")
                Toast.makeText(mContext, "call activity -- Incoming call", Toast.LENGTH_LONG).show()
            }


            override fun onCallState(accountID: String?, callID: Int, callStateCode: Int, callStatusCode: Int, connectTimestamp: Long) {
                super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp)

                Log.d(TAG, "📞 Call state changed: accountID=$accountID, callID=$callID, State=$callStateCode, Status=$callStatusCode")

                when (callStateCode) {
                    pjsip_inv_state.PJSIP_INV_STATE_CALLING -> {
                        binding.textViewCallState.text = "Calling..."
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> {
                        binding.textViewCallState.text = "Connecting..."
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
                        binding.textViewCallState.text = "Call Connected"
                    }
                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> {
                        Log.d(TAG, "❌ Call disconnected")
                        finish()
                    }
                }
            }



//            override fun onCallState(
//                accountID: String?,
//                callID: Int,
//                callStateCode: Int,
//                callStatusCode: Int,
//                connectTimestamp: Long
//            ) {
//                super.onCallState(
//                    accountID,
//                    callID,
//                    callStateCode,
//                    callStatusCode,
//                    connectTimestamp
//                )
//                Log.d(TAG, "******* onCallState: accountID=$accountID, callID=$callID, callStateCode=$callStateCode, callStatusCode=$callStatusCode")
//                when (callStateCode) {
//                    pjsip_inv_state.PJSIP_INV_STATE_CALLING -> binding.textViewCallState.text = "Calling"
//                    pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> binding.textViewCallState.text = "Incoming"
//                    pjsip_inv_state.PJSIP_INV_STATE_EARLY -> binding.textViewCallState.text = "Early"
//                    pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> binding.textViewCallState.text = "Connecting"
//                    pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
//                        binding.textViewCallState.text = "Confirmed"
//                        showLayout(CallActivity.Companion.TYPE_CALL_CONNECTED)
//                    }
//                    pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> finish()
//                    pjsip_inv_state.PJSIP_INV_STATE_NULL -> {
//                        // Unknown error
//                        finish()
//                        if (mContext != null) {
//                            Toast.makeText(mContext, "Unknown error", Toast.LENGTH_SHORT).show()
//                        }
//                        finish()
//                    }
//                }
//            }

            override fun onOutgoingCall(
                accountID: String?,
                callID: Int,
                number: String?,
                isVideo: Boolean,
                isVideoConference: Boolean,
                isTransfer: Boolean
            ) {
                Log.d(TAG, "###### onOutgoingCall: $accountID, $callID, $number, $isVideo, $isVideoConference, $isTransfer")
                super.onOutgoingCall(
                    accountID,
                    callID,
                    number,
                    isVideo,
                    isVideoConference,
                    isTransfer
                )
            }

            override fun onStackStatus(started: Boolean) {
                super.onStackStatus(started)
            }

            override fun onReceivedCodecPriorities(codecPriorities: ArrayList<CodecPriority>) {
                super.onReceivedCodecPriorities(codecPriorities)
            }

            override fun onCodecPrioritiesSetStatus(success: Boolean) {
                super.onCodecPrioritiesSetStatus(success)
            }

            override fun onMissedCall(displayName: String?, uri: String?) {
                super.onMissedCall(displayName, uri)
            }

            override fun onVideoSize(width: Int, height: Int) {
                super.onVideoSize(width, height)
            }

            override fun onCallStats(
                callID: Int,
                duration: Int,
                audioCodec: String?,
                callStatusCode: Int,
                rx: RtpStreamStats?,
                tx: RtpStreamStats?
            ) {
                super.onCallStats(callID, duration, audioCodec, callStatusCode, rx, tx)
            }
        }
    }

    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
        SipServiceCommand.startVideoPreview(
            this@CallActivity,
            mAccountID.toString(),
            mCallID,
            binding.svLocal.holder.surface
        )
    }

    override fun surfaceChanged(
        holder: SurfaceHolder,
        format: Int,
        width: Int,
        height: Int
    ) {
        TODO("Not yet implemented")
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        TODO("Not yet implemented")
    }

    companion object {
        private const val TAG = "CallActivity"

        const val TYPE_INCOMING_CALL: kotlin.Int = 646
        const val TYPE_OUT_CALL: kotlin.Int = 647
        const val TYPE_CALL_CONNECTED: kotlin.Int = 648


        fun startActivityIn(context: Context, accountID: String, callID: Int, displayName: String, remoteUri: String, isVideo: Boolean) {
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra("accountID", accountID)
                putExtra("callID", callID)
                putExtra("displayName", displayName)
                putExtra("remoteUri", remoteUri)
                putExtra("isVideo", isVideo)
                putExtra("type", TYPE_INCOMING_CALL)
            }
            context.startActivity(intent)
        }

        fun startActivityOut(context: Context, accountID: String, callID: Int, number: String, isVideo: Boolean, isVideoConference: Boolean) {
            val intent = Intent(context, CallActivity::class.java).apply {
                putExtra("accountID", accountID)
                putExtra("callID", callID)
                putExtra("number", number)
                putExtra("isVideo", isVideo)
                putExtra("isVideoConference", isVideoConference)
                putExtra("type", TYPE_OUT_CALL)
            }
            context.startActivity(intent)
        }

//        fun startActivityOut(
//            context: Context, accountID: String, callID: Int, number: String, isVideo: Boolean, isVideoConference: Boolean
//        ) {
//            Log.d("hi", "Outgoing call broadcast received with params:")
//            Log.d("hi", "Account ID: ${accountID}")
//            Log.d("hi", "Call ID: ${callID}")
//            Log.d("hi", "Number: ${number}")
//
//
//            val intent = Intent(context, CallActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("accountID", accountID)
//                putExtra("callID", callID)
//                putExtra("number", number)
//                putExtra("isVideo", isVideo)
//                putExtra("isVideoConference", isVideoConference)
//                putExtra("type", TYPE_OUT_CALL)
//            }
//            context.startActivity(intent)
//
//        }
    }

}
