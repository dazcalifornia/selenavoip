package com.synapes.selenvoip

import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selenvoip.databinding.ActivityMainBinding
import org.pjsip.pjsua2.pjsip_status_code

class oldActivityMain : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var mAccount: SipAccountData? = null
    private var mAccountId: String? = null
    private var isRegistrationComplete = false

    private var lastLoggedSignalStrength: Int = -1
    private var lastLoggedTime: Long = 0
    private val LOG_INTERVAL = 60000 // 1 minute

    private lateinit var systemBroadcastReceiver: BroadcastReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var telephonyCallback: TelephonyCallback

    private lateinit var mReceiver: BroadcastEventReceiver


    private val localBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val originalAction = intent.getStringExtra(BroadcastEventReceiver.EXTRA_ORIGINAL_ACTION)
            Log.d(TAG, "===LOCAL BROADCAST RECEIVED==== Original action: $originalAction")

            when (originalAction) {


                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION) -> {
                    Log.d(TAG, "MainActivity: Received: onRegistration")
                }
                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.OUTGOING_CALL) -> {
                    Log.d(TAG, "Outgoing call broadcast received with params:")
                    Log.d(TAG, "Account ID: ${mAccountId}")
                    Log.d(TAG, "Call ID: ${intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1)}")
                    Log.d(TAG, "Remote URI: ${intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI)}")

                    CallActivity.startActivityOut(
                        this@oldActivityMain,
                        mAccountId.toString(),
                        intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1),
                        intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI) ?: "",
                        intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false),
                        false
                    )
                    Log.d(TAG, "-------- SHOWN CALL ACTIVITY SCREEN OUTGOING CALL")
                }


                BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.INCOMING_CALL) -> {
                    Log.d(TAG, "MainActivity: Received: Incoming call")
                    val accountID = intent.getStringExtra(SipServiceConstants.PARAM_ACCOUNT_ID)
                    val callID = intent.getIntExtra(SipServiceConstants.PARAM_CALL_ID, -1)
                    val displayName = intent.getStringExtra(SipServiceConstants.PARAM_DISPLAY_NAME)
                    val remoteUri = intent.getStringExtra(SipServiceConstants.PARAM_REMOTE_URI)
                    val isVideo = intent.getBooleanExtra(SipServiceConstants.PARAM_IS_VIDEO, false)
                    Log.d(TAG, "Account ID: $accountID, Call ID: $callID, Display Name: $displayName, Remote URI: $remoteUri, Is Video: $isVideo")
//                    handleIncomingCall(intent)
                    SipServiceCommand.acceptIncomingCall(this@oldActivityMain, accountID.toString(), callID, isVideo);

                    CallActivity.startActivityIn(this@oldActivityMain,
                        accountID.toString(), callID, displayName.toString(), remoteUri.toString(),isVideo,)
                }
                else -> {
                    Log.d(TAG, "MainActivity: Unhandled broadcast: $originalAction")
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        // Initialize managers
        connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        telephonyManager = getSystemService(TELEPHONY_SERVICE) as TelephonyManager
        wifiManager = applicationContext.getSystemService(WIFI_SERVICE) as WifiManager

        // Initialize the broadcast receiver
        initSystemBroadcastReceiver()

        // Register network callback
        registerNetworkCallback()

        // Register signal strength listener
        registerSignalStrengthListener()

        setSupportActionBar(binding.toolbar)

        // Register the localBroadcastReceiver to receive local broadcasts
        val filter = IntentFilter(BroadcastEventReceiver.LOCAL_BROADCAST_ACTION)
        LocalBroadcastManager.getInstance(this).registerReceiver(localBroadcastReceiver, filter)



        requestPermissions()

        // Set up the call button click listener
        binding.callButton.setOnClickListener {
            val destinationNumber = binding.destinationNumberEditText.text.toString()
            if (destinationNumber.isNotEmpty()) {
                Toast.makeText(this, "Making call to $destinationNumber", Toast.LENGTH_SHORT).show()
                audioCall(destinationNumber)

            } else {
                Toast.makeText(this, "Please enter a destination number", Toast.LENGTH_SHORT).show()
            }
        }

    }

    //Audio call

    fun audioCall(callNumber: String) {
        requestPermissions()

        try {
            // 🔹 Check if Account ID is set before calling
            if (mAccountId.isNullOrEmpty()) {
                Log.e(TAG, "Account ID is null or empty. Cannot proceed with the call.")
                Toast.makeText(this, "SIP account is not registered", Toast.LENGTH_SHORT).show()
                return
            }

            // 🔹 Check if SIP registration is complete
            if (!isRegistrationComplete) {
                Log.e(TAG, "SIP registration is incomplete. Call cannot proceed.")
                Toast.makeText(this, "SIP account is not registered yet", Toast.LENGTH_SHORT).show()
                return
            }

            // 🔹 Format the number
            val formattedNumber = if (callNumber.contains("@")) {
                callNumber
            } else {
                "$callNumber@synapes-pbx-poc-01.online"
            }

            // 🔹 Log details before making the call
            Log.d(TAG, """
            Audio Call Details:
            - Account ID: $mAccountId
            - Raw Number: $callNumber
            - Formatted Number: $formattedNumber
        """.trimIndent())

            // 🔹 Log before sending the call request
            Log.d(TAG, "Sending call request to SipServiceCommand.makeCall()")

            // 🔹 Attempt to make the call
            Log.d("Burb", "makeCall triggered for number: $formattedNumber on account: $mAccountId")

            SipServiceCommand.makeCall(this, mAccountId!!, formattedNumber, false, false)


            // 🔹 Log after successfully sending the request
            Log.d(TAG, "Call request sent successfully.")

        } catch (e: Exception) {
            Log.e(TAG, "Audio call error", e)
            Log.e(TAG, "Stack trace: ${e.stackTrace.joinToString("\n")}")
            Toast.makeText(this, "Error initiating call", Toast.LENGTH_SHORT).show()
        }
    }

