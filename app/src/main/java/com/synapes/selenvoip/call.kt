//package com.example.pjsipgo
//
//import android.content.Context
//import android.content.Intent
//import android.os.Bundle
//import android.view.SurfaceHolder
//import android.view.SurfaceView
//import android.view.View
//import android.widget.*
//import androidx.appcompat.app.AppCompatActivity
//import butterknife.BindView
//import butterknife.ButterKnife
//import butterknife.OnClick
//import net.gotev.sipservice.BroadcastEventReceiver
//import net.gotev.sipservice.CodecPriority
//import net.gotev.sipservice.RtpStreamStats
//import net.gotev.sipservice.SipServiceCommand
//import org.pjsip.pjsua2.pjsip_inv_state
//import org.pjsip.pjsua2.pjsip_status_code

//class CallActivity : AppCompatActivity(), SurfaceHolder.Callback {
//    companion object {
//        private const val TAG = "CallActivity"
//        const val TYPE_INCOMING_CALL = 646
//        const val TYPE_OUT_CALL = 647
//        const val TYPE_CALL_CONNECTED = 648
//
//        fun startActivityIn(context: Context, accountID: String, callID: Int, displayName: String, remoteUri: String, isVideo: Boolean) {
//            val intent = Intent(context, CallActivity::class.java).apply {
//                putExtra("accountID", accountID)
//                putExtra("callID", callID)
//                putExtra("displayName", displayName)
//                putExtra("remoteUri", remoteUri)
//                putExtra("isVideo", isVideo)
//                putExtra("type", TYPE_INCOMING_CALL)
//            }
//            context.startActivity(intent)
//        }
//
//        fun startActivityOut(context: Context, accountID: String, callID: Int, number: String, isVideo: Boolean, isVideoConference: Boolean) {
//            val intent = Intent(context, CallActivity::class.java).apply {
//                putExtra("accountID", accountID)
//                putExtra("callID", callID)
//                putExtra("number", number)
//                putExtra("isVideo", isVideo)
//                putExtra("isVideoConference", isVideoConference)
//                putExtra("type", TYPE_OUT_CALL)
//            }
//            context.startActivity(intent)
//        }
//    }
//    @BindView(R.id.textViewPeer)
//    lateinit var mTextViewPeer: TextView


/*
    @BindView(R.id.textViewCallState)
    lateinit var mTextViewCallState: TextView
    @BindView(R.id.buttonAccept)
    lateinit var mButtonAccept: Button
    @BindView(R.id.buttonHangup)
    lateinit var mButtonHangup: Button
    @BindView(R.id.layoutIncomingCall)
    lateinit var mLayoutIncomingCall: LinearLayout

//    @BindView(R.id.tvOutCallInfo)
//    lateinit var mTvOutCallInfo: TextView

    @BindView(R.id.btnCancel)
    lateinit var mBtnCancel: Button
    @BindView(R.id.layoutOutCall)
    lateinit var mLayoutOutCall: LinearLayout
    @BindView(R.id.svRemote)
    lateinit var mSvRemote: SurfaceView
    @BindView(R.id.svLocal)
    lateinit var mSvLocal: SurfaceView
    @BindView(R.id.btnMuteMic)
    lateinit var mBtnMuteMic: ImageButton
    @BindView(R.id.btnHangUp)
    lateinit var mBtnHangUp: ImageButton
    @BindView(R.id.btnSwitchCamera)
    lateinit var mBtnSpeaker: ImageButton
    @BindView(R.id.layoutConnected)
    lateinit var mLayoutConnected: RelativeLayout
    @BindView(R.id.parent)
    lateinit var mParent: LinearLayout

    private lateinit var mAccountID: String
    private lateinit var mDisplayName: String
    private lateinit var mRemoteUri: String
    private var mCallID: Int = 0
    private var mIsVideo: Boolean = false
    private var mType: Int = 0
    private lateinit var mNumber: String
    private var mIsVideoConference: Boolean = false
    private var micMute: Boolean = false
    */


//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_call)
//        ButterKnife.bind(this)
//        registReceiver()
//        initData()
//    }

//    private fun registReceiver() {
//        mReceiver.register(this)
//    }

//    private fun initData() {
//        mAccountID = intent.getStringExtra("accountID") ?: ""
//        mCallID = intent.getIntExtra("callID", -1)
//        mType = intent.getIntExtra("type", -1)
//        mDisplayName = intent.getStringExtra("displayName") ?: ""
//        mRemoteUri = intent.getStringExtra("remoteUri") ?: ""
//        mNumber = intent.getStringExtra("number") ?: ""
//        mIsVideo = intent.getBooleanExtra("isVideo", false)
//        mIsVideoConference = intent.getBooleanExtra("isVideoConference", false)

//        showLayout(mType)
//        mTextViewPeer.text = "$mRemoteUri\n$mDisplayName"
//        mTvOutCallInfo.text = "You are calling $mNumber"

//        mSvLocal.holder.addCallback(this)

//        mSvRemote.holder.addCallback(object : SurfaceHolder.Callback {
//            override fun surfaceCreated(surfaceHolder: SurfaceHolder) {}
//
//            override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {
//                SipServiceCommand.setupIncomingVideoFeed(this@CallActivity, mAccountID, mCallID, surfaceHolder.surface)
//            }
//
//            override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {
//                SipServiceCommand.setupIncomingVideoFeed(this@CallActivity, mAccountID, mCallID, null)
//            }
//        })
//    }

