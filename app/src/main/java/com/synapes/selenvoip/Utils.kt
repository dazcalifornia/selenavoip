package com.synapes.selenvoip

import android.app.ActivityManager
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.provider.Settings
import android.provider.Settings.Secure.getString
import android.util.Log
import androidx.core.content.ContextCompat
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.util.UUID
import kotlin.collections.all
import kotlin.jvm.java
import kotlin.let
import kotlin.text.contains
import kotlin.text.startsWith

class Utils {
    object DeviceUtils {

        fun getDeviceId(context: Context): String {
            val sharedPrefs = context.getSharedPreferences("device_id", Context.MODE_PRIVATE)
            var uniqueId = sharedPrefs.getString("device_id", null)

            if (uniqueId == null) {
                uniqueId = "999_NO_ID_999"
                sharedPrefs.edit().putString("device_id", uniqueId).apply()
            }

            return uniqueId
        }

        fun getAppVersion(context: Context): String {
            return context.packageManager.getPackageInfo(context.packageName, 0).versionName
                ?: "999_NO_APP_VERSION_999"
        }

        fun getDeviceModel(): String {
            return Build.MODEL ?: "999_NO_DEVICE_MODEL_999"
        }

        fun getAndroidVersion(): String {
            return Build.VERSION.RELEASE ?: "999_NO_ANDROID_VERSION_999"
        }

        fun getDeviceManufacturer(): String {
            return Build.MANUFACTURER ?: "999_NO_MANUFACTURER_999"
        }

    }

    // TAKEN FROM SELEN ALARM BOX
    companion object {
        const val BUTTON_GPIO110 = "/sys/class/gpio/gpio1021/value" // GPIO110 (CN34)
        const val BUTTON_GPIO94 = "/sys/class/gpio/gpio1005/value" // GPIO94 (CN8)
        const val LED_GPIO = "/sys/class/gpio/gpio1009/value"  // GPIO98 (CN12) --> 12V
        const val LED2_GPIO = "/sys/class/gpio/gpio1003/value"  // GPIO92 (CN10) --> depends on cn12
        const val LED3_GPIO = "/sys/class/gpio/gpio999/value"  // GPIO88 (CN9) --> depends on cn12
//    const val BUTTON_GPIO = BUTTON_GPIO94 || BUTTON_GPIO110
    }

    internal fun isGpioAvailable(): Boolean {
        val gpioList =
            listOf(BUTTON_GPIO94, BUTTON_GPIO110, LED_GPIO, LED2_GPIO, LED3_GPIO)
        return gpioList.all { File(it).exists() }
    }

    internal fun readGPIO(str: String?): Int {
        return try {
            val fileInputStream = FileInputStream(str?.let { File(it) })
            val bArr = ByteArray(8)
            fileInputStream.read(bArr)
            fileInputStream.close()
            if (bArr[0].toInt() == 49) 1 else 0
        } catch (e: IOException) {
            Log.e("Read GPIO", "Error: $e")
            0
        }
    }

    internal fun writeGPIO(str: String?, i: Int) {
        try {
            val fileOutputStream = FileOutputStream(str?.let { File(it) })
            fileOutputStream.write(if (i == 0) 48 else 49)
            fileOutputStream.close()
        } catch (e: IOException) {
            Log.e("Write GPIO", "GPIO WRITE Error: $e")
        }
    }


    // FIXME: CHECK IF THIS WORKS ON ANDROID 7
    // DO NOT USE FOR NOW
    internal fun isNetworkDeviceTurnedOn(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val network = connectivityManager.activeNetwork ?: return false
            val networkCapabilities =
                connectivityManager.getNetworkCapabilities(network) ?: return false
            return networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || networkCapabilities.hasTransport(
                NetworkCapabilities.TRANSPORT_CELLULAR
            )
        } else {
            val networkInfo = connectivityManager.activeNetworkInfo
            return networkInfo != null && networkInfo.isConnected
        }
    }

    fun turnOnLed() {
//        if (isGpioAvailable()) writeGPIO(LED_GPIO, 0)
//        if (isGpioAvailable()) writeGPIO(LED2_GPIO, 0)
        // NEW HARDWARE
        if (isGpioAvailable()) writeGPIO(LED_GPIO, 1)
        if (isGpioAvailable()) writeGPIO(LED2_GPIO, 1)
        if (isGpioAvailable()) writeGPIO(LED3_GPIO, 1)
    }

    fun turnOffLed() {
//        if (isGpioAvailable()) writeGPIO(LED_GPIO, 1)
//        if (isGpioAvailable()) writeGPIO(LED2_GPIO, 1)
        // NEW HARDWARE
        if (isGpioAvailable()) writeGPIO(LED_GPIO, 0)
        if (isGpioAvailable()) writeGPIO(LED2_GPIO, 0)
        if (isGpioAvailable()) writeGPIO(LED3_GPIO, 0)
    }

    fun isButtonPressed(): Boolean {
        return isGpioAvailable() && (readGPIO(BUTTON_GPIO94) == 0 || readGPIO(BUTTON_GPIO110) == 0)

    }

    fun isButtonReleased(): Boolean {
        return isGpioAvailable() && (readGPIO(BUTTON_GPIO94) == 1 || readGPIO(BUTTON_GPIO110) == 1)
    }

    fun isLedOn(): Boolean {
        return isGpioAvailable() && readGPIO(LED_GPIO) == 0
    }

    fun isVpnActive(context: Context): Boolean {
        // Skip VPN check if on emulator

        if (isEmulator()) {
            return true
        }
        val connectivityManager =
            ContextCompat.getSystemService(context, ConnectivityManager::class.java)
        val activeNetworks = connectivityManager?.allNetworks ?: emptyArray()

        for (network in activeNetworks) {
            val networkCapabilities = connectivityManager?.getNetworkCapabilities(network)
            if (networkCapabilities != null && networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_VPN)) {
                return true
            }
        }

        return false
    }

    fun isAirplaneModeOn(context: Context): Boolean {
        return Settings.Global.getInt(
            context.contentResolver,
            Settings.Global.AIRPLANE_MODE_ON, 0
        ) != 0
    }

    fun simulateCrash() {
        val exception = RuntimeException("This is a crash - simulated")
        throw exception
//        throw RuntimeException("This is a crash")
    }

    fun isEmulator(): Boolean {
        Log.d("Build.FINGERPRINT", Build.FINGERPRINT)
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("sdk_gphone64_arm64")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
    }

    fun flashLEDUntilReceiveVOIPAppBroadcast() {
        Log.d("Utils", "Pressing call button")
        turnOnLed()
        // TODO: Send broadcast to get a broadcast message back to turn off LED in InterCommunicationBroadcast

    }
}