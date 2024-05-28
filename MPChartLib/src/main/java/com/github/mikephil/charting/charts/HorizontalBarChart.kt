package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.HorizontalBarHighlighter
import com.github.mikephil.charting.renderer.HorizontalBarChartRenderer
import com.github.mikephil.charting.renderer.XAxisRendererHorizontalBarChart
import com.github.mikephil.charting.renderer.YAxisRendererHorizontalBarChart
import com.github.mikephil.charting.utils.HorizontalViewPortHandler
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.TransformerHorizontalBarChart
import com.github.mikephil.charting.utils.Utils
import kotlin.math.max
import kotlin.math.min

/**
 * BarChart with horizontal bar orientation. In this implementation, x- and y-axis are switched, meaning the YAxis class
 * represents the horizontal values and the XAxis class represents the vertical values.
 *
 * @author Philipp Jahoda
 */
class HorizontalBarChart : BarChart {
    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        viewPortHandler = HorizontalViewPortHandler()

        super.init()

        mLeftAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)
        mRightAxisTransformer = TransformerHorizontalBarChart(viewPortHandler)

        mRenderer = HorizontalBarChartRenderer(this, animator, viewPortHandler)
        setHighlighter(HorizontalBarHighlighter(this))

        rendererLeftYAxis = YAxisRendererHorizontalBarChart(viewPortHandler, axisLeft, mLeftAxisTransformer)
        rendererRightYAxis = YAxisRendererHorizontalBarChart(viewPortHandler, axisRight, mRightAxisTransformer)
        rendererXAxis = XAxisRendererHorizontalBarChart(viewPortHandler, xAxis, mLeftAxisTransformer, this)
    }

    private val mOffsetsBuffer = RectF()

    override fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (legend == null || !legend!!.isEnabled || legend!!.isDrawInsideEnabled) return

        when (legend!!.orientation) {
            LegendOrientation.VERTICAL -> when (legend!!.horizontalAlignment) {
                LegendHorizontalAlignment.LEFT -> {
                    val additionalOffset = (min(
                            legend!!.mNeededWidth.toDouble(),
                            (viewPortHandler.chartWidth * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.xOffset).toFloat()
                    offsets.left += additionalOffset
                }
                LegendHorizontalAlignment.RIGHT -> {
                    val additionalOffset = (min(
                            legend!!.mNeededWidth.toDouble(),
                            (viewPortHandler.chartWidth * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.xOffset).toFloat()
                    offsets.right += additionalOffset
                }
                LegendHorizontalAlignment.CENTER -> {
                    when (legend!!.verticalAlignment) {
                        LegendVerticalAlignment.TOP -> {
                            val additionalOffset = (min(
                                    legend!!.mNeededHeight.toDouble(),
                                    (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                            ) + legend!!.yOffset).toFloat()
                            offsets.top += additionalOffset
                        }
                        LegendVerticalAlignment.BOTTOM -> {
                            val additionalOffset = (min(
                                    legend!!.mNeededHeight.toDouble(),
                                    (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                            ) + legend!!.yOffset).toFloat()
                            offsets.bottom += additionalOffset
                        }
                        else -> {
                            // Handle other cases if necessary
                        }
                    }
                }
                else -> {
                    // Handle other cases if necessary
                }
            }

            LegendOrientation.HORIZONTAL -> when (legend!!.verticalAlignment) {
                LegendVerticalAlignment.TOP -> {
                    val additionalOffsetTop = (min(
                            legend!!.mNeededHeight.toDouble(),
                            (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.yOffset).toFloat()

                    offsets.top += additionalOffsetTop

                    if (axisLeft!!.isEnabled && axisLeft!!.isDrawLabelsEnabled) {
                        offsets.top += axisLeft!!.getRequiredHeightSpace(rendererLeftYAxis!!.paintAxisLabels)
                    }
                }

                LegendVerticalAlignment.BOTTOM -> {
                    val additionalOffsetBottom = (min(
                            legend!!.mNeededHeight.toDouble(),
                            (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.yOffset).toFloat()

                    offsets.bottom += additionalOffsetBottom


                    if (axisRight!!.isEnabled && axisRight!!.isDrawLabelsEnabled) offsets.bottom += axisRight!!.getRequiredHeightSpace(
                            rendererRightYAxis!!.paintAxisLabels)
                }

                else -> {}
            }
        }
    }

    override fun calculateOffsets() {
        var offsetLeft = 0f
        var offsetRight = 0f
        var offsetTop = 0f
        var offsetBottom = 0f

        calculateLegendOffsets(mOffsetsBuffer)

        offsetLeft += mOffsetsBuffer.left
        offsetTop += mOffsetsBuffer.top
        offsetRight += mOffsetsBuffer.right
        offsetBottom += mOffsetsBuffer.bottom

        // offsets for y-labels
        if (axisLeft!!.needsOffset()) {
            offsetTop += axisLeft!!.getRequiredHeightSpace(rendererLeftYAxis!!.paintAxisLabels)
        }

        if (axisRight!!.needsOffset()) {
            offsetBottom += axisRight!!.getRequiredHeightSpace(rendererRightYAxis!!.paintAxisLabels)
        }

        val xlabelwidth = xAxis!!.mLabelRotatedWidth.toFloat()

        if (xAxis!!.isEnabled) {
            // offsets for x-labels

            if (xAxis!!.position == XAxisPosition.BOTTOM) {
                offsetLeft += xlabelwidth
            } else if (xAxis!!.position == XAxisPosition.TOP) {
                offsetRight += xlabelwidth
            } else if (xAxis!!.position == XAxisPosition.BOTH_SIDED) {
                offsetLeft += xlabelwidth
                offsetRight += xlabelwidth
            }
        }

        offsetTop += extraTopOffset
        offsetRight += extraRightOffset
        offsetBottom += extraBottomOffset
        offsetLeft += extraLeftOffset

        val minOffset = Utils.convertDpToPixel(minOffset)

        viewPortHandler.restrainViewPort(
                max(minOffset.toDouble(), offsetLeft.toDouble()).toFloat(),
                max(minOffset.toDouble(), offsetTop.toDouble()).toFloat(),
                max(minOffset.toDouble(), offsetRight.toDouble()).toFloat(),
                max(minOffset.toDouble(), offsetBottom.toDouble()).toFloat())

        if (isLogEnabled) {
            Log.i(LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop + ", offsetRight: " +
                    offsetRight + ", offsetBottom: "
                    + offsetBottom)
            Log.i(LOG_TAG, "Content: " + viewPortHandler.contentRect.toString())
        }

        prepareOffsetMatrix()
        prepareValuePxMatrix()
    }

    override fun prepareValuePxMatrix() {
        mRightAxisTransformer!!.prepareMatrixValuePx(axisRight!!.mAxisMinimum, axisRight!!.mAxisRange, xAxis!!.mAxisRange,
                xAxis!!.mAxisMinimum)
        mLeftAxisTransformer!!.prepareMatrixValuePx(axisLeft!!.mAxisMinimum, axisLeft!!.mAxisRange, xAxis!!.mAxisRange,
                xAxis!!.mAxisMinimum)
    }

    override fun getMarkerPosition(high: Highlight?): FloatArray {
        return floatArrayOf(high!!.drawY, high.drawX)
    }

    override fun getBarBounds(e: BarEntry, outputRect: RectF) {
        val bounds = outputRect
        val set = mData!!.getDataSetForEntry(e)

        if (set == null) {
            outputRect[Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE] = Float.MIN_VALUE
            return
        }

        val y = e.y
        val x = e.x

        val barWidth = mData!!.barWidth

        val top = x - barWidth / 2f
        val bottom = x + barWidth / 2f
        val left = if (y >= 0) y else 0f
        val right = if (y <= 0) y else 0f

        bounds[left, top, right] = bottom

        set.axisDependency?.let { getTransformer(it).rectValueToPixel(bounds) }
    }

    override var mGetPositionBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable MPPointF instance.
     *
     * @param e
     * @param axis
     * @return
     */
    override fun getPosition(e: Entry?, axis: AxisDependency): MPPointF? {
        if (e == null) return null

        val vals = mGetPositionBuffer
        vals[0] = e.y
        vals[1] = e.x

        getTransformer(axis).pointValuesToPixel(vals)

        return MPPointF.getInstance(vals[0], vals[1])
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch point
     * inside the BarChart.
     *
     * @param x
     * @param y
     * @return
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            if (isLogEnabled) Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else return highlighter!!.getHighlight(y, x) // switch x and y
    }

    override fun getLowestVisibleX(): Float {
        getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), posForGetLowestVisibleX)
        val result = max(xAxis!!.mAxisMinimum.toDouble(), posForGetLowestVisibleX.y).toFloat()
        return result
    }

    override fun getHighestVisibleX(): Float {
        getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(viewPortHandler.contentLeft(),
                viewPortHandler.contentTop(), posForGetHighestVisibleX)
        val result = min(xAxis!!.mAxisMaximum.toDouble(), posForGetHighestVisibleX.y).toFloat()
        return result
    }

    /**
     * ###### VIEWPORT METHODS BELOW THIS ######
     */
    override fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = xAxis!!.mAxisRange / (maxXRange)
        viewPortHandler.setMinimumScaleY(xScale)
    }

    override fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = xAxis!!.mAxisRange / (minXRange)
        viewPortHandler.setMaximumScaleY(xScale)
    }

    override fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = xAxis!!.mAxisRange / minXRange
        val maxScale = xAxis!!.mAxisRange / maxXRange
        viewPortHandler.setMinMaxScaleY(minScale, maxScale)
    }

    override fun setVisibleYRangeMaximum(maxYRange: Float, axis: AxisDependency) {
        val yScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinimumScaleX(yScale)
    }

    override fun setVisibleYRangeMinimum(minYRange: Float, axis: AxisDependency) {
        val yScale = getAxisRange(axis) / minYRange
        viewPortHandler.setMaximumScaleX(yScale)
    }

    override fun setVisibleYRange(minYRange: Float, maxYRange: Float, axis: AxisDependency) {
        val minScale = getAxisRange(axis) / minYRange
        val maxScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinMaxScaleX(minScale, maxScale)
    }
}
