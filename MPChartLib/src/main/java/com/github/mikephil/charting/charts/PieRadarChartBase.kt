package com.github.mikephil.charting.charts

import android.animation.ObjectAnimator
import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.github.mikephil.charting.animation.Easing.EasingFunction
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.PieRadarChartTouchListener
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt

/**
 * Baseclass of PieChart and RadarChart.
 *
 * @author Philipp Jahoda
 */
abstract class PieRadarChartBase<T : ChartData<out IDataSet<out Entry>?>?>
    : Chart<T> {
    /**
     * holds the normalized version of the current rotation angle of the chart
     */
    private var mRotationAngle = 270f

    /**
     * gets the raw version of the current rotation angle of the pie chart the
     * returned value could be any value, negative or positive, outside of the
     * 360 degrees. this is used when working with rotation direction, mainly by
     * gestures and animations.
     *
     * @return
     */
    /**
     * holds the raw version of the current rotation angle of the chart
     */
    var rawRotationAngle: Float = 270f
        private set

    /**
     * Returns true if rotation of the chart by touch is enabled, false if not.
     *
     * @return
     */
    /**
     * Set this to true to enable the rotation / spinning of the chart by touch.
     * Set it to false to disable it. Default: true
     *
     * @param enabled
     */
    /**
     * flag that indicates if rotation is enabled or not
     */
    var isRotationEnabled: Boolean = true

    /**
     * Gets the minimum offset (padding) around the chart, defaults to 0.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 0.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 0.f
     */
    var minOffset: Float = 0f

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        onTouchListener = PieRadarChartTouchListener(this)
    }

    override fun calcMinMax() {
        //mXAxis.mAxisRange = mData.getXVals().size() - 1;
    }

    override fun getMaxVisibleCount(): Int {
        return mData!!.entryCount
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        // use the pie- and radarchart listener own listener
        return if (mTouchEnabled && onTouchListener != null) onTouchListener!!.onTouch(this, event)
        else super.onTouchEvent(event)
    }

    override fun computeScroll() {
        if (onTouchListener is PieRadarChartTouchListener) (onTouchListener as PieRadarChartTouchListener).computeScroll()
    }

    override fun notifyDataSetChanged() {
        if (mData == null) return

        calcMinMax()

        if (legend != null) legendRenderer!!.computeLegend(mData)

        calculateOffsets()
    }

    public override fun calculateOffsets() {
        var legendLeft = 0f
        var legendRight = 0f
        var legendBottom = 0f
        var legendTop = 0f

        if (legend != null && legend!!.isEnabled && !legend!!.isDrawInsideEnabled) {
            val fullLegendWidth = min(legend!!.mNeededWidth.toDouble(),
                    (viewPortHandler.chartWidth * legend!!.maxSizePercent).toDouble()).toFloat()

            when (legend!!.orientation) {
                LegendOrientation.VERTICAL -> {
                    var xLegendOffset = 0f

                    if (legend!!.horizontalAlignment == LegendHorizontalAlignment.LEFT
                            || legend!!.horizontalAlignment == LegendHorizontalAlignment.RIGHT) {
                        if (legend!!.verticalAlignment == LegendVerticalAlignment.CENTER) {
                            // this is the space between the legend and the chart
                            val spacing = Utils.convertDpToPixel(13f)

                            xLegendOffset = fullLegendWidth + spacing
                        } else {
                            // this is the space between the legend and the chart
                            val spacing = Utils.convertDpToPixel(8f)

                            val legendWidth = fullLegendWidth + spacing
                            val legendHeight = legend!!.mNeededHeight + legend!!.mTextHeightMax

                            val center = center

                            val bottomX = if (legend!!.horizontalAlignment ==
                                    LegendHorizontalAlignment.RIGHT
                            ) width - legendWidth + 15f
                            else legendWidth - 15f
                            val bottomY = legendHeight + 15f
                            val distLegend = distanceToCenter(bottomX, bottomY)

                            val reference = getPosition(center, radius,
                                    getAngleForPoint(bottomX, bottomY))

                            val distReference = distanceToCenter(reference.x, reference.y)
                            val minOffset = Utils.convertDpToPixel(5f)

                            if (bottomY >= center.y && height - legendWidth > width) {
                                xLegendOffset = legendWidth
                            } else if (distLegend < distReference) {
                                val diff = distReference - distLegend
                                xLegendOffset = minOffset + diff
                            }

                            MPPointF.recycleInstance(center)
                            MPPointF.recycleInstance(reference)
                        }
                    }

                    when (legend!!.horizontalAlignment) {
                        LegendHorizontalAlignment.LEFT -> legendLeft = xLegendOffset
                        LegendHorizontalAlignment.RIGHT -> legendRight = xLegendOffset
                        LegendHorizontalAlignment.CENTER -> when (legend!!.verticalAlignment) {
                            LegendVerticalAlignment.TOP -> legendTop = min(legend!!.mNeededHeight.toDouble(),
                                    (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()).toFloat()

                            LegendVerticalAlignment.BOTTOM -> legendBottom = min(legend!!.mNeededHeight.toDouble(),
                                    (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()).toFloat()

                            LegendVerticalAlignment.CENTER -> {
                            }
                        }
                    }
                }

                LegendOrientation.HORIZONTAL -> {
                    var yLegendOffset = 0f

                    if (legend!!.verticalAlignment == LegendVerticalAlignment.TOP ||
                            legend!!.verticalAlignment == LegendVerticalAlignment.BOTTOM) {
                        // It's possible that we do not need this offset anymore as it
                        //   is available through the extraOffsets, but changing it can mean
                        //   changing default visibility for existing apps.

                        val yOffset = requiredLegendOffset

                        yLegendOffset = min((legend!!.mNeededHeight + yOffset).toDouble(),
                                (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()).toFloat()

                        when (legend!!.verticalAlignment) {
                            LegendVerticalAlignment.TOP -> legendTop = yLegendOffset
                            LegendVerticalAlignment.BOTTOM -> legendBottom = yLegendOffset
                            LegendVerticalAlignment.CENTER -> {
                            }
                        }
                    }
                }
            }
            legendLeft += requiredBaseOffset
            legendRight += requiredBaseOffset
            legendTop += requiredBaseOffset
            legendBottom += requiredBaseOffset
        }

        var minOffset = Utils.convertDpToPixel(minOffset)

        if (this is RadarChart) {
            val x = this.xAxis

            if (x!!.isEnabled && x.isDrawLabelsEnabled) {
                minOffset = max(minOffset.toDouble(), x.mLabelRotatedWidth.toDouble()).toFloat()
            }
        }

        legendTop += extraTopOffset
        legendRight += extraRightOffset
        legendBottom += extraBottomOffset
        legendLeft += extraLeftOffset

        val offsetLeft = max(minOffset.toDouble(), legendLeft.toDouble()).toFloat()
        val offsetTop = max(minOffset.toDouble(), legendTop.toDouble()).toFloat()
        val offsetRight = max(minOffset.toDouble(), legendRight.toDouble()).toFloat()
        val offsetBottom = max(minOffset.toDouble(), max(requiredBaseOffset.toDouble(), legendBottom.toDouble())).toFloat()

        viewPortHandler.restrainViewPort(offsetLeft, offsetTop, offsetRight, offsetBottom)

        if (isLogEnabled) Log.i(LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop
                + ", offsetRight: " + offsetRight + ", offsetBottom: " + offsetBottom)
    }

    /**
     * returns the angle relative to the chart center for the given point on the
     * chart in degrees. The angle is always between 0 and 360°, 0° is NORTH,
     * 90° is EAST, ...
     *
     * @param x
     * @param y
     * @return
     */
    fun getAngleForPoint(x: Float, y: Float): Float {
        val c = centerOffsets

        val tx = (x - c.x).toDouble()
        val ty = (y - c.y).toDouble()
        val length = sqrt(tx * tx + ty * ty)
        val r = acos(ty / length)

        var angle = Math.toDegrees(r).toFloat()

        if (x > c.x) angle = 360f - angle

        // add 90° because chart starts EAST
        angle = angle + 90f

        // neutralize overflow
        if (angle > 360f) angle = angle - 360f

        MPPointF.recycleInstance(c)

        return angle
    }

    /**
     * Returns a recyclable MPPointF instance.
     * Calculates the position around a center point, depending on the distance
     * from the center, and the angle of the position around the center.
     *
     * @param center
     * @param dist
     * @param angle  in degrees, converted to radians internally
     * @return
     */
    fun getPosition(center: MPPointF, dist: Float, angle: Float): MPPointF {
        val p = MPPointF.getInstance(0f, 0f)
        getPosition(center, dist, angle, p)
        return p
    }

    fun getPosition(center: MPPointF, dist: Float, angle: Float, outputPoint: MPPointF) {
        outputPoint.x = (center.x + dist * cos(Math.toRadians(angle.toDouble()))).toFloat()
        outputPoint.y = (center.y + dist * sin(Math.toRadians(angle.toDouble()))).toFloat()
    }

    /**
     * Returns the distance of a certain point on the chart to the center of the
     * chart.
     *
     * @param x
     * @param y
     * @return
     */
    fun distanceToCenter(x: Float, y: Float): Float {
        val c = centerOffsets

        var dist = 0f

        var xDist = 0f
        var yDist = 0f

        xDist = if (x > c.x) {
            x - c.x
        } else {
            c.x - x
        }

        yDist = if (y > c.y) {
            y - c.y
        } else {
            c.y - y
        }

        // pythagoras
        dist = sqrt(xDist.pow(2.0f) + yDist.pow(2.0f)).toFloat()

        MPPointF.recycleInstance(c)

        return dist
    }

    /**
     * Returns the xIndex for the given angle around the center of the chart.
     * Returns -1 if not found / outofbounds.
     *
     * @param angle
     * @return
     */
    abstract fun getIndexForAngle(angle: Float): Int

    var rotationAngle: Float
        /**
         * gets a normalized version of the current rotation angle of the pie chart,
         * which will always be between 0.0 < 360.0
         *
         * @return
         */
        get() = mRotationAngle
        /**
         * Set an offset for the rotation of the RadarChart in degrees. Default 270f
         * --> top (NORTH)
         *
         * @param angle
         */
        set(angle) {
            rawRotationAngle = angle
            mRotationAngle = Utils.getNormalizedAngle(rawRotationAngle)
        }

    val diameter: Float
        /**
         * returns the diameter of the pie- or radar-chart
         *
         * @return
         */
        get() {
            val content = viewPortHandler.contentRect
            content.left += extraLeftOffset
            content.top += extraTopOffset
            content.right -= extraRightOffset
            content.bottom -= extraBottomOffset
            return min(content.width().toDouble(), content.height().toDouble()).toFloat()
        }

    /**
     * Returns the radius of the chart in pixels.
     *
     * @return
     */
    abstract val radius: Float

    protected abstract val requiredLegendOffset: Float
        /**
         * Returns the required offset for the chart legend.
         *
         * @return
         */
        get

    protected abstract val requiredBaseOffset: Float
        /**
         * Returns the base offset needed for the chart without calculating the
         * legend size.
         *
         * @return
         */
        get

    override fun getYChartMax(): Float {
        // TODO Auto-generated method stub
        return 0f
    }

    override fun getYChartMin(): Float {
        // TODO Auto-generated method stub
        return 0f
    }

    /**
     * ################ ################ ################ ################
     */
    /** CODE BELOW THIS RELATED TO ANIMATION  */
    /**
     * Applys a spin animation to the Chart.
     *
     * @param durationmillis
     * @param fromangle
     * @param toangle
     */
    @SuppressLint("NewApi")
    fun spin(durationmillis: Int, fromangle: Float, toangle: Float, easing: EasingFunction?) {
        rotationAngle = fromangle

        val spinAnimator = ObjectAnimator.ofFloat(this, "rotationAngle", fromangle,
                toangle)
        spinAnimator.setDuration(durationmillis.toLong())
        spinAnimator.interpolator = easing

        spinAnimator.addUpdateListener { postInvalidate() }
        spinAnimator.start()
    }
}
