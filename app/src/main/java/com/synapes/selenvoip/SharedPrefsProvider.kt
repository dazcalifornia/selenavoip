package com.synapes.selenvoip

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.SharedPreferences
import android.content.UriMatcher
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import java.lang.reflect.Type

class SharedPrefsProvider : ContentProvider() {
    private lateinit var sharedPrefs: SharedPreferences
    private lateinit var encryptedSharedPreferences: SharedPreferences
    private lateinit var gson: Gson
    private var encryptionHelper: EncryptionHelper? = null

    private val uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
        addURI(AUTHORITY, "prefs", PREFS_URI)
    }


    override fun onCreate(): Boolean {
        val currentContext = context ?: return false

        // Initialize regular SharedPreferences
        sharedPrefs = currentContext.getSharedPreferences(PREFS_FILENAME, Context.MODE_PRIVATE)

        // Initialize EncryptedSharedPreferences
        encryptedSharedPreferences = initializeEncryptedSharedPreferences(currentContext)

        // Initialize Gson
        gson = Gson()

        // Migrate data from regular SharedPreferences to EncryptedSharedPreferences
        migrateFrom(sharedPrefs, currentContext)

        instance = this
        Log.d(
            TAG,
            "SharedPrefsProvider onCreate, sharedPrefs and encryptedSharedPreferences initialized"
        )
        return true
    }

    private fun initializeEncryptedSharedPreferences(context: Context): SharedPreferences {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        return EncryptedSharedPreferences.create(
            context,
            PREFS_ENCRYPTED_FILE_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }


    private fun migrateFrom(prefs: SharedPreferences, context: Context) {
        Log.d(TAG, "Starting migration from SharedPreferences to EncryptedSharedPreferences")

        // Migrate accounts
        val accounts: MutableList<SipAccountData> =
            getAccounts(prefs.getString(PREFS_KEY_ACCOUNTS, "").orEmpty())
        if (accounts.isNotEmpty()) {
            if (isEncryptionEnabled()) {
                initCrypto(context, getAlias())
                encryptedSharedPreferences
                    .edit()
                    .putString(
                        PREFS_KEY_ACCOUNTS,
                        gson.toJson(getDecryptedConfiguredAccounts(accounts))
                    )
                    .apply()
            } else {
                encryptedSharedPreferences
                    .edit()
                    .putString(PREFS_KEY_ACCOUNTS, gson.toJson(accounts))
                    .apply()
            }
        }

        // Migrate SIP server certificate verification setting
        encryptedSharedPreferences
            .edit()
            .putBoolean(
                PREFS_KEY_VERIFY_SIP_SERVER_CERT,
                prefs.getBoolean(PREFS_KEY_VERIFY_SIP_SERVER_CERT, false)
            ).apply()

        // Remove migrated data from old SharedPreferences
        prefs.edit()
            .remove(PREFS_KEY_ACCOUNTS)
            .remove(PREFS_KEY_ENCRYPTION_ENABLED)
            .remove(PREFS_KEY_KEYSTORE_ALIAS)
            .remove(PREFS_KEY_VERIFY_SIP_SERVER_CERT)
            .apply()

        Log.d(TAG, "Migration complete.")
    }

    override fun query(
        uri: Uri,
        projection: Array<out String>?,
        selection: String?,
        selectionArgs: Array<out String>?,
        sortOrder: String?
    ): Cursor? {
//        Log.d(TAG, "query called with URI: $uri, selection: $selection")
        return when (uriMatcher.match(uri)) {
            PREFS_URI -> {
                val cursor = MatrixCursor(arrayOf("key", "value"))
                if (selection != null && selectionArgs != null) {
                    val key = selectionArgs[0]
                    val value = sharedPrefs.getString(key, null)
//                    Log.d(TAG, "Query for key: $key, value: $value")
                    if (value != null) {
                        cursor.addRow(arrayOf(key, value))
                    }
                } else {
                    sharedPrefs.all.forEach { (key, value) ->
                        cursor.addRow(arrayOf(key, value.toString()))
//                        Log.d(TAG, "Query all - key: $key, value: $value")
                    }
                }
                cursor
            }

            else -> null
        }
    }

    override fun insert(uri: Uri, values: ContentValues?): Uri? {
//        Log.d(TAG, "insert called with URI: $uri")
        if (uriMatcher.match(uri) != PREFS_URI || values == null) {
//            Log.d(TAG, "insert: URI mismatch or null values")
            return null
        }
        val key = values.getAsString("key")
        val value = values.getAsString("value")
        if (key != null && value != null) {
//            Log.d(TAG, "Inserting key: $key, value: $value")
            sharedPrefs.edit().putString(key, value).apply()
            context?.contentResolver?.notifyChange(uri, null)
            val resultUri = Uri.withAppendedPath(uri, key)
//            Log.d(TAG, "Insert successful, result URI: $resultUri")
            return resultUri
        }
//        Log.d(TAG, "Insert failed: key or value is null")
        return null
    }

    override fun delete(uri: Uri, selection: String?, selectionArgs: Array<out String>?): Int {
        if (uriMatcher.match(uri) != PREFS_URI) return 0
        val editor = sharedPrefs.edit()
        var deletedCount = 0
        if (selection != null) {
            editor.remove(selection)
            deletedCount++
        } else {
            deletedCount = sharedPrefs.all.size
            editor.clear()
        }
        editor.apply()
        context?.contentResolver?.notifyChange(uri, null)
        return deletedCount
    }


    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<out String>?
    ): Int {
        if (uriMatcher.match(uri) != PREFS_URI || values == null) return 0
        val editor = sharedPrefs.edit()
        var updatedCount = 0
        if (selection != null) {
            val value = values.getAsString("value")
            if (value != null) {
                editor.putString(selection, value)
                updatedCount++
            }
        } else {
            for ((key, value) in values.valueSet()) {
                editor.putString(key.toString(), value.toString())
                updatedCount++
            }
        }
        editor.apply()
        context?.contentResolver?.notifyChange(uri, null)
        return updatedCount
    }

    override fun getType(uri: Uri): String? {
        return when (uriMatcher.match(uri)) {
            PREFS_URI -> "vnd.android.cursor.dir/vnd.$AUTHORITY.prefs"
            else -> null
        }
    }

    fun retrieveConfiguredAccounts(): MutableList<SipAccountData> {
        val accounts: String =
            encryptedSharedPreferences.getString(PREFS_KEY_ACCOUNTS, "").orEmpty()
        return getAccounts(accounts)
    }

    fun persistConfiguredAccounts(accounts: List<SipAccountData>) {
        val accountsJson = gson.toJson(accounts)
        encryptedSharedPreferences.edit().putString(PREFS_KEY_ACCOUNTS, accountsJson).apply()
    }

    private fun getAccounts(accounts: String): MutableList<SipAccountData> {
        if (accounts.isBlank() || accounts == "[]") {
            return mutableListOf()
        }

        return try {
            val listType = object : TypeToken<List<SipAccountData>>() {}.type
            gson.fromJson(accounts, listType)
        } catch (e: JsonSyntaxException) {
            Log.e(TAG, "Error parsing accounts JSON", e)
            mutableListOf()
        }
    }

    @Synchronized
    private fun isEncryptionEnabled(): Boolean {
        return sharedPrefs.getBoolean(PREFS_KEY_ENCRYPTION_ENABLED, false)
    }

    private fun setAlias(alias: String?) {
        sharedPrefs.edit().putString(PREFS_KEY_KEYSTORE_ALIAS, alias).apply()
    }

    private fun getAlias(): String {
        return sharedPrefs.getString(PREFS_KEY_KEYSTORE_ALIAS, "").toString()
    }

    private fun initCrypto(context: Context, alias: String?) {
        Crypto.init(context, alias.toString(), false)
        encryptionHelper = EncryptionHelper.Companion.getInstance()
    }

    @Synchronized
    private fun getDecryptedConfiguredAccounts(accounts: MutableList<SipAccountData>): MutableList<SipAccountData> {
        for (i in accounts.indices) {
            accounts[i].setUsername(decrypt(accounts[i].username))
            accounts[i].setPassword(decrypt(accounts[i].password))
        }
        return accounts
    }

    private fun decrypt(data: String?): String? {
        try {
            return encryptionHelper?.decrypt(data)
        } catch (e: Exception) {
            Log.e(TAG, "Error while deciphering the string", e)
            return null
        }
    }

    fun retrieveConfiguredCodecPriorities(): ArrayList<CodecPriority>? {
        val codecPriorities: String =
            sharedPrefs.getString(PREFS_KEY_CODEC_PRIORITIES, "").toString()
        if (codecPriorities.isEmpty()) return null

        val listType: Type? = object : TypeToken<ArrayList<CodecPriority?>?>() {}.type
        return gson.fromJson(codecPriorities, listType)
    }

    fun persistConfiguredCodecPriorities(codecPriorities: ArrayList<CodecPriority?>?) {
        sharedPrefs.edit().putString(PREFS_KEY_CODEC_PRIORITIES, gson.toJson(codecPriorities))
            .apply()
    }


    fun isDND(): Boolean {
        return sharedPrefs.getBoolean(PREFS_KEY_DND, false)
    }

    fun setDND(dnd: Boolean) {
        sharedPrefs.edit().putBoolean(PREFS_KEY_DND, dnd).apply()
    }

    fun setVerifySipServerCert(verify: Boolean) {
        sharedPrefs.edit().putBoolean(PREFS_KEY_VERIFY_SIP_SERVER_CERT, verify).apply()
    }

    fun isVerifySipServerCert(): Boolean {
        return sharedPrefs.getBoolean(PREFS_KEY_VERIFY_SIP_SERVER_CERT, false)
    }


    companion object {
        private val TAG = "**SHARED PREFS**"
        private val AUTHORITY = "com.synapes.voip.provider"
        val CONTENT_URI: Uri = Uri.parse("content://$AUTHORITY/prefs")

        private const val PREFS_URI = 1
        private val PREFS_FILENAME = "selen_shared_preferences"
        private val PREFS_ENCRYPTED_FILE_NAME = "encrypted_selen_shared_preferences"
        private val PREFS_KEY_ACCOUNTS = "accounts"
        private val PREFS_KEY_CODEC_PRIORITIES = "codec_priorities"
        private val PREFS_KEY_DND = "dnd_pref"
        private val PREFS_KEY_VERIFY_SIP_SERVER_CERT = "sip_server_cert_verification_enabled"
        private val PREFS_KEY_ENCRYPTION_ENABLED = "encryption_enabled"
        private val PREFS_KEY_KEYSTORE_ALIAS = "keystore_alias"

        private const val KEY_EXTENSION_NUMBER = "extension_number"
        private const val KEY_EXTENSION_PASSWORD = "extension_password"
        private const val KEY_DESTINATION_NUMBER = "destination_number"
        private const val KEY_LAT = "lat"
        private const val KEY_LON = "lon"
        private const val KEY_IS_ONLINE = "is_online"
        private const val KEY_MISSING_FIELDS = "missing_fields"
        private const val KEY_NEEDS_ATTENTION = "needs_attention"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_COMPANY_NAME = "company_name"
        private const val KEY_LOCATION_DESCRIPTION = "location_description"


//        fun updateDeviceInfo(context: Context, deviceInfo: DeviceInfo) {
//            Log.d(TAG, "Updating device info for device ID: ${deviceInfo.device_id}")

//            val currentExtension = getFromSharedPreferences(context, KEY_EXTENSION_NUMBER)
//            val currentExtensionPassword = getFromSharedPreferences(context, KEY_EXTENSION_PASSWORD)
//            val currentDestination = getFromSharedPreferences(context, KEY_DESTINATION_NUMBER)

//            Log.d(
//                TAG,
//                "Current values - Extension: $currentExtension, Destination: $currentDestination"
//            )
//            Log.d(
//                TAG,
//                "New values - Extension: ${deviceInfo.extension_number}, Destination: ${deviceInfo.destination_number}"
//            )

//            if (deviceInfo.extension_number != currentExtension) {
//                Log.d(TAG, "Updating extension number")
//                saveToSharedPreferences(context, KEY_EXTENSION_NUMBER, deviceInfo.extension_number)
//            }
//            if (deviceInfo.extension_password != currentExtensionPassword) {
//                saveToSharedPreferences(
//                    context,
//                    KEY_EXTENSION_PASSWORD,
//                    deviceInfo.extension_password
//                )
//            }
//            if (deviceInfo.destination_number != currentDestination) {
//                Log.d(TAG, "Updating destination number")
//                saveToSharedPreferences(
//                    context,
//                    KEY_DESTINATION_NUMBER,
//                    deviceInfo.destination_number
//                )
//            }
//
//            saveToSharedPreferences(context, KEY_LAT, deviceInfo.lat)
//            saveToSharedPreferences(context, KEY_LON, deviceInfo.lon)
//            saveToSharedPreferences(context, KEY_IS_ONLINE, deviceInfo.is_online.toString())
//            saveToSharedPreferences(
//                context,
//                KEY_MISSING_FIELDS,
//                deviceInfo.missing_fields.joinToString(",")
//            )
//            saveToSharedPreferences(
//                context,
//                KEY_NEEDS_ATTENTION,
//                deviceInfo.needs_attention.toString()
//            )
//            saveToSharedPreferences(context, KEY_PHONE_NUMBER, deviceInfo.phone_number)
//            saveToSharedPreferences(context, KEY_COMPANY_NAME, deviceInfo.company_name)
//            saveToSharedPreferences(
//                context,
//                KEY_LOCATION_DESCRIPTION,
//                deviceInfo.location_description
//            )
//        }
//
//        fun getDeviceInfoFromSharedPreferences(context: Context): DeviceInfo? {
//            val extension = getFromSharedPreferences(context, KEY_EXTENSION_NUMBER)
//            val extensionPassword = getFromSharedPreferences(context, KEY_EXTENSION_PASSWORD)
//            val destination = getFromSharedPreferences(context, KEY_DESTINATION_NUMBER)
//            val lat = getFromSharedPreferences(context, KEY_LAT)
//            val lon = getFromSharedPreferences(context, KEY_LON)
//            val isOnline = getFromSharedPreferences(context, KEY_IS_ONLINE).toBoolean() == true
//            val missingFields =
//                getFromSharedPreferences(context, KEY_MISSING_FIELDS)?.split(",") ?: emptyList()
//            val needsAttention =
//                getFromSharedPreferences(context, KEY_NEEDS_ATTENTION)?.toBoolean() == true
//            val phoneNumber = getFromSharedPreferences(context, "phone_number") ?: ""
//            val companyName = getFromSharedPreferences(context, "company_name") ?: ""
//            val locationDescription =
//                getFromSharedPreferences(context, KEY_LOCATION_DESCRIPTION) ?: ""

//            Log.d(TAG, "Retrieved values - Extension: $extension, Destination: $destination")

//            return if (extension != null && destination != null) {
//                DeviceInfo(
//                    device_id = Utils.DeviceUtils.getDeviceId(context),
//                    extension_number = extension.toString(),
//                    extension_password = extensionPassword.toString(),
//                    destination_number = destination.toString(),
//                    phone_number = phoneNumber.toString(),
//                    company_name = companyName.toString(),
//                    location_description = locationDescription.toString(),
//                    lat = lat ?: "",
//                    lon = lon ?: "",
//                    is_online = isOnline,
//                    missing_fields = missingFields,
//                    needs_attention = needsAttention
//                )
//            } else {
//                null
//            }
//        }

//        private fun saveToSharedPreferences(context: Context, key: String, value: String?) {
//            Log.d(TAG, "Saving to SharedPreferences - Key: $key, Value: $value")
//            val values = ContentValues().apply {
//                put("key", key)
//                put("value", value)
//            }
//            try {
//                val uri = context.contentResolver.insert(CONTENT_URI, values)
//                Log.d(TAG, "Insert result URI: $uri")
//            } catch (e: Exception) {
//                Log.e(TAG, "Error saving to SharedPreferences", e)
//            }
//        }
//
//        private fun getFromSharedPreferences(context: Context, key: String): String? {
//            Log.d(TAG, "Getting from SharedPreferences - Key: $key")
//            val projection = arrayOf("value")
//            val selection = "key = ?"
//            val selectionArgs = arrayOf(key)
//
//            try {
//                context.contentResolver.query(
//                    CONTENT_URI,
//                    projection,
//                    selection,
//                    selectionArgs,
//                    null
//                )?.use { cursor ->
//                    if (cursor.moveToFirst()) {
//                        val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
//                        Log.d(TAG, "Retrieved value for key $key: $value")
//                        return value
//                    }
//                }
//            } catch (e: Exception) {
//                Log.e(TAG, "Error getting from SharedPreferences", e)
//            }
//            Log.d(TAG, "No value found for key: $key")
//            return null
//        }

//        fun dumpSharedPreferences(context: Context) {
//            Log.d(TAG, "Dumping all SharedPreferences content")
//            context.contentResolver.query(CONTENT_URI, null, null, null, null)?.use { cursor ->
//                while (cursor.moveToNext()) {
//                    val key = cursor.getString(cursor.getColumnIndexOrThrow("key"))
//                    val value = cursor.getString(cursor.getColumnIndexOrThrow("value"))
//                    Log.d(TAG, "Key: $key, Value: $value")
//                }
//            }
//        }

        @Volatile
        private var instance: SharedPrefsProvider? = null

        @JvmStatic
        fun getInstance(context: Context): SharedPrefsProvider {
            return instance ?: synchronized(this) {
                instance ?: run {
                    // If instance is null, wait for ContentProvider to be initialized
                    context.contentResolver.query(CONTENT_URI, null, null, null, null)?.close()
                    instance ?: throw IllegalStateException("SharedPrefsProvider not initialized")
                }
            }
        }

    }

}