//    fun audioCall(callNumber: String) {
//        requestPermissions()
//        try {
//            mAccountId?.let { accountId ->
//                // Format the call number with domain if needed
//                val formattedNumber = if (callNumber.contains("@")) {
//                    callNumber
//                } else {
//                    "$callNumber@synapes-pbx-poc-01.online"
//                }
//
//                if (mAccountId.isNullOrEmpty()) {
//                    Log.e(TAG, "Account ID is null or empty. Cannot proceed with the call.")
//                    Toast.makeText(this, "SIP account is not registered", Toast.LENGTH_SHORT).show()
//                    return
//                }else{
//                    Log.i(TAG, "Account ID is not empty. Cannot proceed with the call.")
//                }
//
//
//                Log.d(TAG, """
//                Audio Call Details:
//                - Account ID: $accountId
//                - Raw Number: $callNumber
//                - Formatted Number: $formattedNumber
//            """.trimIndent())
//
//                SipServiceCommand.makeCall(this, accountId, formattedNumber, false, false)
//            } ?: run {
//                Log.e(TAG, "Account ID is null")
//                Toast.makeText(this, "Account not set up", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            Log.e(TAG, "Audio call error", e)
//            Log.e(TAG, "Stack trace: ${e.stackTrace.joinToString("\n")}")
//            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show()
//        }
//    }


