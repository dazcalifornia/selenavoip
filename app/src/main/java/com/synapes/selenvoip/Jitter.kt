package com.synapes.selenvoip

import android.os.Parcel
import android.os.Parcelable

@Suppress("unused")
class Jitter : Parcelable {
    private val max: Int
    private val mean: Int
    private val min: Int

    constructor(max: Int, mean: Int, min: Int) {
        this.max = max
        this.mean = mean
        this.min = min
    }

    private constructor(`in`: Parcel) {
        this.max = `in`.readInt()
        this.mean = `in`.readInt()
        this.min = `in`.readInt()
    }

    override fun writeToParcel(parcel: Parcel, arg1: Int) {
        parcel.writeInt(max)
        parcel.writeInt(mean)
        parcel.writeInt(min)
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun toString(): String {
        return "Max: $max Mean: $mean Min: $min"
    }

    fun getMax(): Int {
        return max
    }

    fun getMean(): Int {
        return mean
    }

    fun getMin(): Int {
        return min
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<Jitter> = object : Parcelable.Creator<Jitter> {
            override fun createFromParcel(`in`: Parcel): Jitter {
                return Jitter(`in`)
            }

            override fun newArray(size: Int): Array<Jitter?> {
                return arrayOfNulls<Jitter>(size)
            }
        }
    }
}
