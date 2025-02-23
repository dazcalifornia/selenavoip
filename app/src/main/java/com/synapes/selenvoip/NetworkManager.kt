package com.synapes.selenvoip.managers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.wifi.WifiManager
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class NetworkManager(private val activity: AppCompatActivity) {
    private val TAG = "NetworkManager"

    private lateinit var systemBroadcastReceiver: BroadcastReceiver
    private lateinit var connectivityManager: ConnectivityManager
    private lateinit var wifiManager: WifiManager
    private lateinit var networkCallback: ConnectivityManager.NetworkCallback

    init {
        initializeManagers()
        initSystemBroadcastReceiver()
        registerNetworkCallback()
    }

    private fun initializeManagers() {
        connectivityManager = activity.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        wifiManager = activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private fun initSystemBroadcastReceiver() {
        systemBroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_BOOT_COMPLETED -> Log.d(TAG, "Boot completed")
                    Intent.ACTION_POWER_CONNECTED -> Log.d(TAG, "Power connected")
                    Intent.ACTION_POWER_DISCONNECTED -> Log.d(TAG, "Power disconnected")
                    Intent.ACTION_BATTERY_LOW -> Log.d(TAG, "Battery low")
                    Intent.ACTION_AIRPLANE_MODE_CHANGED -> {
                        val isAirplaneModeOn = intent.getBooleanExtra("state", false)
                        Log.d(TAG, "Airplane mode changed: $isAirplaneModeOn")
                    }
                    WifiManager.WIFI_STATE_CHANGED_ACTION -> handleWifiStateChange(intent)
                }
            }
        }

        val intentFilter = IntentFilter().apply {
            addAction(Intent.ACTION_BOOT_COMPLETED)
            addAction(Intent.ACTION_POWER_CONNECTED)
            addAction(Intent.ACTION_POWER_DISCONNECTED)
            addAction(Intent.ACTION_BATTERY_LOW)
            addAction(Intent.ACTION_AIRPLANE_MODE_CHANGED)
            addAction(WifiManager.WIFI_STATE_CHANGED_ACTION)
        }
        activity.registerReceiver(systemBroadcastReceiver, intentFilter)
    }

    private fun handleWifiStateChange(intent: Intent) {
        when (intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, WifiManager.WIFI_STATE_UNKNOWN)) {
            WifiManager.WIFI_STATE_ENABLED -> {
                Log.d(TAG, "Wi-Fi enabled")
                updateWifiStrength()
            }
            WifiManager.WIFI_STATE_DISABLED -> Log.d(TAG, "Wi-Fi disabled")
        }
    }

    private fun registerNetworkCallback() {
        networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
                val capabilities = connectivityManager.getNetworkCapabilities(network)
                capabilities?.let {
                    when {
                        it.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ->
                            Log.d(TAG, "Cellular connection available")
                        it.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ->
                            Log.d(TAG, "Wi-Fi connection available")
                        it.hasTransport(NetworkCapabilities.TRANSPORT_VPN) ->
                            Log.d(TAG, "VPN connection available")

                        else -> {
                            Log.d(TAG, "damn")
                        }
                    }
                }
            }

            override fun onLost(network: Network) {
                super.onLost(network)
                handleNetworkLost(network)
            }

            override fun onCapabilitiesChanged(network: Network, networkCapabilities: NetworkCapabilities) {
                super.onCapabilitiesChanged(network, networkCapabilities)
                if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                    Log.d(TAG, "VPN capabilities changed")
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
    }

    private fun handleNetworkLost(network: Network) {
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            Log.d(TAG, "VPN connection lost")
        } else {
            Log.d(TAG, "Network lost")
        }
    }

    private fun updateWifiStrength() {
        val wifiInfo = wifiManager.connectionInfo
        if (wifiInfo.networkId != -1) {
            val rssi = wifiInfo.rssi
            val level = WifiManager.calculateSignalLevel(rssi, 5)
            Log.d(TAG, "Wi-Fi signal strength: $level (RSSI: $rssi)")
        } else {
            Log.d(TAG, "Not connected to Wi-Fi")
        }
    }

    fun checkVpnStatus() {
        val network = connectivityManager.activeNetwork
        val capabilities = connectivityManager.getNetworkCapabilities(network)
        if (capabilities?.hasTransport(NetworkCapabilities.TRANSPORT_VPN) == true) {
            Log.d(TAG, "VPN is currently active")
        } else {
            Log.d(TAG, "No active VPN connection")
        }
    }

    fun onResume() {
        initSystemBroadcastReceiver()
        registerNetworkCallback()
//        checkVpnStatus()
    }

    fun onPause() {
        activity.unregisterReceiver(systemBroadcastReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }

    fun onDestroy() {
        activity.unregisterReceiver(systemBroadcastReceiver)
        connectivityManager.unregisterNetworkCallback(networkCallback)
    }
}