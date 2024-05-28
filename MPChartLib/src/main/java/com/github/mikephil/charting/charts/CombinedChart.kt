package com.github.mikephil.charting.charts

import android.content.Context
import android.graphics.Canvas
import android.util.AttributeSet
import android.util.Log
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BubbleData
import com.github.mikephil.charting.data.CandleData
import com.github.mikephil.charting.data.CombinedData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.ScatterData
import com.github.mikephil.charting.highlight.CombinedHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.dataprovider.CombinedDataProvider
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.renderer.CombinedChartRenderer
import java.util.Objects

/**
 * This chart class allows the combination of lines, bars, scatter and candle
 * data all displayed in one chart area.
 *
 * @author Philipp Jahoda
 */
class CombinedChart : BarLineChartBase<CombinedData?>, CombinedDataProvider {
    /**
     * if set to true, all values are drawn above their bars, instead of below
     * their top
     */
    private var mDrawValueAboveBar = true


    /**
     * flag that indicates whether the highlight should be full-bar oriented, or single-value?
     */
    protected var mHighlightFullBarEnabled: Boolean = false

    /**
     * if set to true, a grey area is drawn behind each bar that indicates the
     * maximum value
     */
    private var mDrawBarShadow = false

    protected lateinit var mDrawOrder: Array<DrawOrder>

    /**
     * enum that allows to specify the order in which the different data objects
     * for the combined-chart are drawn
     */
    enum class DrawOrder {
        BAR, BUBBLE, LINE, CANDLE, SCATTER
    }

    constructor(context: Context?) : super(context)

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun init() {
        super.init()

        // Default values are not ready here yet
        mDrawOrder = arrayOf(DrawOrder.BAR, DrawOrder.BUBBLE, DrawOrder.LINE, DrawOrder.CANDLE, DrawOrder.SCATTER
        )

        setHighlighter(CombinedHighlighter(this, this))

        // Old default behaviour
        isHighlightFullBarEnabled = true

        mRenderer = CombinedChartRenderer(this, animator, viewPortHandler)
    }

    override fun getCombinedData(): CombinedData {
        return mData!!
    }

    override fun setData(data: CombinedData?) {
        super.setData(data)
        setHighlighter(CombinedHighlighter(this, this))
        (mRenderer as CombinedChartRenderer).createRenderers()
        (mRenderer as CombinedChartRenderer).initBuffers()
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the selected value at the given touch
     * point
     * inside the CombinedChart.
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

    override fun getLineData(): LineData? {
        if (mData == null) return null
        return mData!!.lineData
    }

    override fun getBarData(): BarData? {
        if (mData == null) return null
        return mData!!.barData
    }

    override fun getScatterData(): ScatterData? {
        if (mData == null) return null
        return mData!!.scatterData
    }

    override fun getCandleData(): CandleData? {
        if (mData == null) return null
        return mData!!.candleData
    }

    override fun getBubbleData(): BubbleData? {
        if (mData == null) return null
        return mData!!.bubbleData
    }

    override fun isDrawBarShadowEnabled(): Boolean {
        return mDrawBarShadow
    }

    override fun isDrawValueAboveBarEnabled(): Boolean {
        return mDrawValueAboveBar
    }

    /**
     * If set to true, all values are drawn above their bars, instead of below
     * their top.
     *
     * @param enabled
     */
    fun setDrawValueAboveBar(enabled: Boolean) {
        mDrawValueAboveBar = enabled
    }


    /**
     * If set to true, a grey area is drawn behind each bar that indicates the
     * maximum value. Enabling his will reduce performance by about 50%.
     *
     * @param enabled
     */
    fun setDrawBarShadow(enabled: Boolean) {
        mDrawBarShadow = enabled
    }

    /**
     * Set this to true to make the highlight operation full-bar oriented,
     * false to make it highlight single values (relevant only for stacked).
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

    var drawOrder: Array<DrawOrder>?
        /**
         * Returns the currently set draw order.
         *
         * @return
         */
        get() = mDrawOrder
        /**
         * Sets the order in which the provided data objects should be drawn. The
         * earlier you place them in the provided array, the further they will be in
         * the background. e.g. if you provide new DrawOrer[] { DrawOrder.BAR,
         * DrawOrder.LINE }, the bars will be drawn behind the lines.
         *
         * @param order
         */
        set(order) {
            if (order == null || order.size <= 0) return
            mDrawOrder = order
        }

    /**
     * draws all MarkerViews on the highlighted positions
     */
    override fun drawMarkers(canvas: Canvas?) {
        // if there is no marker view or drawing marker is disabled

        if (marker == null || !isDrawMarkersEnabled || !valuesToHighlight()) return

        for (i in Objects.requireNonNull<Array<Highlight?>>(highlighted).indices) {
            val highlight = highlighted!![i]!!

            val set: IDataSet<*> = mData!!.getDataSetByHighlight(highlight)

            val e = mData!!.getEntryForHighlight(highlight) ?: continue

            val entryIndex = (set as IDataSet<Entry>).getEntryIndex(e)

            // make sure entry not null
            if (entryIndex > set.entryCount * animator!!.phaseX) continue

            val pos = getMarkerPosition(highlight)

            // check bounds
            if (!viewPortHandler.isInBounds(pos[0], pos[1])) continue

            // callbacks to update the content
            marker!!.refreshContent(e, highlight)

            // draw the marker
            marker!!.draw(canvas, pos[0], pos[1])
        }
    }
}
