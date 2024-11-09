package com.synapes.selenvoip

import android.os.Parcel
import android.os.Parcelable

/**
 * sipservice
 *
 * Created by Vincenzo Esposito on 19/04/19.
 * Copyright © 2019 VoiSmart S.r.l. All rights reserved.
 */
@Suppress("unused")
class RtpStreamStats : Parcelable {
    private val pkt: Long
    private val discard: Long
    private val loss: Long
    private val reorder: Long
    private val dup: Long
    private val jitter: Jitter?

    internal constructor(
        pkt: Long,
        discard: Long,
        loss: Long,
        reorder: Long,
        dup: Long,
        jitter: Jitter?
    ) {
        this.pkt = pkt
        this.discard = discard
        this.loss = loss
        this.reorder = reorder
        this.dup = dup
        this.jitter = jitter
    }

    private constructor(`in`: Parcel) {
        this.pkt = `in`.readLong()
        this.discard = `in`.readLong()
        this.loss = `in`.readLong()
        this.reorder = `in`.readLong()
        this.dup = `in`.readLong()
        this.jitter = `in`.readParcelable(Jitter::class.java.classLoader)
    }


    override fun writeToParcel(parcel: Parcel, arg1: Int) {
        parcel.writeLong(pkt)
        parcel.writeLong(discard)
        parcel.writeLong(loss)
        parcel.writeLong(reorder)
        parcel.writeLong(dup)
        parcel.writeParcelable(jitter, 0)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return ("Packets: " + pkt + "\n"
                + "Discard: " + discard + "\n"
                + "Loss: " + loss + "\n"
                + "Reorder: " + reorder + "\n"
                + "Duplicate: " + dup + "\n"
                + "Jitter: " + jitter.toString() + "\n")
    }

    fun getPackets(): Long {
        return pkt
    }

    fun getDiscard(): Long {
        return discard
    }

    fun getLoss(): Long {
        return loss
    }

    fun getReorder(): Long {
        return reorder
    }

    fun getDup(): Long {
        return dup
    }

    fun getJitter(): Jitter? {
        return jitter
    }

    companion object {
        // This is used to regenerate the object.
        // All Parcelables must have a CREATOR that implements these two methods
        @JvmField
        val CREATOR: Parcelable.Creator<RtpStreamStats?> =
            object : Parcelable.Creator<RtpStreamStats?> {
                override fun createFromParcel(`in`: Parcel): RtpStreamStats {
                    return RtpStreamStats(`in`)
                }

                override fun newArray(size: Int): Array<RtpStreamStats?> {
                    return arrayOfNulls<RtpStreamStats>(size)
                }
            }
    }
}
