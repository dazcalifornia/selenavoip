package com.synapes.selenvoip

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.synapes.selenvoip.databinding.ActivityMainBinding
import com.synapes.selenvoip.databinding.DialogLoginBinding
import com.synapes.selenvoip.managers.BroadcastManager
import com.synapes.selenvoip.managers.NetworkManager
import com.synapes.selenvoip.managers.PermissionManager
import com.synapes.selenvoip.managers.SipManager

class MainActivity<EditText : View?> : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var networkManager: NetworkManager
    private lateinit var permissionManager: PermissionManager
    private lateinit var sipManager: SipManager
    private lateinit var broadcastManager: BroadcastManager


    init {
        System.loadLibrary("pjsua2")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        initializeManagers()
        setupCallButton()
        setupLoginButton()
        requestPermissions()
        updateLoginState()
    }

    private fun setupLoginButton() {
        binding.loginButton.setOnClickListener {
            if (sipManager.getRegistrationStatus()) {
                // If already logged in, perform logout
                sipManager.logout()
                updateLoginState()
            } else {
                showLoginDialog()
            }
        }
    }



    private fun showLoginDialog() {
        val dialogBinding = DialogLoginBinding.inflate(layoutInflater)

        val dialog = AlertDialog.Builder(this)
            .setTitle("SIP Login")
            .setView(dialogBinding.root)
            .setPositiveButton("Login", null)
            .setNegativeButton("Cancel") { dialog, _ -> dialog.dismiss() }
            .create()

        dialog.show()

        // Set default values
        dialogBinding.serverEditText.setText("synapes-pbx-poc-01.online")
        dialogBinding.usernameEditText.setText("933933")
        dialogBinding.passwordEditText.setText("933933")
        dialogBinding.portEditText.setText("5060")

        // Override positive button to handle validation
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val server = dialogBinding.serverEditText.text.toString()
            val username = dialogBinding.usernameEditText.text.toString()
            val password = dialogBinding.passwordEditText.text.toString()
            val port = dialogBinding.portEditText.text.toString()

            if (validateInputs(server, username, password, port)) {
                performLogin(server, username, password, port)
                dialog.dismiss()
            }
        }
    }

    private fun validateInputs(server: String, username: String, password: String, port: String): Boolean {
        if (server.isEmpty() || username.isEmpty() || password.isEmpty() || port.isEmpty()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    private fun performLogin(server: String, username: String, password: String, port: String) {
        try {
            Log.d("MainActivity", "Attempting login with: server=$server, username=$username, port=$port")
            sipManager.login(server, username, password, port)
            updateLoginState()
        } catch (e: Exception) {
            Log.e("MainActivity", "Login failed", e)
            Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }


    private fun updateLoginState() {
        val isLoggedIn = sipManager.getRegistrationStatus()
        binding.userInfoText.apply {
            visibility = if (isLoggedIn) View.VISIBLE else View.GONE
            text = if (isLoggedIn) "User: ${sipManager.getSipAccount()?.username}" else ""
        }
        binding.loginButton.text = if (isLoggedIn) "Logout" else "Login"
    }




    private fun initializeManagers() {
        networkManager = NetworkManager(this)
        permissionManager = PermissionManager(this)
        sipManager = SipManager(this)
        broadcastManager = BroadcastManager(this)
    }

    private fun setupCallButton() {
        binding.callButton.setOnClickListener {
            val destinationNumber = binding.destinationNumberEditText.text.toString()
            if (destinationNumber.isNotEmpty()) {
                Toast.makeText(this, "Making call to $destinationNumber", Toast.LENGTH_SHORT).show()
                sipManager.audioCall(destinationNumber)
            } else {
                Toast.makeText(this, "Please enter a destination number", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun requestPermissions() {
        permissionManager.requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        networkManager.onResume()
    }

    override fun onPause() {
        super.onPause()
        networkManager.onPause()
    }

    override fun onStart() {
        super.onStart()
        broadcastManager.onStart()
    }

    override fun onStop() {
        super.onStop()
        broadcastManager.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        broadcastManager.onDestroy()
        networkManager.onDestroy()
    }
}