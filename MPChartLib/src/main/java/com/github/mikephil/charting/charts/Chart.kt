package com.github.mikephil.charting.charts

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Bitmap.CompressFormat
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Paint.Align
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Environment
import android.provider.MediaStore
import android.text.TextUtils
import android.util.AttributeSet
import android.util.Log
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.animation.Easing.EasingFunction
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.ChartData
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.DefaultValueFormatter
import com.github.mikephil.charting.formatter.IValueFormatter
import com.github.mikephil.charting.highlight.ChartHighlighter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.highlight.IHighlighter
import com.github.mikephil.charting.interfaces.dataprovider.ChartInterface
import com.github.mikephil.charting.interfaces.datasets.IDataSet
import com.github.mikephil.charting.listener.ChartTouchListener
import com.github.mikephil.charting.listener.OnChartGestureListener
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.DataRenderer
import com.github.mikephil.charting.renderer.LegendRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Utils
import com.github.mikephil.charting.utils.ViewPortHandler
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import kotlin.math.abs
import kotlin.math.max

/**
 * Baseclass of all Chart-Views.
 *
 * @author Philipp Jahoda
 */
abstract class Chart<T : ChartData<out IDataSet<out Entry>?>?> : ViewGroup, ChartInterface {
    /**
     * Returns true if log-output is enabled for the chart, fals if not.
     *
     * @return
     */
    /**
     * Set this to true to enable logcat outputs for the chart. Beware that
     * logcat output decreases rendering performance. Default: disabled.
     *
     * @param enabled
     */
    /**
     * flag that indicates if logging is enabled or not
     */
    var isLogEnabled: Boolean = false

    /**
     * object that holds all data that was originally set for the chart, before
     * it was modified or any filtering algorithms had been applied
     */
    @JvmField
    protected var mData: T? = null

    /**
     * Returns true if values can be highlighted via tap gesture, false if not.
     *
     * @return
     */
    /**
     * Set this to false to prevent values from being highlighted by tap gesture.
     * Values can still be highlighted via drag or programmatically. Default: true
     *
     * @param enabled
     */
    /**
     * Flag that indicates if highlighting per tap (touch) is enabled
     */
    var isHighlightPerTapEnabled: Boolean = true

    /**
     * If set to true, chart continues to scroll after touch up default: true
     */
    /**
     * If set to true, chart continues to scroll after touch up. Default: true.
     *
     * @param enabled
     */
    /**
     * If set to true, chart continues to scroll after touch up
     */
    var isDragDecelerationEnabled: Boolean = true

    /**
     * Deceleration friction coefficient in [0 ; 1] interval, higher values
     * indicate that speed will decrease slowly, for example if it set to 0, it
     * will stop immediately. 1 is an invalid value, and will be converted to
     * 0.999f automatically.
     */
    private var mDragDecelerationFrictionCoef = 0.9f

    /**
     * default value-formatter, number of digits depends on provided chart-data
     */
    protected var mDefaultValueFormatter: DefaultValueFormatter = DefaultValueFormatter(0)

    /**
     * paint object used for drawing the description text in the bottom right
     * corner of the chart
     */
    protected var mDescPaint: Paint? = null

    /**
     * paint object for drawing the information text when there are no values in
     * the chart
     */
    protected var mInfoPaint: Paint? = null

    /**
     * Returns the object representing all x-labels, this method can be used to
     * acquire the XAxis object and modify it (e.g. change the position of the
     * labels, styling, etc.)
     *
     * @return
     */
    /**
     * the object representing the labels on the x-axis
     */
    public open var xAxis: XAxis? = null
        get() = field
        set(value) {
            field = value
        }


    /**
     * if true, touch gestures are enabled on the chart
     */
    @JvmField
    protected var mTouchEnabled: Boolean = true

    /**
     * Returns the Description object of the chart that is responsible for holding all information related
     * to the description text that is displayed in the bottom right corner of the chart (by default).
     *
     * @return
     */
    /**
     * Sets a new Description object for the chart.
     *
     * @param desc
     */
    /**
     * the object responsible for representing the description text
     */
    var description: Description? = null

    /**
     * Returns the Legend object of the chart. This method can be used to get an
     * instance of the legend in order to customize the automatically generated
     * Legend.
     *
     * @return
     */
    /**
     * the legend object containing all data associated with the legend
     */
    public var legend: Legend? = null
        protected set

    /**
     * listener that is called when a value on the chart is selected
     */
    protected var mSelectionListener: OnChartValueSelectedListener? = null

    /**
     * Returns an instance of the currently active touch listener.
     *
     * @return
     */
    /**
     * Set a new (e.g. custom) ChartTouchListener NOTE: make sure to
     * setTouchEnabled(true); if you need touch gestures on the chart
     *
     * @param l
     */
    var onTouchListener: ChartTouchListener<*>? = null

    /**
     * text that is displayed when the chart is empty
     */
    private var mNoDataText = "No chart data available."

