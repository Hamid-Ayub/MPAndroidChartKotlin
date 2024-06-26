package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.graphics.RectF
import android.graphics.Typeface
import android.util.AttributeSet
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.PieHighlighter
import com.github.mikephil.charting.renderer.PieChartRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import java.util.Objects
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * View that represents a pie chart. Draws cake like slices.
 *
 * @author Philipp Jahoda
 */
class PieChart : PieRadarChartBase<PieData?> {
    /**
     * returns the circlebox, the boundingbox of the pie-chart slices
     *
     * @return
     */
    /**
     * rect object that represents the bounds of the piechart, needed for
     * drawing the circle
     */
    val circleBox: RectF? = RectF()

    /**
     * Returns true if drawing the entry labels is enabled, false if not.
     *
     * @return
     */
    /**
     * flag indicating if entry labels should be drawn or not
     */
    var isDrawEntryLabelsEnabled: Boolean = true
        private set

    /**
     * returns an integer array of all the different angles the chart slices
     * have the angles in the returned array determine how much space (of 360°)
     * each slice takes
     *
     * @return
     */
    /**
     * array that holds the width of each pie-slice in degrees
     */
    var drawAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns the absolute angles of the different chart slices (where the
     * slices end)
     *
     * @return
     */
    /**
     * array that holds the absolute angle in degrees of each slice
     */
    var absoluteAngles: FloatArray = FloatArray(1)
        private set

    /**
     * returns true if the hole in the center of the pie-chart is set to be
     * visible, false if not
     *
     * @return
     */
    /**
     * set this to true to draw the pie center empty
     *
     * @param enabled
     */
    /**
     * if true, the white hole inside the chart will be drawn
     */
    var isDrawHoleEnabled: Boolean = true

    /**
     * Returns true if the inner tips of the slices are visible behind the hole,
     * false if not.
     *
     * @return true if slices are visible behind the hole.
     */
    /**
     * if true, the hole will see-through to the inner tips of the slices
     */
    var isDrawSlicesUnderHoleEnabled: Boolean = false
        private set

    /**
     * Returns true if using percentage values is enabled for the chart.
     *
     * @return
     */
    /**
     * if true, the values inside the piechart are drawn as percent values
     */
    var isUsePercentValuesEnabled: Boolean = false
        private set

    /**
     * Returns true if the chart is set to draw each end of a pie-slice
     * "rounded".
     *
     * @return
     */
    /**
     * if true, the slices of the piechart are rounded
     */
    var isDrawRoundedSlicesEnabled: Boolean = false
        private set

    /**
     * variable for the text that is drawn in the center of the pie-chart
     */
    private var mCenterText: CharSequence = ""

    private val mCenterTextOffset: MPPointF = MPPointF.getInstance(0f, 0f)

    /**
     * Returns the size of the hole radius in percent of the total radius.
     *
     * @return
     */
    /**
     * sets the radius of the hole in the center of the piechart in percent of
     * the maximum radius (max = the radius of the whole chart), default 50%
     *
     * @param percent
     */
    /**
     * indicates the size of the hole in the center of the piechart, default:
     * radius / 2
     */
    var holeRadius: Float = 50f

    /**
     * sets the radius of the transparent circle that is drawn next to the hole
     * in the piechart in percent of the maximum radius (max = the radius of the
     * whole chart), default 55% -> means 5% larger than the center-hole by
     * default
     *
     * @param percent
     */
    /**
     * the radius of the transparent circle next to the chart-hole in the center
     */
    var transparentCircleRadius: Float = 55f

    /**
     * returns true if drawing the center text is enabled
     *
     * @return
     */
    /**
     * if enabled, centertext is drawn
     */
    var isDrawCenterTextEnabled: Boolean = true
        private set

    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    /**
     * the rectangular radius of the bounding box for the center text, as a percentage of the pie
     * hole
     * default 1.f (100%)
     */
    var centerTextRadiusPercent: Float = 100f

    protected var mMaxAngle: Float = 360f

    /**
     * Minimum angle to draw slices, this only works if there is enough room for all slices to have
     * the minimum angle, default 0f.
     */
    private var mMinAngleForSlices = 0f

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = PieChartRenderer(this, animator, viewPortHandler)
        xAxis = null

