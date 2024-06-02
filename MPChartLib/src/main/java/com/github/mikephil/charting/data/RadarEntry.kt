package com.github.mikephil.charting.data

import android.annotation.SuppressLint

/**
 * Created by philipp on 13/06/16.
 */
@SuppressLint("ParcelCreator")
class RadarEntry : Entry {
    constructor(value: Float) : super(0f, value)

    constructor(value: Float, data: Any?) : super(0f, value, data)

    val value: Float
        /**
         * This is the same as getY(). Returns the value of the RadarEntry.
         *
         * @return
         */
        get() = y

    override fun copy(): RadarEntry {
        val e = RadarEntry(y, data)
        return e
    }

    @Deprecated("")
    override fun setX(x: Float) {
        super.setX(x)
    }

    @Deprecated("")
    override fun getX(): Float {
        return super.getX()
    }
}