    /**
     * Returns the custom gesture listener.
     *
     * @return
     */
    /**
     * Sets a gesture-listener for the chart for custom callbacks when executing
     * gestures on the chart surface.
     *
     * @param l
     */
    /**
     * Gesture listener for custom callbacks when making gestures on the chart.
     */
    var onChartGestureListener: OnChartGestureListener? = null

    /**
     * Returns the renderer object responsible for rendering / drawing the
     * Legend.
     *
     * @return
     */
    var legendRenderer: LegendRenderer? = null
        protected set

    /**
     * object responsible for rendering the data
     */

    protected lateinit var mRenderer: DataRenderer

    var highlighter: IHighlighter? = null
        protected set

    /**
     * Returns the ViewPortHandler of the chart that is responsible for the
     * content area of the chart and its offsets and dimensions.
     *
     * @return
     */
    /**
     * object that manages the bounds and drawing constraints of the chart
     */
    var viewPortHandler: ViewPortHandler = ViewPortHandler()
        protected set

    /**
     * Returns the animator responsible for animating chart values.
     *
     * @return
     */
    /**
     * object responsible for animations
     */
    lateinit var animator: ChartAnimator
        public set

    /**
     * Extra offsets to be appended to the viewport
     */
    private var mExtraTopOffset = 0f
    private var mExtraRightOffset = 0f
    private var mExtraBottomOffset = 0f
    private var mExtraLeftOffset = 0f

    /**
     * default constructor for initialization in code
     */
    constructor(context: Context?) : super(context) {
        init()
    }

