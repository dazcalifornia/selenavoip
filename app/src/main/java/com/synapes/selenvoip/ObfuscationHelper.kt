package com.synapes.selenvoip

import android.content.Context

class ObfuscationHelper {
    companion object {
        fun getValue(context: Context, string: String): String {
            return if (SharedPrefsProvider.getInstance(context).isObfuscationEnabled()) {
                obfuscate(string)
            } else {
                string
            }
        }

        private fun obfuscate(string: String): String {
            return if (string.length > 5) {
                repeat(string.length - 3) + string.substring(string.length - 3)
            } else {
                repeat(string.length - 1) + string.substring(string.length - 1)
            }
        }

        private fun repeat(n: Int): String {
            return "*".repeat(n)
        }
    }
}
