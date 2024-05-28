package com.github.mikephil.charting.interfaces.datasets

import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.Fill

/**
 * Created by philipp on 21/10/15.
 */
public interface IBarDataSet : IBarLineScatterCandleBubbleDataSet<BarEntry> {

    //val fills: List<Fill?>

    fun getFills(): MutableList<Fill>?

    fun getFill(index: Int): Fill

    /**
     * Returns true if this DataSet is stacked (stacksize > 1) or not.
     *
     * @return
     */

    fun isStacked(): Boolean

    /**
     * Returns the maximum number of bars that can be stacked upon another in
     * this DataSet. This should return 1 for non stacked bars, and > 1 for stacked bars.
     *
     * @return
     */

    fun stackSize(): Int

    /**
     * Returns the color used for drawing the bar-shadows. The bar shadows is a
     * surface behind the bar that indicates the maximum value.
     *
     * @return
     */

    fun barShadowColor(): Int

    /**
     * Returns the width used for drawing borders around the bars.
     * If borderWidth == 0, no border will be drawn.
     *
     * @return
     */

    fun barBorderWidth(): Float

    /**
     * Returns the color drawing borders around the bars.
     *
     * @return
     */

    fun barBorderColor(): Int

    /**
     * Returns the alpha value (transparency) that is used for drawing the
     * highlight indicator.
     *
     * @return
     */
    fun highLightAlpha(): Int


    /**
     * Returns the labels used for the different value-stacks in the legend.
     * This is only relevant for stacked bar entries.
     *
     * @return
     */

    fun stackLabels(): Array<String>
}
