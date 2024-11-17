package com.synapes.selenvoip

import android.os.Parcel
import android.os.Parcelable
import kotlin.math.max

class CodecPriority : Parcelable, Comparable<CodecPriority?> {
    private val mCodecId: String?
    private var mPriority: Int

    internal constructor(codecId: String?, priority: Short) {
        mCodecId = codecId
        mPriority = priority.toInt()
    }

    private constructor(`in`: Parcel) {
        mCodecId = `in`.readString()
        mPriority = `in`.readInt()
    }

    override fun writeToParcel(parcel: Parcel, arg1: Int) {
        parcel.writeString(mCodecId)
        parcel.writeInt(mPriority)
    }

    override fun describeContents(): Int {
        return 0
    }

    fun getCodecId(): String? {
        return mCodecId
    }

    fun getPriority(): Int {
        return mPriority
    }

    fun setPriority(mPriority: Int) {
        if (mPriority > PRIORITY_MAX) {
            this.mPriority = PRIORITY_MAX
        } else this.mPriority = max(mPriority, PRIORITY_DISABLED)
    }

    fun getCodecName(): String {
        val name = mCodecId!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]

        return when (name) {
            "G729" -> G729_LABEL
            "PCMU" -> PCMU_LABEL
            "PCMA" -> PCMA_LABEL
            "speex" -> SPEEX_LABEL
            "G722" -> G722_LABEL
            "G7221" -> G7221_LABEL
            "opus" -> OPUS_LABEL
            else -> name
        }
    }

    fun getCodecSampleRateInKhz(): Int {
        return mCodecId!!.split("/".toRegex()).dropLastWhile { it.isEmpty() }
            .toTypedArray()[1].toInt() / 1000
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) return true
        if (o == null || javaClass != o.javaClass) return false

        val that = o as CodecPriority

        return mCodecId == that.mCodecId
    }

    override fun hashCode(): Int {
        return mCodecId.hashCode()
    }

    override fun toString(): String {
        return "CodecID: $mCodecId, Priority: $mPriority"
    }

    override fun compareTo(another: CodecPriority?): Int {
        if (another == null) return -1

        if (mPriority == another.mPriority) return 0

        return if ((mPriority > another.mPriority)) -1 else 1
    }

    companion object {
        const val PRIORITY_MAX: Int = 254
        const val PRIORITY_MAX_VIDEO: Int = 128
        const val PRIORITY_MIN: Int = 1
        const val PRIORITY_DISABLED: Int = 0

        private const val G729_LABEL = "G.729"
        private const val PCMU_LABEL = "PCMU"
        private const val PCMA_LABEL = "PCMA"
        private const val SPEEX_LABEL = "Speex"
        private const val G722_LABEL = "G.722"
        private const val G7221_LABEL = "G.722.1"
        private const val OPUS_LABEL = "Opus"

        // This is used to regenerate the object.
        // All Parcelables must have a CREATOR that implements these two methods
        @JvmField
        val CREATOR: Parcelable.Creator<CodecPriority?> =
            object : Parcelable.Creator<CodecPriority?> {
                override fun createFromParcel(`in`: Parcel): CodecPriority {
                    return CodecPriority(`in`)
                }

                override fun newArray(size: Int): Array<CodecPriority?> {
                    return arrayOfNulls<CodecPriority>(size)
                }
            }
    }
}
