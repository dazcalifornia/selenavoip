package com.synapes.selenvoip

import android.util.Log
import org.pjsip.pjsua2.LogEntry
import org.pjsip.pjsua2.LogWriter
import org.pjsip.pjsua2.pj_log_decoration

class SipLogger : LogWriter() {
    override fun write(entry: LogEntry) {
        when (entry.level) {
            0, 1 -> Log.e("PJSIP " + entry.threadName, entry.msg)
            2 -> Log.w("PJSIP " + entry.threadName, entry.msg)
            3 -> Log.i("PJSIP " + entry.threadName, entry.msg)
            4 -> Log.d("PJSIP " + entry.threadName, entry.msg)
            else -> Log.d("PJSIP " + entry.threadName, entry.msg)
        }
    }

    /**
     * Change decor flags as needed
     * @return decor flags
     */
    fun getDecor(): Int {
        return (pj_log_decoration.PJ_LOG_HAS_CR
                or pj_log_decoration.PJ_LOG_HAS_INDENT
                or pj_log_decoration.PJ_LOG_HAS_SENDER)
    }
}