//    @OnClick(R.id.buttonAccept, R.id.buttonHangup, R.id.btnCancel, R.id.btnMuteMic, R.id.btnHangUp, R.id.btnSwitchCamera)
//    fun onViewClicked(view: View) {
//        when (view.id) {
//            R.id.buttonAccept -> SipServiceCommand.acceptIncomingCall(this, mAccountID, mCallID, mIsVideo)
//            R.id.buttonHangup -> {
//                SipServiceCommand.declineIncomingCall(this, mAccountID, mCallID)
//                finish()
//            }
//            R.id.btnCancel -> {
//                SipServiceCommand.hangUpActiveCalls(this, mAccountID)
//                finish()
//            }
//            R.id.btnMuteMic -> {
//                micMute = !micMute
//                SipServiceCommand.setCallMute(this, mAccountID, mCallID, micMute)
//                mBtnMuteMic.isSelected = micMute
//            }
//            R.id.btnHangUp -> {
//                SipServiceCommand.hangUpCall(this, mAccountID, mCallID)
//                finish()
//            }
//            R.id.btnSwitchCamera -> SipServiceCommand.switchVideoCaptureDevice(this, mAccountID, mCallID)
//        }
//    }

/*
//    private fun showLayout(type: Int) {
//        when (type) {
//            TYPE_INCOMING_CALL -> {
//                mLayoutIncomingCall.visibility = View.VISIBLE
//                mLayoutOutCall.visibility = View.GONE
//                mLayoutConnected.visibility = View.GONE
//            }
//            TYPE_OUT_CALL -> {
//                mLayoutIncomingCall.visibility = View.GONE
//                mLayoutOutCall.visibility = View.VISIBLE
//                mLayoutConnected.visibility = View.GONE
//            }
//            TYPE_CALL_CONNECTED -> {
//                mLayoutIncomingCall.visibility = View.GONE
//                mLayoutOutCall.visibility = View.GONE
//                mLayoutConnected.visibility = View.VISIBLE
//            }
//            else -> {
//                val textView = TextView(this)
//                textView.text = "ERROR~~~~~~~~~~~~~"
//                mParent.addView(textView)
//            }
//        }
//    }

//    override fun onDestroy() {
//        super.onDestroy()
//        mReceiver.unregister(this)
//    }

//    override fun surfaceCreated(surfaceHolder: SurfaceHolder) {
//        SipServiceCommand.startVideoPreview(this@CallActivity, mAccountID, mCallID, mSvLocal.holder.surface)
//    }

//    override fun surfaceChanged(surfaceHolder: SurfaceHolder, i: Int, i1: Int, i2: Int) {}

//    override fun surfaceDestroyed(surfaceHolder: SurfaceHolder) {}

//    val mReceiver: BroadcastEventReceiver = object : BroadcastEventReceiver() {
//        override fun onIncomingCall(accountID: String, callID: Int, displayName: String, remoteUri: String, isVideo: Boolean) {
//            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo)
//            Toast.makeText(receiverContext, "Incoming call from [$remoteUri]", Toast.LENGTH_SHORT).show()
//        }

//        override fun onCallState(accountID: String, callID: Int, callStateCode: pjsip_inv_state, callStatusCode: pjsip_status_code, connectTimestamp: Long, isLocalHold: Boolean, isLocalMute: Boolean, isLocalVideoMute: Boolean) {
//            super.onCallState(accountID, callID, callStateCode, callStatusCode, connectTimestamp, isLocalHold, isLocalMute, isLocalVideoMute)
//            when (callStateCode) {
//                pjsip_inv_state.PJSIP_INV_STATE_CALLING -> mTextViewCallState.text = "calling"
//                pjsip_inv_state.PJSIP_INV_STATE_INCOMING -> mTextViewCallState.text = "incoming"
//                pjsip_inv_state.PJSIP_INV_STATE_EARLY -> mTextViewCallState.text = "early"
//                pjsip_inv_state.PJSIP_INV_STATE_CONNECTING -> mTextViewCallState.text = "connecting"
//                pjsip_inv_state.PJSIP_INV_STATE_CONFIRMED -> {
//                    mTextViewCallState.text = "confirmed"
//                    showLayout(TYPE_CALL_CONNECTED)
//                }
//                pjsip_inv_state.PJSIP_INV_STATE_DISCONNECTED -> finish()
//                pjsip_inv_state.PJSIP_INV_STATE_NULL -> {
//                    Toast.makeText(receiverContext, "Unknown error", Toast.LENGTH_SHORT).show()
//                    finish()
//                }
//            }
//        }

//        override fun onOutgoingCall(accountID: String, callID: Int, number: String, isVideo: Boolean, isVideoConference: Boolean) {
//            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference)
//        }
//
//        override fun onStackStatus(started: Boolean) {
//            super.onStackStatus(started)
//        }
//
//        override fun onReceivedCodecPriorities(codecPriorities: ArrayList<CodecPriority>) {
//            super.onReceivedCodecPriorities(codecPriorities)
//        }
//
//        override fun onCodecPrioritiesSetStatus(success: Boolean) {
//            super.onCodecPrioritiesSetStatus(success)
//        }
//
//        override fun onMissedCall(displayName: String, uri: String) {
//            super.onMissedCall(displayName, uri)
//        }
//
//        override fun onVideoSize(width: Int, height: Int) {
//            super.onVideoSize(width, height)
//        }
//
//        override fun onCallStats(duration: Int, audioCodec: String, callStatusCode: pjsip_status_code, rx: RtpStreamStats, tx: RtpStreamStats) {
//            super.onCallStats(duration, audioCodec, callStatusCode, rx, tx)
//        }
//    }
    */

//}