        highlighter = PieHighlighter(this)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null) return

        mRenderer!!.drawData(canvas)

        if (valuesToHighlight()) mRenderer!!.drawHighlighted(canvas, highlighted)

        mRenderer!!.drawExtras(canvas)

        mRenderer!!.drawValues(canvas)

        legendRenderer!!.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)
    }

    override fun calculateOffsets() {
        super.calculateOffsets()

        // prevent nullpointer when no data set
        if (mData == null) return

        val diameter = diameter
        val radius = diameter / 2f

        val c = centerOffsets

        val shift = mData!!.dataSet?.selectionShift

        // create the circle box that will contain the pie-chart (the bounds of
        // the pie-chart)
        circleBox!![c.x - radius + shift!!, c.y - radius + shift, c.x + radius - shift] = c.y + radius - shift

        MPPointF.recycleInstance(c)
    }

    override fun calcMinMax() {
        calcAngles()
    }

    override fun getMarkerPosition(highlight: Highlight?): FloatArray {
        val center = centerCircleBox
        var r = radius

        var off = r / 10f * 3.6f

        if (isDrawHoleEnabled) {
            off = (r - (r / 100f * holeRadius)) / 2f
        }

        r -= off // offset to keep things inside the chart

        val rotationAngle = rotationAngle

        val entryIndex = highlight!!.x.toInt()

        // offset needed to center the drawn text in the slice
        val offset = drawAngles[entryIndex] / 2

        // calculate the text position
        val x = (r
                * cos(Math.toRadians(((rotationAngle + absoluteAngles[entryIndex] - offset)
                * Objects.requireNonNull(animator)!!.phaseY).toDouble())) + center.x).toFloat()
        val y = (r
                * sin(Math.toRadians(((rotationAngle + absoluteAngles[entryIndex] - offset)
                * animator!!.phaseY).toDouble())) + center.y).toFloat()

        MPPointF.recycleInstance(center)
        return floatArrayOf(x, y)
    }

    /**
     * calculates the needed angles for the chart slices
     */
    private fun calcAngles() {
        val entryCount = mData!!.entryCount

        if (drawAngles.size != entryCount) {
            drawAngles = FloatArray(entryCount)
        } else {
            for (i in 0 until entryCount) {
                drawAngles[i] = 0f
            }
        }
        if (absoluteAngles.size != entryCount) {
            absoluteAngles = FloatArray(entryCount)
        } else {
            for (i in 0 until entryCount) {
                absoluteAngles[i] = 0f
            }
        }

        val yValueSum = mData!!.yValueSum

        val dataSets = mData!!.dataSets

        val hasMinAngle = mMinAngleForSlices != 0f && entryCount * mMinAngleForSlices <= mMaxAngle
        val minAngles = FloatArray(entryCount)

        var cnt = 0
        var offset = 0f
        var diff = 0f

        for (i in 0 until mData!!.dataSetCount) {
            val set = dataSets[i]

            for (j in 0 until set.entryCount) {
                val drawAngle = calcAngle(abs(set.getEntryForIndex(j).y.toDouble()).toFloat(), yValueSum)

                if (hasMinAngle) {
                    val temp = drawAngle - mMinAngleForSlices
                    if (temp <= 0) {
                        minAngles[cnt] = mMinAngleForSlices
                        offset += -temp
                    } else {
                        minAngles[cnt] = drawAngle
                        diff += temp
                    }
                }

                drawAngles[cnt] = drawAngle

                if (cnt == 0) {
                    absoluteAngles[cnt] = drawAngles[cnt]
                } else {
                    absoluteAngles[cnt] = absoluteAngles[cnt - 1] + drawAngles[cnt]
                }

                cnt++
            }
        }

        if (hasMinAngle) {
            // Correct bigger slices by relatively reducing their angles based on the total angle needed to subtract
            // This requires that `entryCount * mMinAngleForSlices <= mMaxAngle` be true to properly work!
            for (i in 0 until entryCount) {
                minAngles[i] -= (minAngles[i] - mMinAngleForSlices) / diff * offset
                if (i == 0) {
                    absoluteAngles[0] = minAngles[0]
                } else {
                    absoluteAngles[i] = absoluteAngles[i - 1] + minAngles[i]
                }
            }

            drawAngles = minAngles
        }
    }

    /**
     * Checks if the given index is set to be highlighted.
     *
     * @param index
     * @return
     */
    fun needsHighlight(index: Int): Boolean {
        // no highlight

        if (!valuesToHighlight()) return false

        for (i in highlighted!!.indices)  // check if the xvalue for the given dataset needs highlight
            if (highlighted!![i]!!.x.toInt() == index) return true

        return false
    }

    /**
     * calculates the needed angle for a given value
     *
     * @param value
     * @param yValueSum
     * @return
     */
    /**
     * calculates the needed angle for a given value
     *
     * @param value
     * @return
     */
    private fun calcAngle(value: Float, yValueSum: Float = mData!!.yValueSum): Float {
        return value / yValueSum * mMaxAngle
    }

    @get:Deprecated("")
    override var xAxis: XAxis?
        /**
         * This will throw an exception, PieChart has no XAxis object.
         *
         * @return
         */
        get() {
            throw RuntimeException("PieChart has no XAxis")
        }
        set(xAxis) {
            super.xAxis = xAxis
        }

    override fun getIndexForAngle(angle: Float): Int {
        // take the current angle of the chart into consideration

        val a = Utils.getNormalizedAngle(angle - rotationAngle)

        for (i in absoluteAngles.indices) {
            if (absoluteAngles[i] > a) return i
        }

        return -1 // return -1 if no index found
    }

    /**
     * Returns the index of the DataSet this x-index belongs to.
     *
     * @param xIndex
     * @return
     */
    fun getDataSetIndexForIndex(xIndex: Int): Int {
        val dataSets = mData!!.dataSets

        for (i in dataSets.indices) {
            if (dataSets[i].getEntryForXValue(xIndex.toFloat(), Float.NaN) != null) return i
        }

        return -1
    }

    /**
     * Sets the color for the hole that is drawn in the center of the PieChart
     * (if enabled).
     *
     * @param color
     */
    fun setHoleColor(color: Int) {
        (mRenderer as PieChartRenderer).paintHole.color = color
    }

    /**
     * Enable or disable the visibility of the inner tips of the slices behind the hole
     */
    fun setDrawSlicesUnderHole(enable: Boolean) {
        isDrawSlicesUnderHoleEnabled = enable
    }

    var centerText: CharSequence?
        /**
         * returns the text that is drawn in the center of the pie-chart
         *
         * @return
         */
        get() = mCenterText
        /**
         * Sets the text String that is displayed in the center of the PieChart.
         *
         * @param text
         */
        set(text) {
            mCenterText = text ?: ""
        }

    /**
     * set this to true to draw the text that is displayed in the center of the
     * pie chart
     *
     * @param enabled
     */
    fun setDrawCenterText(enabled: Boolean) {
        this.isDrawCenterTextEnabled = enabled
    }

    override val requiredLegendOffset: Float
        get() = legendRenderer!!.labelPaint.textSize * 2f

    override val requiredBaseOffset: Float
        get() = 0f

    override val radius: Float
        get() = if (circleBox == null) 0f
        else min((circleBox.width() / 2f).toDouble(), (circleBox.height() / 2f).toDouble()).toFloat()

    val centerCircleBox: MPPointF
        /**
         * returns the center of the circlebox
         *
         * @return
         */
        get() = MPPointF.getInstance(circleBox!!.centerX(), circleBox.centerY())

    /**
     * sets the typeface for the center-text paint
     *
     * @param t
     */
    fun setCenterTextTypeface(t: Typeface?) {
        (mRenderer as PieChartRenderer).paintCenterText.setTypeface(t)
    }

    /**
     * Sets the size of the center text of the PieChart in dp.
     *
     * @param sizeDp
     */
    fun setCenterTextSize(sizeDp: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.textSize = Utils.convertDpToPixel(sizeDp)
    }

    /**
     * Sets the size of the center text of the PieChart in pixels.
     *
     * @param sizePixels
     */
    fun setCenterTextSizePixels(sizePixels: Float) {
        (mRenderer as PieChartRenderer).paintCenterText.textSize = sizePixels
    }

    /**
     * Sets the offset the center text should have from it's original position in dp. Default x = 0, y = 0
     *
     * @param x
     * @param y
     */
    fun setCenterTextOffset(x: Float, y: Float) {
        mCenterTextOffset.x = Utils.convertDpToPixel(x)
        mCenterTextOffset.y = Utils.convertDpToPixel(y)
    }

    val centerTextOffset: MPPointF
        /**
         * Returns the offset on the x- and y-axis the center text has in dp.
         *
         * @return
         */
        get() = MPPointF.getInstance(mCenterTextOffset.x, mCenterTextOffset.y)

    /**
     * Sets the color of the center text of the PieChart.
     *
     * @param color
     */
    fun setCenterTextColor(color: Int) {
        (mRenderer as PieChartRenderer).paintCenterText.color = color
    }

    /**
     * Sets the color the transparent-circle should have.
     *
     * @param color
     */
    fun setTransparentCircleColor(color: Int) {
        val p = (mRenderer as PieChartRenderer).paintTransparentCircle
        val alpha = p.alpha
        p.color = color
        p.alpha = alpha
    }

    /**
     * Sets the amount of transparency the transparent circle should have 0 = fully transparent,
     * 255 = fully opaque.
     * Default value is 100.
     *
     * @param alpha 0-255
     */
    fun setTransparentCircleAlpha(alpha: Int) {
        (mRenderer as PieChartRenderer).paintTransparentCircle.alpha = alpha
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     * Deprecated -> use setDrawEntryLabels(...) instead.
     *
     * @param enabled
     */
    @Deprecated("")
    fun setDrawSliceText(enabled: Boolean) {
        isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Set this to true to draw the entry labels into the pie slices (Provided by the getLabel() method of the PieEntry class).
     *
     * @param enabled
     */
    fun setDrawEntryLabels(enabled: Boolean) {
        isDrawEntryLabelsEnabled = enabled
    }

    /**
     * Sets the color the entry labels are drawn with.
     *
     * @param color
     */
    fun setEntryLabelColor(color: Int) {
        (mRenderer as PieChartRenderer).paintEntryLabels.color = color
    }

    /**
     * Sets a custom Typeface for the drawing of the entry labels.
     *
     * @param tf
     */
    fun setEntryLabelTypeface(tf: Typeface?) {
        (mRenderer as PieChartRenderer).paintEntryLabels.setTypeface(tf)
    }

    /**
     * Sets the size of the entry labels in dp. Default: 13dp
     *
     * @param size
     */
    fun setEntryLabelTextSize(size: Float) {
        (mRenderer as PieChartRenderer).paintEntryLabels.textSize = Utils.convertDpToPixel(size)
    }

    /**
     * Sets whether to draw slices in a curved fashion, only works if drawing the hole is enabled
     * and if the slices are not drawn under the hole.
     *
     * @param enabled draw curved ends of slices
     */
    fun setDrawRoundedSlices(enabled: Boolean) {
        isDrawRoundedSlicesEnabled = enabled
    }

    /**
     * If this is enabled, values inside the PieChart are drawn in percent and
     * not with their original value. Values provided for the IValueFormatter to
     * format are then provided in percent.
     *
     * @param enabled
     */
    fun setUsePercentValues(enabled: Boolean) {
        isUsePercentValuesEnabled = enabled
    }

    var maxAngle: Float
        get() = mMaxAngle
        /**
         * Sets the max angle that is used for calculating the pie-circle. 360f means
         * it's a full PieChart, 180f results in a half-pie-chart. Default: 360f
         *
         * @param maxangle min 90, max 360
         */
        set(maxangle) {
            var maxangle = maxangle
            if (maxangle > 360) maxangle = 360f

            if (maxangle < 90) maxangle = 90f

            this.mMaxAngle = maxangle
        }

    var minAngleForSlices: Float
        /**
         * The minimum angle slices on the chart are rendered with, default is 0f.
         *
         * @return minimum angle for slices
         */
        get() = mMinAngleForSlices
        /**
         * Set the angle to set minimum size for slices, you must call [.notifyDataSetChanged]
         * and [.invalidate] when changing this, only works if there is enough room for all
         * slices to have the minimum angle.
         *
         * @param minAngle minimum 0, maximum is half of [.setMaxAngle]
         */
        set(minAngle) {
            var minAngle = minAngle
            if (minAngle > (mMaxAngle / 2f)) minAngle = mMaxAngle / 2f
            else if (minAngle < 0) minAngle = 0f

            this.mMinAngleForSlices = minAngle
        }

    override fun onDetachedFromWindow() {
        // releases the bitmap in the renderer to avoid oom error
        if (mRenderer != null && mRenderer is PieChartRenderer) {
            (mRenderer as PieChartRenderer).releaseBitmap()
        }
        super.onDetachedFromWindow()
    }
}
