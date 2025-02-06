//package com.example.pjsipgo
//
//import android.Manifest
//import android.content.pm.PackageManager
//import android.os.Bundle
//import android.text.TextUtils
//import android.util.Log
//import android.view.View
//import android.widget.EditText
//import android.widget.LinearLayout
//import android.widget.Toast
//import net.gotev.sipservice.BroadcastEventReceiver
//import net.gotev.sipservice.Logger
//import net.gotev.sipservice.SipAccountData
//import net.gotev.sipservice.SipServiceCommand
//import org.pjsip.pjsua2.pjsip_status_code
//import androidx.annotation.NonNull
//import androidx.appcompat.app.AppCompatActivity
//import androidx.core.app.ActivityCompat
//import androidx.core.content.ContextCompat
//import butterknife.BindView
//import butterknife.ButterKnife
//
//class MainActivity : AppCompatActivity() {
//    companion object {
//        private const val TAG = "MainActivity"
//        private const val REQUEST_PERMISSIONS_STORAGE = 0x100
//    }
//
//    @BindView(R.id.etAccount)
//    lateinit var mEtAccount: EditText
//    @BindView(R.id.etPwd)
//    lateinit var mEtPwd: EditText
//    @BindView(R.id.etServer)
//    lateinit var mEtServer: EditText
//    @BindView(R.id.etPort)
//    lateinit var mEtPort: EditText
//    @BindView(R.id.layoutLogin)
//    lateinit var mLayoutLogin: LinearLayout
//    @BindView(R.id.etCallNumer)
//    lateinit var mEtCallNumer: EditText
//    @BindView(R.id.layoutCallOut)
//    lateinit var mLayoutCallOut: LinearLayout
//
//    private var mAccount: SipAccountData? = null
//    private var mAccountId: String? = null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContentView(R.layout.activity_main)
//        ButterKnife.bind(this)
//        mReceiver.register(this)
//        Logger.setLogLevel(Logger.LogLevel.DEBUG)
//        requestPermissions()
//    }
//
//    fun login(view: View) {
//        val server = mEtServer.text.toString().trim()
//        val account = mEtAccount.text.toString().trim()
//        val pwd = mEtPwd.text.toString().trim()
//        val port = mEtPort.text.toString().trim()
//        if (TextUtils.isEmpty(server) || TextUtils.isEmpty(account) || TextUtils.isEmpty(pwd) || TextUtils.isEmpty(port)) {
//            Toast.makeText(this, "Please fill in all information!", Toast.LENGTH_SHORT).show()
//            return
//        }
//
//        mAccount = SipAccountData().apply {
//            host = server
//            realm = "*"
//            this.port = port.toInt()
//            username = account
//            password = pwd
//        }
//        mAccountId = SipServiceCommand.setAccount(this, mAccount!!)
//        Log.i(TAG, "login: $mAccountId")
//    }
//
//    fun audioCall(view: View) {
//        requestPermissions()
//        val callNumber = mEtCallNumer.text.toString().trim()
//        if (TextUtils.isEmpty(callNumber)) {
//            Toast.makeText(this, "Please enter the call number!", Toast.LENGTH_SHORT).show()
//            return
//        }
//        try {
//            SipServiceCommand.makeCall(this, mAccountId, callNumber, false, false)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    fun videoCall(view: View) {
//        requestPermissions()
//        val callNumber = mEtCallNumer.text.toString().trim()
//        if (TextUtils.isEmpty(callNumber)) {
//            Toast.makeText(this, "Please enter the call number!", Toast.LENGTH_SHORT).show()
//            return
//        }
//        try {
//            SipServiceCommand.makeCall(this, mAccountId, callNumber, true, false)
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show()
//        }
//    }
//
//    private fun requestPermissions() {
//        val permissions = arrayOf(
//            Manifest.permission.WRITE_EXTERNAL_STORAGE,
//            Manifest.permission.READ_EXTERNAL_STORAGE,
//            Manifest.permission.CAMERA,
//            Manifest.permission.RECORD_AUDIO
//        )
//        if (!checkPermissionAllGranted(permissions)) {
//            ActivityCompat.requestPermissions(this, permissions, REQUEST_PERMISSIONS_STORAGE)
//        }
//    }
//
//    private fun checkPermissionAllGranted(permissions: Array<String>): Boolean {
//        return permissions.all { ContextCompat.checkSelfPermission(this, it) == PackageManager.PERMISSION_GRANTED }
//    }
//
//    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        if (requestCode == REQUEST_PERMISSIONS_STORAGE) {
//            val ok = grantResults.all { it == PackageManager.PERMISSION_GRANTED }
//            if (ok) {
//                Toast.makeText(this@MainActivity, "Permissions granted successfully!", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        mReceiver.unregister(this)
//        mAccount?.let {
//            SipServiceCommand.removeAccount(this, mAccountId)
//        }
//    }
//
//    val mReceiver = object : BroadcastEventReceiver() {
//        override fun onRegistration(accountID: String?, registrationStateCode: pjsip_status_code?) {
//            super.onRegistration(accountID, registrationStateCode)
//            Log.i(TAG, "onRegistration: ")
//            if (registrationStateCode == pjsip_status_code.PJSIP_SC_OK) {
//                Toast.makeText(receiverContext, "Login successful, account: $accountID", Toast.LENGTH_SHORT).show()
//                mLayoutCallOut.visibility = View.VISIBLE
//                mLayoutLogin.visibility = View.GONE
//            } else {
//                Toast.makeText(receiverContext, "Login failed, code: $registrationStateCode", Toast.LENGTH_SHORT).show()
//            }
//        }
//
//        override fun onIncomingCall(accountID: String?, callID: Int, displayName: String?, remoteUri: String?, isVideo: Boolean) {
//            super.onIncomingCall(accountID, callID, displayName, remoteUri, isVideo)
//            CallActivity.startActivityIn(receiverContext, accountID, callID, displayName, remoteUri, isVideo)
//        }
//
//        override fun onOutgoingCall(accountID: String?, callID: Int, number: String?, isVideo: Boolean, isVideoConference: Boolean) {
//            super.onOutgoingCall(accountID, callID, number, isVideo, isVideoConference)
//            CallActivity.startActivityOut(receiverContext, accountID, callID, number, isVideo, isVideoConference)
//        }
//    }
//}