//    fun audioCall(callNumber: String) {
//        requestPermissions()
//        try {
//            mAccountId?.let { accountId ->
//                Log.d(TAG, "Initiating call with accountId: $accountId to number: $callNumber")
//                SipServiceCommand.makeCall(this, accountId, callNumber, false, false)
//            } ?: run {
//                Toast.makeText(this, "Account not set up", Toast.LENGTH_SHORT).show()
//            }
//        } catch (e: Exception) {
//            e.printStackTrace()
//            Log.e(TAG,"ERR: ${e}")
//            Toast.makeText(this, "Account error", Toast.LENGTH_SHORT).show()
//        }
//    }




    private fun requestPermissions() {
        val permissionsToRequest = REQUIRED_PERMISSIONS.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }.toTypedArray()

        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest,
                Config.PERMISSION_REQUEST_CODE
            )
        } else {
            Log.i(TAG, "All permissions are already granted. Initializing app...")
            initializeApp()
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    override fun onResume() {
        super.onResume()
        initSystemBroadcastReceiver()
        registerNetworkCallback()
        registerSignalStrengthListener()
        checkVpnStatus()
    }

    override fun onPause() {
        super.onPause()
        unregisterReceiver(systemBroadcastReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
        // Unregister TelephonyCallback if using Android 12+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            telephonyManager.unregisterTelephonyCallback(telephonyCallback)
        }
    }

    private fun initSystemBroadcastReceiver() {
        systemBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BOOT_COMPLETED -> {
                        Log.d("SystemBroadcast", "Boot completed")
                    }

                    Intent.ACTION_POWER_CONNECTED -> {
                        Log.d("SystemBroadcast", "Power connected")
                    }

                    Intent.ACTION_POWER_DISCONNECTED -> {
                        Log.d("SystemBroadcast", "Power disconnected")
                    }

                    Intent.ACTION_BATTERY_LOW -> {
                        Log.d("SystemBroadcast", "Battery low")
                    }

                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                        Log.d("SystemBroadcast", "Airplane mode changed: $isAirplaneModeOn")
                    }

                    WifiManager.WIFI_STATE_CHANGED_ACTION -> {
                        when (intent.getIntExtra(
                            WifiManager.EXTRA_WIFI_STATE,
                            WifiManager.WIFI_STATE_UNKNOWN
                        )) {
                            WifiManager.WIFI_STATE_ENABLED -> {
                                Log.d("SystemBroadcast", "Wi-Fi enabled")
                                updateWifiStrength()
                            }

                            WifiManager.WIFI_STATE_DISABLED -> {
                                Log.d("SystemBroadcast", "Wi-Fi disabled")
                            }
                        }
                    }
                }
            }
        }

        // Register the broadcast receiver with an IntentFilter
        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        registerReceiver(systemBroadcastReceiver, intentFilter)
    }

    private fun registerSignalStrengthListener() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.READ_PHONE_STATE
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                telephonyCallback =
                    object : TelephonyCallback(), TelephonyCallback.SignalStrengthsListener {
                        override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                            val cellularStrength = signalStrength.level
                            logSignalStrengthIfNeeded(cellularStrength)
                        }
                    }
                telephonyManager.registerTelephonyCallback(mainExecutor, telephonyCallback)
            } else {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.READ_PHONE_STATE),
                    PERMISSION_REQUEST_READ_PHONE_STATE
                )
            }
        } else {
            @Suppress("DEPRECATION")
            telephonyManager.listen(object : PhoneStateListener() {
                override fun onSignalStrengthsChanged(signalStrength: SignalStrength) {
                    val cellularStrength = signalStrength.level
                    logSignalStrengthIfNeeded(cellularStrength)
                }
            }, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS)
        }
    }

    private fun logSignalStrengthIfNeeded(cellularStrength: Int) {
        val currentTime = System.currentTimeMillis()
        if (cellularStrength != lastLoggedSignalStrength || currentTime - lastLoggedTime > LOG_INTERVAL) {
            Log.d(TAG, "Cellular signal strength: $cellularStrength")
            lastLoggedSignalStrength = cellularStrength
            lastLoggedTime = currentTime
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            Config.PERMISSION_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    Log.d(TAG, "All permissions granted")
                    initializeApp()
                } else {
                    val deniedPermissions = permissions.filterIndexed { index, _ ->
                        grantResults[index] == PackageManager.PERMISSION_DENIED
                    }
                    Log.d(TAG, "Denied permissions: $deniedPermissions")
                }
            }

            PERMISSION_REQUEST_READ_PHONE_STATE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    registerSignalStrengthListener()
                } else {
                    Log.d("Permissions", "READ_PHONE_STATE permission denied")
                }
            }

            else -> {
                Log.d(TAG, "Unhandled permission request: $requestCode")
            }
        }
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities?.let {
                    when {
                        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> {
                            Log.d("NetworkCallback", "Cellular connection available")
                        }

                        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> {
                            Log.d("NetworkCallback", "Wi-Fi connection available")
                        }

                        it.hasTransport(NetworkCapabilities.TRANSPORT_VPN) -> {
                            Log.d("NetworkCallback", "VPN connection available")
                        }

                        else -> {
                            Log.d(TAG, "Unhandled network callback")
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
                    Log.d(TAG, "VPN connection lost")
                } else {
                    Log.d(TAG, "Network lost")
                }
            }

            override fun onCapabilitiesChanged(
                network: Network,
                networkCapabilities: NetworkCapabilities
            ) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    Log.d(TAG, "VPN capabilities changed")
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun updateWifiStrength() {
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.networkId != -1) {  // Check if connected to a network
            val rssi = wifiInfo.rssi
            val level = WifiManager.calculateSignalLevel(rssi, 5)
            Log.d(TAG, "Wi-Fi signal strength: $level (RSSI: $rssi)")
        } else {
            Log.d(TAG, "Not connected to Wi-Fi")
        }
    }

    private fun checkVpnStatus() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            Log.d(TAG, "VPN is currently active")
            Toast.makeText(this, "HI", Toast.LENGTH_LONG).show()
        } else {
            Log.d(TAG, "No active VPN connection")
        }
    }

    private fun initializeApp() {
        Log.d(TAG, "App initialization complete. Start logging in...")
        autoLogin()
    }

    private fun autoLogin() {
        Log.d(TAG, "Attempting auto-login...")

        val server = "synapes-pbx-poc-01.online"
        val account = "933933"
        val password = "933933"
        val port = "5060"

        if (server.isNotEmpty() && account.isNotEmpty() && password.isNotEmpty() && port.isNotEmpty()) {
            mAccount = SipAccountData().apply {
                host = server
                realm = "*"
                this.port = port.toInt()
                username = account
                this.password = password
                transport = SipAccountTransport.UDP

                // Ensure these fields are set correctly
                setAuthenticationType(SipAccountData.AUTH_TYPE_DIGEST)
                setRealm(server) // Use the server as the realm
                setRegExpirationTimeout(300) // Set registration expiration (e.g., 300 seconds)
            }

            try {
                val sipAccountData = mAccount!!
                mAccountId = SipServiceCommand.setAccount(this, sipAccountData)
                Log.d(TAG, "Auto-login initiated with account ID: $mAccountId")
                Log.d(
                    TAG,
                    "**** Account Info: ${sipAccountData.getRegistrarUri()}, ${sipAccountData.getAuthCredInfo()}, ${sipAccountData.username}, ${sipAccountData.password}\n" +
                            "**** SIP URI: ${sipAccountData.getIdUri()}\n" +
                            "**** Proxy URI: ${sipAccountData.getProxyUri()}"
                )
                isRegistrationComplete = true
//                Toast.makeText(this,"sipAccountData:${sipAccountData}", Toast.LENGTH_LONG).show()

            } catch (e: Exception) {
                Log.e(TAG, "Auto-login failed: ${e.message}")
                Toast.makeText(this, "Auto-login failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        } else {
            Log.d(TAG, "Auto-login failed: Missing credentials")
            Toast.makeText(this, "Auto-login failed: Missing credentials", Toast.LENGTH_SHORT)
                .show()
        }
        Log.d(TAG, "Auto-login completed. Registration = $isRegistrationComplete")
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        // Register the BroadcastReceiver to listen for custom broadcasts
        val intentFilter =
            IntentFilter(BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION))


    }

    override fun onStop() {
        super.onStop()
        // Unregister the BroadcastReceiver to avoid memory leaks
//        unregisterReceiver(mReceiver)
        Log.d(TAG, "onStop() called")
    }

    override fun onDestroy() {
        super.onDestroy()
        // Unregister the broadcast receivers
        unregisterReceiver(systemBroadcastReceiver)
        LocalBroadcastManager.getInstance(this).unregisterReceiver(localBroadcastReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    companion object {
        private const val TAG = "MainActivity"
        private const val PERMISSION_REQUEST_READ_PHONE_STATE = 1
        private val REQUIRED_PERMISSIONS = arrayOf(
            Manifest.permission.USE_SIP,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.WAKE_LOCK,
            Manifest.permission.INTERNET,
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.MODIFY_AUDIO_SETTINGS
        )
    }
}