package com.synapes.selenvoip

import org.pjsip.pjsua2.CallInfo
import java.util.regex.Pattern

class CallerInfo(callInfo: CallInfo) {
    private var displayName: String? = null
    private var remoteUri: String? = null

    init {
        val temp = callInfo.remoteUri

        if (temp == null || temp.isEmpty()) {
            remoteUri = UNKNOWN
            displayName = remoteUri
        }

        val displayNameAndRemoteUriPattern = Pattern.compile("^\"([^\"]+).*?sip:(.*?)>$")
        val completeInfo = displayNameAndRemoteUriPattern.matcher(temp)
        if (completeInfo.matches()) {
            displayName = completeInfo.group(1)
            remoteUri = completeInfo.group(2)
        } else {
            val remoteUriPattern = Pattern.compile("^.*?sip:(.*?)>$")
            val remoteUriInfo = remoteUriPattern.matcher(temp)
            if (remoteUriInfo.matches()) {
                remoteUri = remoteUriInfo.group(1)
                displayName = remoteUri
            } else {
                remoteUri = UNKNOWN
                displayName = remoteUri
            }
        }
    }

    fun getDisplayName(): String? {
        return displayName
    }

    fun getRemoteUri(): String? {
        return remoteUri
    }

    companion object {
        private const val UNKNOWN = "Unknown"
    }
}