    /**
     * constructor for initialization in xml
     */
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    /**
     * even more awesome constructor
     */
    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle) {
        init()
    }

    /**
     * initialize all paints and stuff
     */
    protected open fun init() {
        setWillNotDraw(false)

        // setLayerType(View.LAYER_TYPE_HARDWARE, null);
        animator = ChartAnimator { // ViewCompat.postInvalidateOnAnimation(Chart.this);
            postInvalidate()
        }

        // initialize the utils
        Utils.init(context)
        mMaxHighlightDistance = Utils.convertDpToPixel(500f)

        description = Description()
        legend = Legend()

        legendRenderer = LegendRenderer(viewPortHandler, legend)

        xAxis = XAxis()

        mDescPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        mInfoPaint = Paint(Paint.ANTI_ALIAS_FLAG)
        mInfoPaint!!.color = Color.rgb(247, 189, 51) // orange
        mInfoPaint!!.textAlign = Align.CENTER
        mInfoPaint!!.textSize = Utils.convertDpToPixel(12f)

        if (isLogEnabled) Log.i("", "Chart.init()")
    }

    // public void initWithDummyData() {
    // ColorTemplate template = new ColorTemplate();
    // template.addColorsForDataSets(ColorTemplate.COLORFUL_COLORS,
    // getContext());
    //
    // setColorTemplate(template);
    // setDrawYValues(false);
    //
    // ArrayList<String> xVals = new ArrayList<String>();
    // Calendar calendar = Calendar.getInstance();
    // for (int i = 0; i < 12; i++) {
    // xVals.add(calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT,
    // Locale.getDefault()));
    // }
    //
    // ArrayList<DataSet> dataSets = new ArrayList<DataSet>();
    // for (int i = 0; i < 3; i++) {
    //
    // ArrayList<Entry> yVals = new ArrayList<Entry>();
    //
    // for (int j = 0; j < 12; j++) {
    // float val = (float) (Math.random() * 100);
    // yVals.add(new Entry(val, j));
    // }
    //
    // DataSet set = new DataSet(yVals, "DataSet " + i);
    // dataSets.add(set); // add the datasets
    // }
    // // create a data object with the datasets
    // ChartData data = new ChartData(xVals, dataSets);
    // setData(data);
    // invalidate();
    // }
    /**
     * Sets a new data object for the chart. The data object contains all values
     * and information needed for displaying.
     *
     * @param data
     */
    open fun setData(data: T) {
        mData = data
        mOffsetsCalculated = false

        if (data == null) {
            return
        }

        // calculate how many digits are needed
        setupDefaultFormatter(data.yMin, data.yMax)

        for (set in mData!!.dataSets) {
            if (set != null) {
                if ( set.needsFormatter() || set.valueFormatter === mDefaultValueFormatter) set.valueFormatter = mDefaultValueFormatter
            }
        }

        // let the chart know there is new data
        notifyDataSetChanged()

        if (isLogEnabled) Log.i(LOG_TAG, "Data is set.")
    }

    /**
     * Clears the chart from all data (sets it to null) and refreshes it (by
     * calling invalidate()).
     */
    fun clear() {
        mData = null
        mOffsetsCalculated = false
        highlighted = null
        onTouchListener!!.setLastHighlighted(null)
        invalidate()
    }

    /**
     * Removes all DataSets (and thereby Entries) from the chart. Does not set the data object to null. Also refreshes the
     * chart by calling invalidate().
     */
    fun clearValues() {
        mData!!.clearValues()
        invalidate()
    }

    val isEmpty: Boolean
        /**
         * Returns true if the chart is empty (meaning it's data object is either
         * null or contains no entries).
         *
         * @return
         */
        get() = if (mData == null) true
        else {
            if (mData!!.entryCount <= 0) true
            else false
        }

    /**
     * Lets the chart know its underlying data has changed and performs all
     * necessary recalculations. It is crucial that this method is called
     * everytime data is changed dynamically. Not calling this method can lead
     * to crashes or unexpected behaviour.
     */
    abstract fun notifyDataSetChanged()

    /**
     * Calculates the offsets of the chart to the border depending on the
     * position of an eventual legend or depending on the length of the y-axis
     * and x-axis labels and their position
     */
    protected abstract fun calculateOffsets()

    /**
     * Calculates the y-min and y-max value and the y-delta and x-delta value
     */
    protected abstract fun calcMinMax()

    /**
     * Calculates the required number of digits for the values that might be
     * drawn in the chart (if enabled), and creates the default-value-formatter
     */
    protected fun setupDefaultFormatter(min: Float, max: Float) {
        var reference = 0f

        reference = if (mData == null || mData!!.entryCount < 2) {
            max(abs(min.toDouble()), abs(max.toDouble())).toFloat()
        } else {
            abs((max - min).toDouble()).toFloat()
        }

        val digits = Utils.getDecimals(reference)

        // setup the formatter with a new number of digits
        mDefaultValueFormatter.setup(digits)
    }

    /**
     * flag that indicates if offsets calculation has already been done or not
     */
    private var mOffsetsCalculated = false

    override fun onDraw(canvas: Canvas) {
        // super.onDraw(canvas);

        if (mData == null) {
            val hasText = !TextUtils.isEmpty(mNoDataText)

            if (hasText) {
                val pt = center

                when (mInfoPaint!!.textAlign) {
                    Align.LEFT -> {
                        pt.x = 0f
                        canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint!!)
                    }

                    Align.RIGHT -> {
                        pt.x *= 2.0.toFloat()
                        canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint!!)
                    }

                    else -> canvas.drawText(mNoDataText, pt.x, pt.y, mInfoPaint!!)
                }
            }

            return
        }

        if (!mOffsetsCalculated) {
            calculateOffsets()
            mOffsetsCalculated = true
        }
    }

    /**
     * Draws the description text in the bottom right corner of the chart (per default)
     */
    protected fun drawDescription(c: Canvas) {
        // check if description should be drawn

        if (description != null && description!!.isEnabled) {
            val position = description!!.position

            mDescPaint!!.setTypeface(description!!.typeface)
            mDescPaint!!.textSize = description!!.textSize
            mDescPaint!!.color = description!!.textColor
            mDescPaint!!.textAlign = description!!.textAlign

            val x: Float
            val y: Float

            // if no position specified, draw on default position
            if (position == null) {
                x = width - viewPortHandler.offsetRight() - description!!.xOffset
                y = height - viewPortHandler.offsetBottom() - description!!.yOffset
            } else {
                x = position.x
                y = position.y
            }

            c.drawText(description!!.text, x, y, mDescPaint!!)
        }
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS CODE FOR HIGHLIGHTING  */
    /**
     * Returns the array of currently highlighted values. This might a null or
     * empty array if nothing is highlighted.
     *
     * @return
     */
    /**
     * array of Highlight objects that reference the highlighted slices in the
     * chart
     */
    var highlighted: Array<Highlight?>? = null
        protected set

    /**
     * The maximum distance in dp away from an entry causing it to highlight.
     */
    protected var mMaxHighlightDistance: Float = 0f

    override fun getMaxHighlightDistance(): Float {
        return mMaxHighlightDistance
    }

    /**
     * Sets the maximum distance in screen dp a touch can be away from an entry to cause it to get highlighted.
     * Default: 500dp
     *
     * @param distDp
     */
    fun setMaxHighlightDistance(distDp: Float) {
        mMaxHighlightDistance = Utils.convertDpToPixel(distDp)
    }

    /**
     * Returns true if there are values to highlight, false if there are no
     * values to highlight. Checks if the highlight array is null, has a length
     * of zero or if the first object is null.
     *
     * @return
     */
    fun valuesToHighlight(): Boolean {
        return if (highlighted == null || highlighted!!.size <= 0 || highlighted!![0] == null) false
        else true
    }

    /**
     * Sets the last highlighted value for the touchlistener.
     *
     * @param highs
     */
    protected fun setLastHighlighted(highs: Array<Highlight?>?) {
        if (highs == null || highs.size <= 0 || highs[0] == null) {
            onTouchListener!!.setLastHighlighted(null)
        } else {
            onTouchListener!!.setLastHighlighted(highs[0])
        }
    }

    /**
     * Highlights the values at the given indices in the given DataSets. Provide
     * null or an empty array to undo all highlighting. This should be used to
     * programmatically highlight values.
     * This method *will not* call the listener.
     *
     * @param highs
     */
    fun highlightValues(highs: Array<Highlight?>?) {
        // set the indices to highlight

        highlighted = highs

        setLastHighlighted(highs)

        // redraw the chart
        invalidate()
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     * @param x The x-value to highlight
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex The data index to search in (only used in CombinedChartView currently)
     */
    open fun highlightValue(x: Float, dataSetIndex: Int, dataIndex: Int) {
        highlightValue(x, dataSetIndex, dataIndex, true)
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * @param x The x-value to highlight
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex The data index to search in (only used in CombinedChartView currently)
     * @param callListener Should the listener be called for this change
     */
    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     * @param x The x-value to highlight
     * @param dataSetIndex The dataset index to search in
     */
    fun highlightValue(x: Float, dataSetIndex: Int, dataIndex: Int, callListener: Boolean) {
        highlightValue(x, Float.NaN, dataSetIndex, dataIndex, callListener)
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * @param x The x-value to highlight
     * @param dataSetIndex The dataset index to search in
     * @param callListener Should the listener be called for this change
     */
    fun highlightValue(x: Float, dataSetIndex: Int, callListener: Boolean) {
        highlightValue(x, Float.NaN, dataSetIndex, -1, callListener)
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * @param x The x-value to highlight
     * @param y The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex The data index to search in (only used in CombinedChartView currently)
     * @param callListener Should the listener be called for this change
     */
    /**
     * Highlights the value at the given x-value and y-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     * @param x The x-value to highlight
     * @param y The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     */
    /**
     * Highlights the value at the given x-value and y-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * This method will call the listener.
     * @param x The x-value to highlight
     * @param y The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param dataIndex The data index to search in (only used in CombinedChartView currently)
     */
    @JvmOverloads
    fun highlightValue(x: Float, y: Float, dataSetIndex: Int, dataIndex: Int = -1, callListener: Boolean = true) {
        if (dataSetIndex < 0 || dataSetIndex >= mData!!.dataSetCount) {
            highlightValue(null, callListener)
        } else {
            highlightValue(Highlight(x, y, dataSetIndex, dataIndex), callListener)
        }
    }

    /**
     * Highlights any y-value at the given x-value in the given DataSet.
     * Provide -1 as the dataSetIndex to undo all highlighting.
     * @param x The x-value to highlight
     * @param y The y-value to highlight. Supply `NaN` for "any"
     * @param dataSetIndex The dataset index to search in
     * @param callListener Should the listener be called for this change
     */
    fun highlightValue(x: Float, y: Float, dataSetIndex: Int, callListener: Boolean) {
        highlightValue(x, y, dataSetIndex, -1, callListener)
    }

    /**
     * Highlights the values represented by the provided Highlight object
     * This method *will not* call the listener.
     *
     * @param highlight contains information about which entry should be highlighted
     */
    fun highlightValue(highlight: Highlight?) {
        highlightValue(highlight, false)
    }

    /**
     * Highlights the value selected by touch gesture. Unlike
     * highlightValues(...), this generates a callback to the
     * OnChartValueSelectedListener.
     *
     * @param high         - the highlight object
     * @param callListener - call the listener
     */
    fun highlightValue(high: Highlight?, callListener: Boolean) {
        var high = high
        var e: Entry? = null

        if (high == null) highlighted = null
        else {
            if (isLogEnabled) Log.i(LOG_TAG, "Highlighted: $high")

            e = mData!!.getEntryForHighlight(high)
            if (e == null) {
                highlighted = null
                high = null
            } else {
                // set the indices to highlight

                highlighted = arrayOf(high
                )
            }
        }

        setLastHighlighted(highlighted)

        if (callListener && mSelectionListener != null) {
            if (!valuesToHighlight()) mSelectionListener!!.onNothingSelected()
            else {
                // notify the listener
                mSelectionListener!!.onValueSelected(e, high)
            }
        }

        // redraw the chart
        invalidate()
    }

    /**
     * Returns the Highlight object (contains x-index and DataSet index) of the
     * selected value at the given touch point inside the Line-, Scatter-, or
     * CandleStick-Chart.
     *
     * @param x
     * @param y
     * @return
     */
    open fun getHighlightByTouchPoint(x: Float, y: Float): Highlight? {
        if (mData == null) {
            Log.e(LOG_TAG, "Can't select by touch. No data set.")
            return null
        } else return highlighter!!.getHighlight(x, y)
    }

    /**
     * ################ ################ ################ ################
     */
    /** BELOW CODE IS FOR THE MARKER VIEW  */
    /**
     * returns true if drawing the marker is enabled when tapping on values
     * (use the setMarker(IMarker marker) method to specify a marker)
     *
     * @return
     */
    /**
     * if set to true, the marker view is drawn when a value is clicked
     */
    var isDrawMarkersEnabled: Boolean = true
        protected set

    /**
     * returns the marker that is set as a marker view for the chart
     *
     * @return
     */
    /**
     * sets the marker that is displayed when a value is clicked on the chart
     *
     * @param marker
     */
    /**
     * the view that represents the marker
     */
    public var marker: IMarker? = null

    /**
     * draws all MarkerViews on the highlighted positions
     */
    protected open fun drawMarkers(canvas: Canvas?) {
        // if there is no marker view or drawing marker is disabled

        if (marker == null || !isDrawMarkersEnabled || !valuesToHighlight()) return

        for (i in highlighted!!.indices) {
            val highlight = highlighted!![i]

            val set = mData!!.getDataSetByIndex(highlight!!.dataSetIndex)!!

           // val e = mData!!.getEntryForHighlight(highlighted!![i])

            val e: Entry = mData!!.getEntryForHighlight(highlight)
            // Make sure `e` is not null before proceeding
            //if (e == null) continue

            val entryIndex = (set as IDataSet<Entry>).getEntryIndex(e)

           // val entryIndex = set.getEntryIndex(e)



            // make sure entry not null
            if (e == null || entryIndex > set.entryCount * animator!!.phaseX) continue

            val pos = getMarkerPosition(highlight)

            // check bounds
            if (!viewPortHandler.isInBounds(pos[0], pos[1])) continue

            // callbacks to update the content
            marker!!.refreshContent(e, highlight)

            // draw the marker
            marker!!.draw(canvas, pos[0], pos[1])
        }
    }

    /**
     * Returns the actual position in pixels of the MarkerView for the given
     * Highlight object.
     *
     * @param high
     * @return
     */
    protected open fun getMarkerPosition(high: Highlight?): FloatArray {
        return floatArrayOf(high!!.drawX, high.drawY)
    }

    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW THIS RELATED TO ANIMATION  */

    var dragDecelerationFrictionCoef: Float
        /**
         * Returns drag deceleration friction coefficient
         *
         * @return
         */
        get() = mDragDecelerationFrictionCoef
        /**
         * Deceleration friction coefficient in [0 ; 1] interval, higher values
         * indicate that speed will decrease slowly, for example if it set to 0, it
         * will stop immediately. 1 is an invalid value, and will be converted to
         * 0.999f automatically.
         *
         * @param newValue
         */
        set(newValue) {
            var newValue = newValue
            if (newValue < 0f) newValue = 0f

            if (newValue >= 1f) newValue = 0.999f

            mDragDecelerationFrictionCoef = newValue
        }

    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR PROVIDING EASING FUNCTIONS  */
    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillisX
     * @param durationMillisY
     * @param easingX         a custom easing function to be used on the animation phase
     * @param easingY         a custom easing function to be used on the animation phase
     */
    @RequiresApi(11)
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easingX: EasingFunction?,
                  easingY: EasingFunction?) {
        animator!!.animateXY(durationMillisX, durationMillisY, easingX!!, easingY!!)
    }

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillisX
     * @param durationMillisY
     * @param easing         a custom easing function to be used on the animation phase
     */
    @RequiresApi(11)
    fun animateXY(durationMillisX: Int, durationMillisY: Int, easing: EasingFunction?) {
        animator!!.animateXY(durationMillisX, durationMillisY, easing!!)
    }

    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillis
     * @param easing         a custom easing function to be used on the animation phase
     */
    @RequiresApi(11)
    fun animateX(durationMillis: Int, easing: EasingFunction?) {
        animator!!.animateX(durationMillis, easing!!)
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillis
     * @param easing         a custom easing function to be used on the animation phase
     */
    @RequiresApi(11)
    fun animateY(durationMillis: Int, easing: EasingFunction?) {
        animator!!.animateY(durationMillis, easing!!)
    }

    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR PREDEFINED EASING OPTIONS  */
    /**
     * ################ ################ ################ ################
     * ANIMATIONS ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     */
    /** CODE BELOW FOR ANIMATIONS WITHOUT EASING  */
    /**
     * Animates the rendering of the chart on the x-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillis
     */
    @RequiresApi(11)
    fun animateX(durationMillis: Int) {
        animator!!.animateX(durationMillis)
    }

    /**
     * Animates the rendering of the chart on the y-axis with the specified
     * animation time. If animate(...) is called, no further calling of
     * invalidate() is necessary to refresh the chart. ANIMATIONS ONLY WORK FOR
     * API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillis
     */
    fun animateY(durationMillis: Int) {
        animator!!.animateY(durationMillis)
    }

    /**
     * Animates the drawing / rendering of the chart on both x- and y-axis with
     * the specified animation time. If animate(...) is called, no further
     * calling of invalidate() is necessary to refresh the chart. ANIMATIONS
     * ONLY WORK FOR API LEVEL 11 (Android 3.0.x) AND HIGHER.
     *
     * @param durationMillisX
     * @param durationMillisY
     */
    fun animateXY(durationMillisX: Int, durationMillisY: Int) {
        animator!!.animateXY(durationMillisX, durationMillisY)
    }


    /**
     * ################ ################ ################ ################
     */
    /** BELOW THIS ONLY GETTERS AND SETTERS  */

    /**
     * Returns the default IValueFormatter that has been determined by the chart
     * considering the provided minimum and maximum values.
     *
     * @return
     */
    override fun getDefaultValueFormatter(): IValueFormatter {
        return mDefaultValueFormatter
    }

    /**
     * set a selection listener for the chart
     *
     * @param l
     */
    fun setOnChartValueSelectedListener(l: OnChartValueSelectedListener?) {
        this.mSelectionListener = l
    }

    val yMax: Float
        /**
         * returns the current y-max value across all DataSets
         *
         * @return
         */
        get() = mData!!.yMax

    val yMin: Float
        /**
         * returns the current y-min value across all DataSets
         *
         * @return
         */
        get() = mData!!.yMin

    override fun getXChartMax(): Float {
        return xAxis!!.mAxisMaximum
    }

    override fun getXChartMin(): Float {
        return xAxis!!.mAxisMinimum
    }

    override fun getXRange(): Float {
        return xAxis!!.mAxisRange
    }

    val center: MPPointF
        /**
         * Returns a recyclable MPPointF instance.
         * Returns the center point of the chart (the whole View) in pixels.
         *
         * @return
         */
        get() = MPPointF.getInstance(width / 2f, height / 2f)

    /**
     * Returns a recyclable MPPointF instance.
     * Returns the center of the chart taking offsets under consideration.
     * (returns the center of the content rectangle)
     *
     * @return
     */
    override fun getCenterOffsets(): MPPointF {
        return viewPortHandler.contentCenter
    }

    /**
     * Sets extra offsets (around the chart view) to be appended to the
     * auto-calculated offsets.
     *
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    fun setExtraOffsets(left: Float, top: Float, right: Float, bottom: Float) {
        extraLeftOffset = left
        extraTopOffset = top
        extraRightOffset = right
        extraBottomOffset = bottom
    }

    var extraTopOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's top
         */
        get() = mExtraTopOffset
        /**
         * Set an extra offset to be appended to the viewport's top
         */
        set(offset) {
            mExtraTopOffset = Utils.convertDpToPixel(offset)
        }

    var extraRightOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's right
         */
        get() = mExtraRightOffset
        /**
         * Set an extra offset to be appended to the viewport's right
         */
        set(offset) {
            mExtraRightOffset = Utils.convertDpToPixel(offset)
        }

    var extraBottomOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's bottom
         */
        get() = mExtraBottomOffset
        /**
         * Set an extra offset to be appended to the viewport's bottom
         */
        set(offset) {
            mExtraBottomOffset = Utils.convertDpToPixel(offset)
        }

    var extraLeftOffset: Float
        /**
         * @return the extra offset to be appended to the viewport's left
         */
        get() = mExtraLeftOffset
        /**
         * Set an extra offset to be appended to the viewport's left
         */
        set(offset) {
            mExtraLeftOffset = Utils.convertDpToPixel(offset)
        }

    /**
     * Sets the text that informs the user that there is no data available with
     * which to draw the chart.
     *
     * @param text
     */
    fun setNoDataText(text: String) {
        mNoDataText = text
    }

    /**
     * Sets the color of the no data text.
     *
     * @param color
     */
    fun setNoDataTextColor(color: Int) {
        mInfoPaint!!.color = color
    }

    /**
     * Sets the typeface to be used for the no data text.
     *
     * @param tf
     */
    fun setNoDataTextTypeface(tf: Typeface?) {
        mInfoPaint!!.setTypeface(tf)
    }

    /**
     * alignment of the no data text
     *
     * @param align
     */
    fun setNoDataTextAlignment(align: Align?) {
        mInfoPaint!!.textAlign = align
    }

    /**
     * Set this to false to disable all gestures and touches on the chart,
     * default: true
     *
     * @param enabled
     */
    fun setTouchEnabled(enabled: Boolean) {
        this.mTouchEnabled = enabled
    }

    @get:Deprecated("")
    @set:Deprecated("")
    var markerView: IMarker?
        get() = marker
        set(v) {
            marker = v
        }

    /**
     * Returns the rectangle that defines the borders of the chart-value surface
     * (into which the actual values are drawn).
     *
     * @return
     */
    override fun getContentRect(): RectF {
        return viewPortHandler.contentRect
    }

    /**
     * disables intercept touchevents
     */
    fun disableScroll() {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(true)
    }

    /**
     * enables intercept touchevents
     */
    fun enableScroll() {
        val parent = parent
        parent?.requestDisallowInterceptTouchEvent(false)
    }

    /**
     * set a new paint object for the specified parameter in the chart e.g.
     * Chart.PAINT_VALUES
     *
     * @param p     the new paint object
     * @param which Chart.PAINT_VALUES, Chart.PAINT_GRID, Chart.PAINT_VALUES,
     * ...
     */
    open fun setPaint(p: Paint?, which: Int) {
        when (which) {
            PAINT_INFO -> mInfoPaint = p
            PAINT_DESCRIPTION -> mDescPaint = p
        }
    }

    /**
     * Returns the paint object associated with the provided constant.
     *
     * @param which e.g. Chart.PAINT_LEGEND_LABEL
     * @return
     */
    open fun getPaint(which: Int): Paint? {
        when (which) {
            PAINT_INFO -> return mInfoPaint
            PAINT_DESCRIPTION -> return mDescPaint
        }
        return null
    }

    @get:Deprecated("")
    val isDrawMarkerViewsEnabled: Boolean
        get() = isDrawMarkersEnabled

    @Deprecated("")
    fun setDrawMarkerViews(enabled: Boolean) {
        setDrawMarkers(enabled)
    }

    /**
     * Set this to true to draw a user specified marker when tapping on
     * chart values (use the setMarker(IMarker marker) method to specify a
     * marker). Default: true
     *
     * @param enabled
     */
    fun setDrawMarkers(enabled: Boolean) {
        isDrawMarkersEnabled = enabled
    }

    /**
     * Returns the ChartData object that has been set for the chart.
     *
     * @return
     */
    override fun getData(): T? {
        return mData
    }

    var renderer: DataRenderer?
        /**
         * Returns the Renderer object the chart uses for drawing data.
         *
         * @return
         */
        get() = mRenderer
        /**
         * Sets a new DataRenderer object for the chart.
         *
         * @param renderer
         */
        set(renderer) {
            if (renderer != null) mRenderer = renderer
        }

    /**
     * Sets a custom highligher object for the chart that handles / processes
     * all highlight touch events performed on the chart-view.
     *
     * @param highlighter
     */
    fun setHighlighter(highlighter: ChartHighlighter<*>?) {
        this.highlighter = highlighter
    }

    /**
     * Returns a recyclable MPPointF instance.
     *
     * @return
     */
    override fun getCenterOfView(): MPPointF {
        return center
    }

    val chartBitmap: Bitmap
        /**
         * Returns the bitmap that represents the chart.
         *
         * @return
         */
        get() {
            // Define a bitmap with the same size as the view
            val returnedBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
            // Bind a canvas to it
            val canvas = Canvas(returnedBitmap)
            // Get the view's background
            val bgDrawable = background
            if (bgDrawable != null) // has background drawable, then draw it on the canvas
                bgDrawable.draw(canvas)
            else  // does not have background drawable, then draw white background on
            // the canvas
                canvas.drawColor(Color.WHITE)
            // draw the view on the canvas
            draw(canvas)
            // return the bitmap
            return returnedBitmap
        }

    /**
     * Saves the current chart state with the given name to the given path on
     * the sdcard leaving the path empty "" will put the saved file directly on
     * the SD card chart is saved as a PNG image, example:
     * saveToPath("myfilename", "foldername1/foldername2");
     *
     * @param title
     * @param pathOnSD e.g. "folder1/folder2/folder3"
     * @return returns true on success, false on error
     */
    fun saveToPath(title: String, pathOnSD: String): Boolean {
        val b = chartBitmap

        var stream: OutputStream? = null
        try {
            stream = FileOutputStream(Environment.getExternalStorageDirectory().path
                    + pathOnSD + "/" + title
                    + ".png")

            /*
             * Write bitmap to file using JPEG or PNG and 40% quality hint for
             * JPEG.
             */
            b.compress(CompressFormat.PNG, 40, stream)

            stream.close()
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

        return true
    }

    /**
     * Saves the current state of the chart to the gallery as an image type. The
     * compression must be set for JPEG only. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName        e.g. "my_image"
     * @param subFolderPath   e.g. "ChartPics"
     * @param fileDescription e.g. "Chart details"
     * @param format          e.g. Bitmap.CompressFormat.PNG
     * @param quality         e.g. 50, min = 0, max = 100
     * @return returns true if saving was successful, false if not
     */
    /**
     * Saves the current state of the chart to the gallery as a PNG image.
     * NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @return returns true if saving was successful, false if not
     */
    @JvmOverloads
    fun saveToGallery(fileName: String, subFolderPath: String = "", fileDescription: String? = "MPAndroidChart-Library Save", format: CompressFormat? = CompressFormat.PNG, quality: Int = 40): Boolean {
        // restrain quality
        var fileName = fileName
        var quality = quality
        if (quality < 0 || quality > 100) quality = 50

        val currentTime = System.currentTimeMillis()

        val extBaseDir = Environment.getExternalStorageDirectory()
        val file = File(extBaseDir.absolutePath + "/DCIM/" + subFolderPath)
        if (!file.exists()) {
            if (!file.mkdirs()) {
                return false
            }
        }

        var mimeType = ""
        when (format) {
            CompressFormat.PNG -> {
                mimeType = "image/png"
                if (!fileName.endsWith(".png")) fileName += ".png"
            }

            CompressFormat.WEBP -> {
                mimeType = "image/webp"
                if (!fileName.endsWith(".webp")) fileName += ".webp"
            }

            CompressFormat.JPEG -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) fileName += ".jpg"
            }

            else -> {
                mimeType = "image/jpeg"
                if (!(fileName.endsWith(".jpg") || fileName.endsWith(".jpeg"))) fileName += ".jpg"
            }
        }
        val filePath = file.absolutePath + "/" + fileName
        var out: FileOutputStream? = null
        try {
            out = FileOutputStream(filePath)

            val b = chartBitmap
            b.compress(format!!, quality, out)

            out.flush()
            out.close()
        } catch (e: IOException) {
            e.printStackTrace()

            return false
        }

        val size = File(filePath).length()

        val values = ContentValues(8)

        // store the details
        values.put(MediaStore.Images.Media.TITLE, fileName)
        values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        values.put(MediaStore.Images.Media.DATE_ADDED, currentTime)
        values.put(MediaStore.Images.Media.MIME_TYPE, mimeType)
        values.put(MediaStore.Images.Media.DESCRIPTION, fileDescription)
        values.put(MediaStore.Images.Media.ORIENTATION, 0)
        values.put(MediaStore.Images.Media.DATA, filePath)
        values.put(MediaStore.Images.Media.SIZE, size)

        return context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) != null
    }

    /**
     * Saves the current state of the chart to the gallery as a JPEG image. The
     * filename and compression can be set. 0 == maximum compression, 100 = low
     * compression (high quality). NOTE: Needs permission WRITE_EXTERNAL_STORAGE
     *
     * @param fileName e.g. "my_image"
     * @param quality  e.g. 50, min = 0, max = 100
     * @return returns true if saving was successful, false if not
     */
    fun saveToGallery(fileName: String, quality: Int): Boolean {
        return saveToGallery(fileName, "", "MPAndroidChart-Library Save", CompressFormat.PNG, quality)
    }

    /**
     * Returns all jobs that are scheduled to be executed after
     * onSizeChanged(...).
     *
     * @return
     */
    /**
     * tasks to be done after the view is setup
     */
    var jobs: ArrayList<Runnable> = ArrayList()
        protected set

    fun removeViewportJob(job: Runnable) {
        jobs.remove(job)
    }

    fun clearAllViewportJobs() {
        jobs.clear()
    }

    /**
     * Either posts a job immediately if the chart has already setup it's
     * dimensions or adds the job to the execution queue.
     *
     * @param job
     */
    fun addViewportJob(job: Runnable) {
        if (viewPortHandler.hasChartDimens()) {
            post(job)
        } else {
            jobs.add(job)
        }
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        for (i in 0 until childCount) {
            getChildAt(i).layout(left, top, right, bottom)
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        val size = Utils.convertDpToPixel(50f).toInt()
        setMeasuredDimension(
                max(suggestedMinimumWidth.toDouble(),
                        resolveSize(size,
                                widthMeasureSpec).toDouble()).toInt(),
                max(suggestedMinimumHeight.toDouble(),
                        resolveSize(size,
                                heightMeasureSpec).toDouble()).toInt())
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        if (isLogEnabled) Log.i(LOG_TAG, "OnSizeChanged()")

        if (w > 0 && h > 0 && w < 10000 && h < 10000) {
            if (isLogEnabled) Log.i(LOG_TAG, "Setting chart dimens, width: $w, height: $h")
            viewPortHandler.setChartDimens(w.toFloat(), h.toFloat())
        } else {
            if (isLogEnabled) Log.w(LOG_TAG, "*Avoiding* setting chart dimens! width: $w, height: $h")
        }

        // This may cause the chart view to mutate properties affecting the view port --
        //   lets do this before we try to run any pending jobs on the view port itself
        notifyDataSetChanged()

        for (r in jobs) {
            post(r)
        }

        jobs.clear()

        super.onSizeChanged(w, h, oldw, oldh)
    }

    /**
     * Setting this to true will set the layer-type HARDWARE for the view, false
     * will set layer-type SOFTWARE.
     *
     * @param enabled
     */
    fun setHardwareAccelerationEnabled(enabled: Boolean) {
        if (enabled) setLayerType(LAYER_TYPE_HARDWARE, null)
        else setLayerType(LAYER_TYPE_SOFTWARE, null)
    }

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()

        //Log.i(LOG_TAG, "Detaching...");
        if (mUnbind) unbindDrawables(this)
    }

    /**
     * unbind flag
     */
    private var mUnbind = false

    /**
     * Unbind all drawables to avoid memory leaks.
     * Link: http://stackoverflow.com/a/6779164/1590502
     *
     * @param view
     */
    private fun unbindDrawables(view: View) {
        if (view.background != null) {
            view.background.callback = null
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                unbindDrawables(view.getChildAt(i))
            }
            view.removeAllViews()
        }
    }

    /**
     * Set this to true to enable "unbinding" of drawables. When a View is detached
     * from a window. This helps avoid memory leaks.
     * Default: false
     * Link: http://stackoverflow.com/a/6779164/1590502
     *
     * @param enabled
     */
    fun setUnbindEnabled(enabled: Boolean) {
        this.mUnbind = enabled
    }

    companion object {
        const val LOG_TAG: String = "MPAndroidChart"

        /**
         * paint for the grid background (only line and barchart)
         */
        const val PAINT_GRID_BACKGROUND: Int = 4

        /**
         * paint for the info text that is displayed when there are no values in the
         * chart
         */
        const val PAINT_INFO: Int = 7

        /**
         * paint for the description text in the bottom right corner
         */
        const val PAINT_DESCRIPTION: Int = 11

        /**
         * paint for the hole in the middle of the pie chart
         */
        const val PAINT_HOLE: Int = 13

        /**
         * paint for the text in the middle of the pie chart
         */
        const val PAINT_CENTER_TEXT: Int = 14

        /**
         * paint used for the legend
         */
        const val PAINT_LEGEND_LABEL: Int = 18
    }
}
