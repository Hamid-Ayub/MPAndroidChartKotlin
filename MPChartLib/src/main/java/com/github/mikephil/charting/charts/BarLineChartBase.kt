package com.github.mikephil.charting.charts

import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import com.github.mikephil.charting.components.Legend.LegendHorizontalAlignment
import com.github.mikephil.charting.components.Legend.LegendOrientation
import com.github.mikephil.charting.components.Legend.LegendVerticalAlignment
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.components.YAxis.AxisDependency
import com.github.mikephil.charting.data.BarLineScatterCandleBubbleData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.highlight.ChartHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.BarLineScatterCandleBubbleDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarLineScatterCandleBubbleDataSet
import com.github.mikephil.charting.jobs.AnimatedMoveViewJob
import com.github.mikephil.charting.jobs.AnimatedZoomJob
import com.github.mikephil.charting.jobs.MoveViewJob
import com.github.mikephil.charting.jobs.ZoomJob
import com.github.mikephil.charting.listener.BarLineChartTouchListener
import com.github.mikephil.charting.listener.OnDrawListener
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.MPPointD
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.Utils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Base-class of LineChart, BarChart, ScatterChart and CandleStickChart.
 *
 * @author Philipp Jahoda
 */

abstract class BarLineChartBase<T : BarLineScatterCandleBubbleData<out IBarLineScatterCandleBubbleDataSet<out Entry>?>?>
    : Chart<T>, BarLineScatterCandleBubbleDataProvider {
    /**
     * the maximum number of entries to which values will be drawn
     * (entry numbers greater than this value will cause value-labels to disappear)
     */
    protected var mMaxVisibleCount: Int = 100

    /**
     * @return true if auto scaling on the y axis is enabled.
     * @default false
     */
    /**
     * Flag that indicates if auto scaling on the y axis is enabled. This is
     * especially interesting for charts displaying financial data.
     *
     * @param enabled the y axis automatically adjusts to the min and max y
     * values of the current x axis range whenever the viewport
     * changes
     */
    /**
     * flag that indicates if auto scaling on the y axis is enabled
     */
    var isAutoScaleMinMaxEnabled: Boolean = false

    /**
     * returns true if pinch-zoom is enabled, false if not
     *
     * @return
     */
    /**
     * flag that indicates if pinch-zoom is enabled. if true, both x and y axis
     * can be scaled with 2 fingers, if false, x and y axis can be scaled
     * separately
     */
    var isPinchZoomEnabled: Boolean = false
        protected set

    /**
     * Returns true if zooming via double-tap is enabled false if not.
     *
     * @return
     */
    /**
     * Set this to true to enable zooming in by double-tap on the chart.
     * Default: enabled
     *
     * @param enabled
     */
    /**
     * flag that indicates if double tap zoom is enabled or not
     */
    var isDoubleTapToZoomEnabled: Boolean = true

    /**
     * Set this to true to allow highlighting per dragging over the chart
     * surface when it is fully zoomed out. Default: true
     *
     * @param enabled
     */
    /**
     * flag that indicates if highlighting per dragging over a fully zoomed out
     * chart is enabled
     */
    var isHighlightPerDragEnabled: Boolean = true

    /**
     * Returns true if dragging on the X axis is enabled for the chart, false if not.
     *
     * @return
     */
    /**
     * Set this to true to enable dragging on the X axis
     *
     * @param enabled
     */
    /**
     * if true, dragging is enabled for the chart
     */
    var isDragXEnabled: Boolean = true
    /**
     * Returns true if dragging on the Y axis is enabled for the chart, false if not.
     *
     * @return
     */
    /**
     * Set this to true to enable dragging on the Y axis
     *
     * @param enabled
     */
    var isDragYEnabled: Boolean = true

    var isScaleXEnabled: Boolean = true
    var isScaleYEnabled: Boolean = true

    /**
     * paint object for the (by default) lightgrey background of the grid
     */
    protected var mGridBackgroundPaint: Paint? = null

    protected var mBorderPaint: Paint? = null

    /**
     * flag indicating if the grid background should be drawn or not
     */
    protected var mDrawGridBackground: Boolean = false

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     *
     * @return
     */
    var isDrawBordersEnabled: Boolean = false
        protected set

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     *
     * @return
     */
    var isClipValuesToContentEnabled: Boolean = false
        protected set

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     *
     * @return
     */
    var isClipDataToContentEnabled: Boolean = true
        protected set

    /**
     * Gets the minimum offset (padding) around the chart, defaults to 15.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15.f
     */
    /**
     * Sets the minimum offset (padding) around the chart, defaults to 15
     */
    var minOffset: Float = 15f

    /**
     * Returns true if keeping the position on rotation is enabled and false if not.
     */
    /**
     * Sets whether the chart should keep its position (zoom / scroll) after a rotation (orientation change)
     */
    /**
     * flag indicating if the chart should stay at the same position after a rotation. Default is false.
     */
    var isKeepPositionOnRotation: Boolean = false

    /**
     * Gets the OnDrawListener. May be null.
     *
     * @return
     */
    /**
     * the listener for user drawing on the chart
     */
    var drawListener: OnDrawListener? = null
        protected set

    /**
     * Returns the left y-axis object. In the horizontal bar-chart, this is the
     * top axis.
     *
     * @return
     */
    /**
     * the object representing the labels on the left y-axis
     */
    var axisLeft: YAxis? = null
        protected set

    /**
     * Returns the right y-axis object. In the horizontal bar-chart, this is the
     * bottom axis.
     *
     * @return
     */
    /**
     * the object representing the labels on the right y-axis
     */
    var axisRight: YAxis? = null
        protected set

    /**
     * Sets a custom axis renderer for the left axis and overwrites the existing one.
     *
     * @param rendererLeftYAxis
     */
    var rendererLeftYAxis: YAxisRenderer? = null

    /**
     * Sets a custom axis renderer for the right acis and overwrites the existing one.
     *
     * @param rendererRightYAxis
     */
    var rendererRightYAxis: YAxisRenderer? = null

    @JvmField
    protected var mLeftAxisTransformer: Transformer? = null
    @JvmField
    protected var mRightAxisTransformer: Transformer? = null

    var rendererXAxis: XAxisRenderer? = null
        protected set

    // /** the approximator object used for data filtering */
    // private Approximator mApproximator;
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?) : super(context)

    override fun init() {
        super.init()

        axisLeft = YAxis(AxisDependency.LEFT)
        axisRight = YAxis(AxisDependency.RIGHT)

        mLeftAxisTransformer = Transformer(viewPortHandler)
        mRightAxisTransformer = Transformer(viewPortHandler)

        rendererLeftYAxis = YAxisRenderer(viewPortHandler, axisLeft, mLeftAxisTransformer)
        rendererRightYAxis = YAxisRenderer(viewPortHandler, axisRight, mRightAxisTransformer)

        rendererXAxis = XAxisRenderer(viewPortHandler, xAxis, mLeftAxisTransformer)

        setHighlighter(ChartHighlighter<BarLineScatterCandleBubbleDataProvider?>(this))

        onTouchListener = BarLineChartTouchListener(this, viewPortHandler.matrixTouch, 3f)

        mGridBackgroundPaint = Paint()
        mGridBackgroundPaint!!.style = Paint.Style.FILL
        // mGridBackgroundPaint.setColor(Color.WHITE);
        mGridBackgroundPaint!!.color = Color.rgb(240, 240, 240) // light

        // grey
        mBorderPaint = Paint()
        mBorderPaint!!.style = Paint.Style.STROKE
        mBorderPaint!!.color = Color.BLACK
        mBorderPaint!!.strokeWidth = Utils.convertDpToPixel(1f)
    }

    // for performance tracking
    private var totalTime: Long = 0
    private var drawCycles: Long = 0

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (mData == null) return

        val starttime = System.currentTimeMillis()

        // execute all drawing commands
        drawGridBackground(canvas)

        if (isAutoScaleMinMaxEnabled) {
            autoScale()
        }

        if (axisLeft!!.isEnabled) rendererLeftYAxis!!.computeAxis(axisLeft!!.mAxisMinimum, axisLeft!!.mAxisMaximum, axisLeft!!.isInverted)

        if (axisRight!!.isEnabled) rendererRightYAxis!!.computeAxis(axisRight!!.mAxisMinimum, axisRight!!.mAxisMaximum, axisRight!!.isInverted)

        if (xAxis!!.isEnabled) rendererXAxis!!.computeAxis(xAxis!!.mAxisMinimum, xAxis!!.mAxisMaximum, false)

        rendererXAxis!!.renderAxisLine(canvas)
        rendererLeftYAxis!!.renderAxisLine(canvas)
        rendererRightYAxis!!.renderAxisLine(canvas)

        if (xAxis!!.isDrawGridLinesBehindDataEnabled) rendererXAxis!!.renderGridLines(canvas)

        if (axisLeft!!.isDrawGridLinesBehindDataEnabled) rendererLeftYAxis!!.renderGridLines(canvas)

        if (axisRight!!.isDrawGridLinesBehindDataEnabled) rendererRightYAxis!!.renderGridLines(canvas)

        if (xAxis!!.isEnabled && xAxis!!.isDrawLimitLinesBehindDataEnabled) rendererXAxis!!.renderLimitLines(canvas)

        if (axisLeft!!.isEnabled && axisLeft!!.isDrawLimitLinesBehindDataEnabled) rendererLeftYAxis!!.renderLimitLines(canvas)

        if (axisRight!!.isEnabled && axisRight!!.isDrawLimitLinesBehindDataEnabled) rendererRightYAxis!!.renderLimitLines(canvas)

        var clipRestoreCount = canvas.save()

        if (isClipDataToContentEnabled) {
            // make sure the data cannot be drawn outside the content-rect
            canvas.clipRect(viewPortHandler.contentRect)
        }

        mRenderer.drawData(canvas)

        if (!xAxis!!.isDrawGridLinesBehindDataEnabled) rendererXAxis!!.renderGridLines(canvas)

        if (!axisLeft!!.isDrawGridLinesBehindDataEnabled) rendererLeftYAxis!!.renderGridLines(canvas)

        if (!axisRight!!.isDrawGridLinesBehindDataEnabled) rendererRightYAxis!!.renderGridLines(canvas)

        // if highlighting is enabled
        if (valuesToHighlight()) mRenderer!!.drawHighlighted(canvas, highlighted)

        // Removes clipping rectangle
        canvas.restoreToCount(clipRestoreCount)

        mRenderer.drawExtras(canvas)

        if (xAxis!!.isEnabled && !xAxis!!.isDrawLimitLinesBehindDataEnabled) rendererXAxis!!.renderLimitLines(canvas)

        if (axisLeft!!.isEnabled && !axisLeft!!.isDrawLimitLinesBehindDataEnabled) rendererLeftYAxis!!.renderLimitLines(canvas)

        if (axisRight!!.isEnabled && !axisRight!!.isDrawLimitLinesBehindDataEnabled) rendererRightYAxis!!.renderLimitLines(canvas)

        rendererXAxis!!.renderAxisLabels(canvas)
        rendererLeftYAxis!!.renderAxisLabels(canvas)
        rendererRightYAxis!!.renderAxisLabels(canvas)

        if (isClipValuesToContentEnabled) {
            clipRestoreCount = canvas.save()
            canvas.clipRect(viewPortHandler.contentRect)

            mRenderer!!.drawValues(canvas)

            canvas.restoreToCount(clipRestoreCount)
        } else {
            mRenderer!!.drawValues(canvas)
        }

        legendRenderer!!.renderLegend(canvas)

        drawDescription(canvas)

        drawMarkers(canvas)

        if (isLogEnabled) {
            val drawtime = (System.currentTimeMillis() - starttime)
            totalTime += drawtime
            drawCycles += 1
            val average = totalTime / drawCycles
            Log.i(LOG_TAG, "Drawtime: " + drawtime + " ms, average: " + average + " ms, cycles: "
                    + drawCycles)
        }
    }

    /**
     * RESET PERFORMANCE TRACKING FIELDS
     */
    fun resetTracking() {
        totalTime = 0
        drawCycles = 0
    }

    protected open fun prepareValuePxMatrix() {
        if (isLogEnabled) Log.i(LOG_TAG, "Preparing Value-Px Matrix, xmin: " + xAxis!!.mAxisMinimum + ", xmax: "
                + xAxis!!.mAxisMaximum + ", xdelta: " + xAxis!!.mAxisRange)

        mRightAxisTransformer!!.prepareMatrixValuePx(xAxis!!.mAxisMinimum,
                xAxis!!.mAxisRange,
                axisRight!!.mAxisRange,
                axisRight!!.mAxisMinimum)
        mLeftAxisTransformer!!.prepareMatrixValuePx(xAxis!!.mAxisMinimum,
                xAxis!!.mAxisRange,
                axisLeft!!.mAxisRange,
                axisLeft!!.mAxisMinimum)
    }

    protected fun prepareOffsetMatrix() {
        mRightAxisTransformer!!.prepareMatrixOffset(axisRight!!.isInverted)
        mLeftAxisTransformer!!.prepareMatrixOffset(axisLeft!!.isInverted)
    }

    override fun notifyDataSetChanged() {
        if (mData == null) {
            if (isLogEnabled) Log.i(LOG_TAG, "Preparing... DATA NOT SET.")
            return
        } else {
            if (isLogEnabled) Log.i(LOG_TAG, "Preparing...")
        }

        if (mRenderer != null) mRenderer!!.initBuffers()

        calcMinMax()

        rendererLeftYAxis!!.computeAxis(axisLeft!!.mAxisMinimum, axisLeft!!.mAxisMaximum, axisLeft!!.isInverted)
        rendererRightYAxis!!.computeAxis(axisRight!!.mAxisMinimum, axisRight!!.mAxisMaximum, axisRight!!.isInverted)
        rendererXAxis!!.computeAxis(xAxis!!.mAxisMinimum, xAxis!!.mAxisMaximum, false)

        if (legend != null) legendRenderer!!.computeLegend(mData)

        calculateOffsets()
    }

    /**
     * Performs auto scaling of the axis by recalculating the minimum and maximum y-values based on the entries currently in view.
     */
    protected fun autoScale() {
        val fromX = lowestVisibleX
        val toX = highestVisibleX

        mData!!.calcMinMaxY(fromX, toX)

        xAxis!!.calculate(mData!!.xMin, mData!!.xMax)

        // calculate axis range (min / max) according to provided data
        if (axisLeft!!.isEnabled) axisLeft!!.calculate(mData!!.getYMin(AxisDependency.LEFT),
                mData!!.getYMax(AxisDependency.LEFT))

        if (axisRight!!.isEnabled) axisRight!!.calculate(mData!!.getYMin(AxisDependency.RIGHT),
                mData!!.getYMax(AxisDependency.RIGHT))

        calculateOffsets()
    }

    override fun calcMinMax() {
        xAxis!!.calculate(mData!!.xMin, mData!!.xMax)

        // calculate axis range (min / max) according to provided data
        axisLeft!!.calculate(mData!!.getYMin(AxisDependency.LEFT), mData!!.getYMax(AxisDependency.LEFT))
        axisRight!!.calculate(mData!!.getYMin(AxisDependency.RIGHT), mData!!.getYMax(AxisDependency
                .RIGHT))
    }

    protected open fun calculateLegendOffsets(offsets: RectF) {
        offsets.left = 0f
        offsets.right = 0f
        offsets.top = 0f
        offsets.bottom = 0f

        if (legend == null || !legend!!.isEnabled || legend!!.isDrawInsideEnabled) return

        when (legend!!.orientation) {
            LegendOrientation.VERTICAL -> when (legend!!.horizontalAlignment) {
                LegendHorizontalAlignment.LEFT -> {
                    val additionalOffset = min(
                            legend!!.mNeededWidth.toFloat(),
                            (viewPortHandler.chartWidth * legend!!.maxSizePercent).toFloat()
                    ) + legend!!.xOffset

                    // Perform the assignment separately
                    offsets.left += additionalOffset
                }

                LegendHorizontalAlignment.RIGHT -> {
                    val additionalOffset = (min(
                            legend!!.mNeededWidth.toDouble(),
                            (viewPortHandler.chartWidth * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.xOffset).toFloat()

                    // Perform the assignment separately
                    offsets.right += additionalOffset
                }

                LegendHorizontalAlignment.CENTER -> when (legend!!.verticalAlignment) {
                    LegendVerticalAlignment.TOP -> {
                        val additionalOffset = (min(
                                legend!!.mNeededHeight.toDouble(),
                                (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                        ) + legend!!.yOffset).toFloat()

                        // Perform the assignment separately
                        offsets.top += additionalOffset
                    }
                    LegendVerticalAlignment.BOTTOM -> {
                        val additionalOffset = (min(
                                legend!!.mNeededHeight.toDouble(),
                                (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                        ) + legend!!.yOffset).toFloat()

                        // Perform the assignment separately
                        offsets.bottom += additionalOffset
                    }

                    else -> {}
                }
            }

            LegendOrientation.HORIZONTAL -> when (legend!!.verticalAlignment) {
                LegendVerticalAlignment.TOP -> {
                    val additionalOffset = (min(
                            legend!!.mNeededHeight.toDouble(),
                            (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.yOffset).toFloat()

                    // Perform the assignment separately
                    offsets.top += additionalOffset
                }
                LegendVerticalAlignment.BOTTOM -> {
                    val additionalOffset = (min(
                            legend!!.mNeededHeight.toDouble(),
                            (viewPortHandler.chartHeight * legend!!.maxSizePercent).toDouble()
                    ) + legend!!.yOffset).toFloat()

                    // Perform the assignment separately
                    offsets.bottom += additionalOffset
                }


                else -> {}
            }
        }
    }

    private val mOffsetsBuffer = RectF()

    public override fun calculateOffsets() {
        if (!mCustomViewPortEnabled) {
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
                offsetLeft += axisLeft!!.getRequiredWidthSpace(rendererLeftYAxis
                        !!.getPaintAxisLabels())
            }

            if (axisRight!!.needsOffset()) {
                offsetRight += axisRight!!.getRequiredWidthSpace(rendererRightYAxis
                        !!.getPaintAxisLabels())
            }

            if (xAxis!!.isEnabled && xAxis!!.isDrawLabelsEnabled) {
                val xLabelHeight = xAxis!!.mLabelRotatedHeight + xAxis!!.yOffset

                // offsets for x-labels
                if (xAxis!!.position == XAxisPosition.BOTTOM) {
                    offsetBottom += xLabelHeight
                } else if (xAxis!!.position == XAxisPosition.TOP) {
                    offsetTop += xLabelHeight
                } else if (xAxis!!.position == XAxisPosition.BOTH_SIDED) {
                    offsetBottom += xLabelHeight
                    offsetTop += xLabelHeight
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
                Log.i(LOG_TAG, "offsetLeft: " + offsetLeft + ", offsetTop: " + offsetTop
                        + ", offsetRight: " + offsetRight + ", offsetBottom: " + offsetBottom)
                Log.i(LOG_TAG, "Content: " + viewPortHandler.contentRect.toString())
            }
        }

        prepareOffsetMatrix()
        prepareValuePxMatrix()
    }

    /**
     * draws the grid background
     */
    protected fun drawGridBackground(c: Canvas) {
        if (mDrawGridBackground) {
            // draw the grid background

            c.drawRect(viewPortHandler.contentRect, mGridBackgroundPaint!!)
        }

        if (isDrawBordersEnabled) {
            c.drawRect(viewPortHandler.contentRect, mBorderPaint!!)
        }
    }

    /**
     * Returns the Transformer class that contains all matrices and is
     * responsible for transforming values into pixels on the screen and
     * backwards.
     *
     * @return
     */
    override fun getTransformer(which: AxisDependency): Transformer {
        return if (which == AxisDependency.LEFT) mLeftAxisTransformer!!
        else mRightAxisTransformer!!
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        super.onTouchEvent(event)

        if (onTouchListener == null || mData == null) return false

        // check if touch gestures are enabled
        return if (!mTouchEnabled) false
        else onTouchListener!!.onTouch(this, event)
    }

    override fun computeScroll() {
        if (onTouchListener is BarLineChartTouchListener) (onTouchListener as BarLineChartTouchListener).computeScroll()
    }

    /**
     * ################ ################ ################ ################
     */
    /**
     * CODE BELOW THIS RELATED TO SCALING AND GESTURES AND MODIFICATION OF THE
     * VIEWPORT
     */
    protected var mZoomMatrixBuffer: Matrix = Matrix()

    /**
     * Zooms in by 1.4f, into the charts center.
     */
    fun zoomIn() {
        val center = viewPortHandler.contentCenter

        viewPortHandler.zoomIn(center.x, -center.y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        MPPointF.recycleInstance(center)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms out by 0.7f, from the charts center.
     */
    fun zoomOut() {
        val center = viewPortHandler.contentCenter

        viewPortHandler.zoomOut(center.x, -center.y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        MPPointF.recycleInstance(center)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms out to original size.
     */
    fun resetZoom() {
        viewPortHandler.resetZoom(mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms in or out by the given scale factor. x and y are the coordinates
     * (in pixels) of the zoom center.
     *
     * @param scaleX if < 1f --> zoom out, if > 1f --> zoom in
     * @param scaleY if < 1f --> zoom out, if > 1f --> zoom in
     * @param x
     * @param y
     */
    fun zoom(scaleX: Float, scaleY: Float, x: Float, y: Float) {
        viewPortHandler.zoom(scaleX, scaleY, x, -y, mZoomMatrixBuffer)
        viewPortHandler.refresh(mZoomMatrixBuffer, this, false)

        // Range might have changed, which means that Y-axis labels
        // could have changed in size, affecting Y-axis size.
        // So we need to recalculate offsets.
        calculateOffsets()
        postInvalidate()
    }

    /**
     * Zooms in or out by the given scale factor.
     * x and y are the values (NOT PIXELS) of the zoom center..
     *
     * @param scaleX
     * @param scaleY
     * @param xValue
     * @param yValue
     * @param axis   the axis relative to which the zoom should take place
     */
    fun zoom(scaleX: Float, scaleY: Float, xValue: Float, yValue: Float, axis: AxisDependency) {
        val job: Runnable = ZoomJob.getInstance(viewPortHandler, scaleX, scaleY, xValue, yValue, getTransformer(axis), axis, this)
        addViewportJob(job)
    }

    /**
     * Zooms to the center of the chart with the given scale factor.
     *
     * @param scaleX
     * @param scaleY
     */
    fun zoomToCenter(scaleX: Float, scaleY: Float) {
        val center = centerOffsets

        val save = mZoomMatrixBuffer
        viewPortHandler.zoom(scaleX, scaleY, center.x, -center.y, save)
        viewPortHandler.refresh(save, this, false)
    }

    /**
     * Zooms by the specified scale factor to the specified values on the specified axis.
     *
     * @param scaleX
     * @param scaleY
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration
     */
    @TargetApi(11)
    fun zoomAndCenterAnimated(scaleX: Float, scaleY: Float, xValue: Float, yValue: Float, axis: AxisDependency,
                              duration: Long) {
        val origin = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val job: Runnable = AnimatedZoomJob.getInstance(viewPortHandler, this, getTransformer(axis), getAxis(axis), xAxis
                !!.mAxisRange, scaleX, scaleY, viewPortHandler.scaleX, viewPortHandler.scaleY,
                xValue, yValue, origin.x.toFloat(), origin.y.toFloat(), duration)
        addViewportJob(job)

        MPPointD.recycleInstance(origin)
    }

    protected var mFitScreenMatrixBuffer: Matrix = Matrix()

    /**
     * Resets all zooming and dragging and makes the chart fit exactly it's
     * bounds.
     */
    fun fitScreen() {
        val save = mFitScreenMatrixBuffer
        viewPortHandler.fitScreen(save)
        viewPortHandler.refresh(save, this, false)

        calculateOffsets()
        postInvalidate()
    }

    /**
     * Sets the minimum scale factor value to which can be zoomed out. 1f =
     * fitScreen
     *
     * @param scaleX
     * @param scaleY
     */
    fun setScaleMinima(scaleX: Float, scaleY: Float) {
        viewPortHandler.setMinimumScaleX(scaleX)
        viewPortHandler.setMinimumScaleY(scaleY)
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be maximum
     * visible at once (no further zooming out allowed). If this is e.g. set to
     * 10, no more than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param maxXRange The maximum visible range of x-values.
     */
    open fun setVisibleXRangeMaximum(maxXRange: Float) {
        val xScale = xAxis!!.mAxisRange / (maxXRange)
        viewPortHandler.setMinimumScaleX(xScale)
    }

    /**
     * Sets the size of the area (range on the x-axis) that should be minimum
     * visible at once (no further zooming in allowed). If this is e.g. set to
     * 10, no less than a range of 10 on the x-axis can be viewed at once without
     * scrolling.
     *
     * @param minXRange The minimum visible range of x-values.
     */
    open fun setVisibleXRangeMinimum(minXRange: Float) {
        val xScale = xAxis!!.mAxisRange / (minXRange)
        viewPortHandler.setMaximumScaleX(xScale)
    }

    /**
     * Limits the maximum and minimum x range that can be visible by pinching and zooming. e.g. minRange=10, maxRange=100 the
     * smallest range to be displayed at once is 10, and no more than a range of 100 values can be viewed at once without
     * scrolling
     *
     * @param minXRange
     * @param maxXRange
     */
    open fun setVisibleXRange(minXRange: Float, maxXRange: Float) {
        val minScale = xAxis!!.mAxisRange / minXRange
        val maxScale = xAxis!!.mAxisRange / maxXRange
        viewPortHandler.setMinMaxScaleX(minScale, maxScale)
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be maximum
     * visible at once.
     *
     * @param maxYRange the maximum visible range on the y-axis
     * @param axis      the axis for which this limit should apply
     */
    open fun setVisibleYRangeMaximum(maxYRange: Float, axis: AxisDependency) {
        val yScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinimumScaleY(yScale)
    }

    /**
     * Sets the size of the area (range on the y-axis) that should be minimum visible at once, no further zooming in possible.
     *
     * @param minYRange
     * @param axis      the axis for which this limit should apply
     */
    open fun setVisibleYRangeMinimum(minYRange: Float, axis: AxisDependency) {
        val yScale = getAxisRange(axis) / minYRange
        viewPortHandler.setMaximumScaleY(yScale)
    }

    /**
     * Limits the maximum and minimum y range that can be visible by pinching and zooming.
     *
     * @param minYRange
     * @param maxYRange
     * @param axis
     */
    open fun setVisibleYRange(minYRange: Float, maxYRange: Float, axis: AxisDependency) {
        val minScale = getAxisRange(axis) / minYRange
        val maxScale = getAxisRange(axis) / maxYRange
        viewPortHandler.setMinMaxScaleY(minScale, maxScale)
    }


    /**
     * Moves the left side of the current viewport to the specified x-position.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     */
    fun moveViewToX(xValue: Float) {
        val job: Runnable = MoveViewJob.getInstance(viewPortHandler, xValue, 0f,
                getTransformer(AxisDependency.LEFT), this)

        addViewportJob(job)
    }

    /**
     * This will move the left side of the current viewport to the specified
     * x-value on the x-axis, and center the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y-axis
     */
    fun moveViewTo(xValue: Float, yValue: Float, axis: AxisDependency) {
        val yInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable = MoveViewJob.getInstance(viewPortHandler, xValue, yValue + yInView / 2f,
                getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the left side of the current viewport to the specified x-value
     * and center the viewport to the y value animated.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration the duration of the animation in milliseconds
     */
    @TargetApi(11)
    fun moveViewToAnimated(xValue: Float, yValue: Float, axis: AxisDependency, duration: Long) {
        val bounds = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val yInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable = AnimatedMoveViewJob.getInstance(viewPortHandler, xValue, yValue + yInView / 2f,
                getTransformer(axis), this, bounds.x.toFloat(), bounds.y.toFloat(), duration)

        addViewportJob(job)

        MPPointD.recycleInstance(bounds)
    }

    /**
     * Centers the viewport to the specified y value on the y-axis.
     * This also refreshes the chart by calling invalidate().
     *
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y-axis
     */
    fun centerViewToY(yValue: Float, axis: AxisDependency) {
        val valsInView = getAxisRange(axis) / viewPortHandler.scaleY

        val job: Runnable = MoveViewJob.getInstance(viewPortHandler, 0f, yValue + valsInView / 2f,
                getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value.
     * This also refreshes the chart by calling invalidate().
     *
     * @param xValue
     * @param yValue
     * @param axis   - which axis should be used as a reference for the y axis
     */
    fun centerViewTo(xValue: Float, yValue: Float, axis: AxisDependency) {
        val yInView = getAxisRange(axis) / viewPortHandler.scaleY
        val xInView = xAxis!!.mAxisRange / viewPortHandler.scaleX

        val job: Runnable = MoveViewJob.getInstance(viewPortHandler,
                xValue - xInView / 2f, yValue + yInView / 2f,
                getTransformer(axis), this)

        addViewportJob(job)
    }

    /**
     * This will move the center of the current viewport to the specified
     * x and y value animated.
     *
     * @param xValue
     * @param yValue
     * @param axis
     * @param duration the duration of the animation in milliseconds
     */
    @TargetApi(11)
    fun centerViewToAnimated(xValue: Float, yValue: Float, axis: AxisDependency, duration: Long) {
        val bounds = getValuesByTouchPoint(viewPortHandler.contentLeft(), viewPortHandler.contentTop(), axis)

        val yInView = getAxisRange(axis) / viewPortHandler.scaleY
        val xInView = xAxis!!.mAxisRange / viewPortHandler.scaleX

        val job: Runnable = AnimatedMoveViewJob.getInstance(viewPortHandler,
                xValue - xInView / 2f, yValue + yInView / 2f,
                getTransformer(axis), this, bounds.x.toFloat(), bounds.y.toFloat(), duration)

        addViewportJob(job)

        MPPointD.recycleInstance(bounds)
    }

    /**
     * flag that indicates if a custom viewport offset has been set
     */
    private var mCustomViewPortEnabled = false

    /**
     * Sets custom offsets for the current ViewPort (the offsets on the sides of
     * the actual chart window). Setting this will prevent the chart from
     * automatically calculating it's offsets. Use resetViewPortOffsets() to
     * undo this. ONLY USE THIS WHEN YOU KNOW WHAT YOU ARE DOING, else use
     * setExtraOffsets(...).
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    fun setViewPortOffsets(left: Float, top: Float,
                           right: Float, bottom: Float) {
        mCustomViewPortEnabled = true
        post {
            viewPortHandler.restrainViewPort(left, top, right, bottom)
            prepareOffsetMatrix()
            prepareValuePxMatrix()
        }
    }

    /**
     * Resets all custom offsets set via setViewPortOffsets(...) method. Allows
     * the chart to again calculate all offsets automatically.
     */
    fun resetViewPortOffsets() {
        mCustomViewPortEnabled = false
        calculateOffsets()
    }

    /**
     * ################ ################ ################ ################
     */
    /** CODE BELOW IS GETTERS AND SETTERS  */
    /**
     * Returns the range of the specified axis.
     *
     * @param axis
     * @return
     */
    protected fun getAxisRange(axis: AxisDependency): Float {
        return if (axis == AxisDependency.LEFT) axisLeft!!.mAxisRange
        else axisRight!!.mAxisRange
    }

    /**
     * Sets the OnDrawListener
     *
     * @param drawListener
     */
    fun setOnDrawListener(drawListener: OnDrawListener?) {
        this.drawListener = drawListener
    }

    protected open var mGetPositionBuffer: FloatArray = FloatArray(2)

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the position (in pixels) the provided Entry has inside the chart
     * view or null, if the provided Entry is null.
     *
     * @param e
     * @return
     */
    open fun getPosition(e: Entry?, axis: AxisDependency): MPPointF? {
        if (e == null) return null

        mGetPositionBuffer[0] = e.x
        mGetPositionBuffer[1] = e.y

        getTransformer(axis).pointValuesToPixel(mGetPositionBuffer)

        return MPPointF.getInstance(mGetPositionBuffer[0], mGetPositionBuffer[1])
    }

    /**
     * sets the number of maximum visible drawn values on the chart only active
     * when setDrawValues() is enabled
     *
     * @param count
     */
    fun setMaxVisibleValueCount(count: Int) {
        this.mMaxVisibleCount = count
    }

    override fun getMaxVisibleCount(): Int {
        return mMaxVisibleCount
    }

    /**
     * Sets the color for the background of the chart-drawing area (everything
     * behind the grid lines).
     *
     * @param color
     */
    fun setGridBackgroundColor(color: Int) {
        mGridBackgroundPaint!!.color = color
    }

    var isDragEnabled: Boolean
        /**
         * Returns true if dragging is enabled for the chart, false if not.
         *
         * @return
         */
        get() = isDragXEnabled || isDragYEnabled
        /**
         * Set this to true to enable dragging (moving the chart with the finger)
         * for the chart (this does not effect scaling).
         *
         * @param enabled
         */
        set(enabled) {
            this.isDragXEnabled = enabled
            this.isDragYEnabled = enabled
        }

    /**
     * Set this to true to enable scaling (zooming in and out by gesture) for
     * the chart (this does not effect dragging) on both X- and Y-Axis.
     *
     * @param enabled
     */
    fun setScaleEnabled(enabled: Boolean) {
        this.isScaleXEnabled = enabled
        this.isScaleYEnabled = enabled
    }

    /**
     * set this to true to draw the grid background, false if not
     *
     * @param enabled
     */
    fun setDrawGridBackground(enabled: Boolean) {
        mDrawGridBackground = enabled
    }

    /**
     * When enabled, the borders rectangle will be rendered.
     * If this is enabled, there is no point drawing the axis-lines of x- and y-axis.
     *
     * @param enabled
     */
    fun setDrawBorders(enabled: Boolean) {
        isDrawBordersEnabled = enabled
    }

    /**
     * When enabled, the values will be clipped to contentRect,
     * otherwise they can bleed outside the content rect.
     *
     * @param enabled
     */
    fun setClipValuesToContent(enabled: Boolean) {
        isClipValuesToContentEnabled = enabled
    }

    /**
     * When disabled, the data and/or highlights will not be clipped to contentRect. Disabling this option can
     * be useful, when the data lies fully within the content rect, but is drawn in such a way (such as thick lines)
     * that there is unwanted clipping.
     *
     * @param enabled
     */
    fun setClipDataToContent(enabled: Boolean) {
        isClipDataToContentEnabled = enabled
    }

    /**
     * Sets the width of the border lines in dp.
     *
     * @param width
     */
    fun setBorderWidth(width: Float) {
        mBorderPaint!!.strokeWidth = Utils.convertDpToPixel(width)
    }

    /**
     * Sets the color of the chart border lines.
     *
     * @param color
     */
    fun setBorderColor(color: Int) {
        mBorderPaint!!.color = color
    }

    /**
     * Returns a recyclable MPPointD instance
     * Returns the x and y values in the chart at the given touch point
     * (encapsulated in a MPPointD). This method transforms pixel coordinates to
     * coordinates / values in the chart. This is the opposite method to
     * getPixelForValues(...).
     *
     * @param x
     * @param y
     * @return
     */
    fun getValuesByTouchPoint(x: Float, y: Float, axis: AxisDependency): MPPointD {
        val result = MPPointD.getInstance(0.0, 0.0)
        getValuesByTouchPoint(x, y, axis, result)
        return result
    }

    fun getValuesByTouchPoint(x: Float, y: Float, axis: AxisDependency, outputPoint: MPPointD?) {
        getTransformer(axis).getValuesByTouchPoint(x, y, outputPoint)
    }

    /**
     * Returns a recyclable MPPointD instance
     * Transforms the given chart values into pixels. This is the opposite
     * method to getValuesByTouchPoint(...).
     *
     * @param x
     * @param y
     * @return
     */
    fun getPixelForValues(x: Float, y: Float, axis: AxisDependency): MPPointD {
        return getTransformer(axis).getPixelForValues(x, y)
    }

    /**
     * returns the Entry object displayed at the touched position of the chart
     *
     * @param x
     * @param y
     * @return
     */
    fun getEntryByTouchPoint(x: Float, y: Float): Entry? {
        val h = getHighlightByTouchPoint(x, y)
        if (h != null) {
            return mData!!.getEntryForHighlight(h)
        }
        return null
    }

    /**
     * returns the DataSet object displayed at the touched position of the chart
     *
     * @param x
     * @param y
     * @return
     */
    fun getDataSetByTouchPoint(x: Float, y: Float): IBarLineScatterCandleBubbleDataSet<*>? {
        val h = getHighlightByTouchPoint(x, y)
        if (h != null) {
            return mData!!.getDataSetByIndex(h.dataSetIndex)
        }
        return null
    }

    /**
     * buffer for storing lowest visible x point
     */
    @JvmField
    protected var posForGetLowestVisibleX: MPPointD = MPPointD.getInstance(0.0, 0.0)

    /**
     * Returns the lowest x-index (value on the x-axis) that is still visible on
     * the chart.
     *
     * @return
     */
    override fun getLowestVisibleX(): Float {
        getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(viewPortHandler.contentLeft(),
                viewPortHandler.contentBottom(), posForGetLowestVisibleX)
        val result = max(xAxis!!.mAxisMinimum.toDouble(), posForGetLowestVisibleX.x).toFloat()
        return result
    }

    /**
     * buffer for storing highest visible x point
     */
    @JvmField
    protected var posForGetHighestVisibleX: MPPointD = MPPointD.getInstance(0.0, 0.0)

    /**
     * Returns the highest x-index (value on the x-axis) that is still visible
     * on the chart.
     *
     * @return
     */
    override fun getHighestVisibleX(): Float {
        getTransformer(AxisDependency.LEFT).getValuesByTouchPoint(viewPortHandler.contentRight(),
                viewPortHandler.contentBottom(), posForGetHighestVisibleX)
        val result = min(xAxis!!.mAxisMaximum.toDouble(), posForGetHighestVisibleX.x).toFloat()
        return result
    }

    val visibleXRange: Float
        /**
         * Returns the range visible on the x-axis.
         *
         * @return
         */
        get() = abs((highestVisibleX - lowestVisibleX).toDouble()).toFloat()

    /**
     * returns the current x-scale factor
     */
    override fun getScaleX(): Float {
        return if (viewPortHandler == null) 1f
        else viewPortHandler.scaleX
    }

    /**
     * returns the current y-scale factor
     */
    override fun getScaleY(): Float {
        return if (viewPortHandler == null) 1f
        else viewPortHandler.scaleY
    }

    val isFullyZoomedOut: Boolean
        /**
         * if the chart is fully zoomed out, return true
         *
         * @return
         */
        get() = viewPortHandler.isFullyZoomedOut

    /**
     * Returns the y-axis object to the corresponding AxisDependency. In the
     * horizontal bar-chart, LEFT == top, RIGHT == BOTTOM
     *
     * @param axis
     * @return
     */
    fun getAxis(axis: AxisDependency): YAxis? {
        return if (axis == AxisDependency.LEFT) axisLeft
        else axisRight
    }

    override fun isInverted(axis: AxisDependency): Boolean {
        return getAxis(axis)!!.isInverted
    }

    /**
     * If set to true, both x and y axis can be scaled simultaneously with 2 fingers, if false,
     * x and y axis can be scaled separately. default: false
     *
     * @param enabled
     */
    fun setPinchZoom(enabled: Boolean) {
        isPinchZoomEnabled = enabled
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the x-axis.
     *
     * @param offset
     */
    fun setDragOffsetX(offset: Float) {
        viewPortHandler.setDragOffsetX(offset)
    }

    /**
     * Set an offset in dp that allows the user to drag the chart over it's
     * bounds on the y-axis.
     *
     * @param offset
     */
    fun setDragOffsetY(offset: Float) {
        viewPortHandler.setDragOffsetY(offset)
    }

    /**
     * Returns true if both drag offsets (x and y) are zero or smaller.
     *
     * @return
     */
    fun hasNoDragOffset(): Boolean {
        return viewPortHandler.hasNoDragOffset()
    }

    /**
     * Sets a custom XAxisRenderer and overrides the existing (default) one.
     *
     * @param xAxisRenderer
     */
    fun setXAxisRenderer(xAxisRenderer: XAxisRenderer?) {
        rendererXAxis = xAxisRenderer
    }

    override fun getYChartMax(): Float {
        return max(axisLeft!!.mAxisMaximum.toDouble(), axisRight!!.mAxisMaximum.toDouble()).toFloat()
    }

    override fun getYChartMin(): Float {
        return min(axisLeft!!.mAxisMinimum.toDouble(), axisRight!!.mAxisMinimum.toDouble()).toFloat()
    }

    val isAnyAxisInverted: Boolean
        /**
         * Returns true if either the left or the right or both axes are inverted.
         *
         * @return
         */
        get() {
            if (axisLeft!!.isInverted) return true
            if (axisRight!!.isInverted) return true
            return false
        }

    override fun setPaint(p: Paint?, which: Int) {
        super.setPaint(p, which)

        when (which) {
            PAINT_GRID_BACKGROUND -> mGridBackgroundPaint = p
        }
    }

    override fun getPaint(which: Int): Paint? {
        val p = super.getPaint(which)
        if (p != null) return p

        when (which) {
            PAINT_GRID_BACKGROUND -> return mGridBackgroundPaint
        }
        return null
    }

    protected var mOnSizeChangedBuffer: FloatArray = FloatArray(2)

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        // Saving current position of chart.

        mOnSizeChangedBuffer[1] = 0f
        mOnSizeChangedBuffer[0] = mOnSizeChangedBuffer[1]

        if (isKeepPositionOnRotation) {
            mOnSizeChangedBuffer[0] = viewPortHandler.contentLeft()
            mOnSizeChangedBuffer[1] = viewPortHandler.contentTop()
            getTransformer(AxisDependency.LEFT).pixelsToValue(mOnSizeChangedBuffer)
        }

        //Superclass transforms chart.
        super.onSizeChanged(w, h, oldw, oldh)

        if (isKeepPositionOnRotation) {
            //Restoring old position of chart.

            getTransformer(AxisDependency.LEFT).pointValuesToPixel(mOnSizeChangedBuffer)
            viewPortHandler.centerViewPort(mOnSizeChangedBuffer, this)
        } else {
            viewPortHandler.refresh(viewPortHandler.matrixTouch, this, true)
        }
    }
}
