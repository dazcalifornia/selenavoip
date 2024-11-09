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
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.os.Build
import android.telephony.PhoneStateListener
import android.telephony.SignalStrength
import android.telephony.TelephonyCallback
import android.telephony.TelephonyManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.synapes.selenvoip.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private var lastLoggedSignalStrength: Int = -1
    private var lastLoggedTime: Long = 0
    private val LOG_INTERVAL = 60000 // 1 minute

    private lateinit var systemBroadcastReceiver: BroadcastReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var telephonyManager: TelephonyManager
    private lateinit var wifiManager: WifiManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback
    private lateinit var telephonyCallback: TelephonyCallback

    private val mainActivityReceiver = object : BroadcastEventReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                ACTION_REGISTRATION_CHECK -> {
                    Log.d(TAG, "MainActivity: Received REGISTRATION_CHECK broadcast")
                    // Implement your registration check logic here
                }

                ACTION_MAKE_CALL -> {
                    Log.d(TAG, "MainActivity: Received MAKE_CALL broadcast")
                    val phoneNumber = intent.getStringExtra(EXTRA_PHONE_NUMBER)
                    if (phoneNumber != null) {
                        Log.d(TAG, "MainActivity: Making call to: $phoneNumber")
                        // Implement your make call logic here
                    } else {
                        Log.e(TAG, "MainActivity: No phone number provided for MAKE_CALL")
                    }
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

        // Register the mainActivityReceiver to receive local broadcasts
        val filter = IntentFilter().apply {
            addAction(BroadcastEventReceiver.ACTION_REGISTRATION_CHECK)
            addAction(BroadcastEventReceiver.ACTION_MAKE_CALL)
        }
        LocalBroadcastManager.getInstance(this).registerReceiver(mainActivityReceiver, filter)

        requestPermissions()
    }

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
        } else {
            Log.d(TAG, "No active VPN connection")
        }
    }

    private fun initializeApp() {
        Log.d(TAG, "App initialization complete. Start logging in...")
//        autoLogin()
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
        // Register the BroadcastReceiver to listen for custom broadcasts
        val intentFilter =
            IntentFilter(BroadcastEventEmitter.getAction(BroadcastEventEmitter.BroadcastAction.REGISTRATION))
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            Log.d(
//                TAG,
//                "Android version TIRAMISU or higher, registering with ContextCompat.RECEIVER_EXPORTED...(For other app and system to receive broadcast)"
//            )
//            mReceiver.register(this, ContextCompat.RECEIVER_EXPORTED)
//        } else {
//            Log.d(
//                TAG,
//                "Android version below TIRAMISU, registering without ContextCompat.RECEIVER_EXPORTED..."
//            )
//            mReceiver.register(this)
//        }
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
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mainActivityReceiver)
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
