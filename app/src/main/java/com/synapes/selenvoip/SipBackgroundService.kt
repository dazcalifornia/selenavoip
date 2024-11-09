package com.synapes.selenvoip

import android.app.Service
import android.content.Intent
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.os.PowerManager
import android.os.PowerManager.WakeLock

open class SelenBackgroundService : Service() {
    private var mHandler: Handler? = null
    private var mWakeLock: WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        mHandler = Handler(Looper.getMainLooper())
        acquireWakeLock()
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        releaseWakeLock()
    }

    protected fun enqueueJob(job: Runnable) {
        mHandler!!.post(job)
    }

    protected fun enqueueDelayedJob(job: Runnable, delayMillis: Long) {
        mHandler!!.postDelayed(job, delayMillis)
    }

    protected fun dequeueJob(job: Runnable) {
        mHandler!!.removeCallbacks(job)
    }

    fun acquireWakeLock() {
        val pm = getSystemService(POWER_SERVICE) as PowerManager
        mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, javaClass.getSimpleName())
        mWakeLock!!.acquire()
    }

    fun releaseWakeLock() {
        if (mWakeLock != null && mWakeLock!!.isHeld()) {
            mWakeLock!!.release()
        }
    }
}
