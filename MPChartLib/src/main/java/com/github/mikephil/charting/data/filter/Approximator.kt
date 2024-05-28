package com.github.mikephil.charting.data.filter

import android.annotation.TargetApi
import android.os.Build
import java.util.Arrays
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Implemented according to Wiki-Pseudocode []
 * http://en.wikipedia.org/wiki/Ramer�Douglas�Peucker_algorithm
 *
 * @author Philipp Baldauf & Phliipp Jahoda
 */
class Approximator {
    @TargetApi(Build.VERSION_CODES.GINGERBREAD)
    fun reduceWithDouglasPeucker(points: FloatArray, tolerance: Float): FloatArray {
        var greatestIndex = 0
        var greatestDistance = 0f

        val line: Line = Line(points[0], points[1], points[points.size - 2], points[points.size - 1])

        var i = 2
        while (i < points.size - 2) {
            val distance = line.distance(points[i], points[i + 1])

            if (distance > greatestDistance) {
                greatestDistance = distance
                greatestIndex = i
            }
            i += 2
        }

        if (greatestDistance > tolerance) {
            val reduced1 = reduceWithDouglasPeucker(Arrays.copyOfRange(points, 0, greatestIndex + 2), tolerance)
            val reduced2 = reduceWithDouglasPeucker(Arrays.copyOfRange(points, greatestIndex, points.size),
                    tolerance)

            val result1 = reduced1
            val result2 = Arrays.copyOfRange(reduced2, 2, reduced2.size)

            return concat(result1, result2)
        } else {
            return line.points
        }
    }

    /**
     * Combine arrays.
     *
     * @param arrays
     * @return
     */
    fun concat(vararg arrays: FloatArray): FloatArray {
        var length = 0
        for (array in arrays) {
            length += array.size
        }
        val result = FloatArray(length)
        var pos = 0
        for (array in arrays) {
            for (element in array) {
                result[pos] = element
                pos++
            }
        }
        return result
    }

    private inner class Line(x1: Float, y1: Float, x2: Float, y2: Float) {
        val points: FloatArray = floatArrayOf(x1, y1, x2, y2)

        private val sxey = x1 * y2
        private val exsy = x2 * y1

        private val dx = x1 - x2
        private val dy = y1 - y2

        private val length = sqrt((dx * dx + dy * dy).toDouble()).toFloat()

        fun distance(x: Float, y: Float): Float {
            return (abs((dy * x - dx * y + sxey - exsy).toDouble()) / length).toFloat()
        }
    }
}
