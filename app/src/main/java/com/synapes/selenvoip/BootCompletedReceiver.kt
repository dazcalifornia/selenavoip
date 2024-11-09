package com.synapes.selenvoip

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class BootCompletedReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == Intent.ACTION_BOOT_COMPLETED) {
            // Start your MainActivity or perform any other actions
            val startActivityIntent = Intent(context, MainActivity::class.java)
            startActivityIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context?.startActivity(startActivityIntent)
        }
    }
}
