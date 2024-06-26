package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.highlight.BarHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.renderer.BarChartRenderer

/**
 * Chart that draws bars.
 *
 * @author Philipp Jahoda
 */
open class BarChart : BarLineChartBase<BarData?>, BarDataProvider {
    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    protected var mHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, all values are drawn above their bars, instead of below their top
     */
    private var mDrawValueAboveBar = true

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the maximum value
     */
    private var mDrawBarShadow = false

    private var mFitBars = false

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        mRenderer = BarChartRenderer(this, animator, viewPortHandler)

        setHighlighter(BarHighlighter(this))

        xAxis!!.spaceMin = 0.5f
        xAxis!!.spaceMax = 0.5f
    }

    override fun calcMinMax() {
        if (mFitBars) {
            xAxis!!.calculate(mData!!.xMin - mData!!.barWidth / 2f, mData!!.xMax + mData!!.barWidth / 2f)
        } else {
            xAxis!!.calculate(mData!!.xMin, mData!!.xMax)
        }

        // calculate axis range (min / max) according to provided data
        axisLeft!!.calculate(mData!!.getYMin(YAxis.AxisDependency.LEFT), mData!!.getYMax(YAxis.AxisDependency.LEFT))
        axisRight!!.calculate(mData!!.getYMin(YAxis.AxisDependency.RIGHT), mData!!.getYMax(YAxis.AxisDependency
                .RIGHT))
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the BarChart.
     *
     * @param x
     * @param y
     * @return
     */
    override fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else {
            val h = highlighter!!.getHighlight(x, y)
            if (h == null || !isHighlightFullBarEnabled) return h

            // For isHighlightFullBarEnabled, remove stackIndex
            return Highlight(h.x, h.y,
                    h.xPx, h.yPx,
                    h.dataSetIndex, -1, h.axis)
        }
    }

    /**
     * Returns the bounding box of the specified Entry in the specified DataSet. Returns null if the Entry could not be
     * found in the charts data.  Performance-intensive code should use void getBarBounds(BarEntry, RectF) instead.
     *
     * @param e
     * @return
     */
    fun getBarBounds(e: BarEntry): RectF {
        val bounds = RectF()
        getBarBounds(e, bounds)

        return bounds
    }

    /**
     * The passed outputRect will be assigned the values of the bounding box of the specified Entry in the specified DataSet.
     * The rect will be assigned Float.MIN_VALUE in all locations if the Entry could not be found in the charts data.
     *
     * @param e
     * @return
     */
    open fun getBarBounds(e: BarEntry, outputRect: RectF) {
        val bounds = outputRect

        val set = mData!!.getDataSetForEntry(e)

        if (set == null) {
            bounds[Float.MIN_VALUE, Float.MIN_VALUE, Float.MIN_VALUE] = Float.MIN_VALUE
            return
        }

        val y = e.y
        val x = e.x

        val barWidth = mData!!.barWidth

        val left = x - barWidth / 2f
        val right = x + barWidth / 2f
        val top = if (y >= 0) y else 0f
        val bottom = if (y <= 0) y else 0f

        bounds[left, top, right] = bottom

        getTransformer(set.axisDependency).rectValueToPixel(outputRect)
    }

    /**
     * If set to true, all values are drawn above their bars, instead of below their top.
     *
     * @param enabled
     */
    fun setDrawValueAboveBar(enabled: Boolean) {
        mDrawValueAboveBar = enabled
    }

    /**
     * returns true if drawing values above bars is enabled, false if not
     *
     * @return
     */
    override fun isDrawValueAboveBarEnabled(): Boolean {
        return mDrawValueAboveBar
    }

    /**
     * If set to true, a grey area is drawn behind each bar that indicates the maximum value. Enabling his will reduce
     * performance by about 50%.
     *
     * @param enabled
     */
    fun setDrawBarShadow(enabled: Boolean) {
        mDrawBarShadow = enabled
    }

    /**
     * returns true if drawing shadows (maxvalue) for each bar is enabled, false if not
     *
     * @return
     */
    override fun isDrawBarShadowEnabled(): Boolean {
        return mDrawBarShadow
    }

    /**
     * Set this to true to make the highlight operation full-bar oriented, false to make it highlight single values (relevant
     * only for stacked). If enabled, highlighting operations will highlight the whole bar, even if only a single stack entry
     * was tapped.
     * Default: false
     *
     * @param enabled
     */
    fun setHighlightFullBarEnabled(enabled: Boolean) {
        mHighlightFullBarEnabled = enabled
    }

    /**
     * @return true the highlight operation is be full-bar oriented, false if single-value
     */
    override fun isHighlightFullBarEnabled(): Boolean {
        return mHighlightFullBarEnabled
    }

    /**
     * Highlights the value at the given x-value in the given DataSet. Provide
     * -1 as the dataSetIndex to undo all highlighting.
     *
     * @param x
     * @param dataSetIndex
     * @param stackIndex   the index inside the stack - only relevant for stacked entries
     */
    override fun highlightValue(x: Float, dataSetIndex: Int, stackIndex: Int) {
        highlightValue(Highlight(x, dataSetIndex, stackIndex), false)
    }

    override fun getBarData(): BarData {
        return mData!!
    }

    /**
     * Adds half of the bar width to each side of the x-axis range in order to allow the bars of the barchart to be
     * fully displayed.
     * Default: false
     *
     * @param enabled
     */
    fun setFitBars(enabled: Boolean) {
        mFitBars = enabled
    }

    /**
     * Groups all BarDataSet objects this data object holds together by modifying the x-value of their entries.
     * Previously set x-values of entries will be overwritten. Leaves space between bars and groups as specified
     * by the parameters.
     * Calls notifyDataSetChanged() afterwards.
     *
     * @param fromX      the starting point on the x-axis where the grouping should begin
     * @param groupSpace the space between groups of bars in values (not pixels) e.g. 0.8f for bar width 1f
     * @param barSpace   the space between individual bars in values (not pixels) e.g. 0.1f for bar width 1f
     */
    fun groupBars(fromX: Float, groupSpace: Float, barSpace: Float) {
        if (barData == null) {
            throw RuntimeException("You need to set data for the chart before grouping bars.")
        } else {
            barData.groupBars(fromX, groupSpace, barSpace)
            notifyDataSetChanged()
        }
    }
}
