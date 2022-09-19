package io.github.bric3.gctk.app

import org.jfree.chart.ChartFactory
import org.jfree.chart.ChartPanel
import org.jfree.chart.JFreeChart
import org.jfree.chart.plot.PlotOrientation
import org.jfree.data.xy.XYSeriesCollection

class XYPlotViewFactory {
    fun makeChartPanel(): ChartPanel {
        val panel = ChartPanel(makeChart()).also {
            // it.chartRenderingInfo.entityCollection = null
            it.isMouseWheelEnabled = true
            it.chart.also {
                it.xyPlot.isDomainPannable = true
            }
        }

        return panel
    }

    var title = "XY Plot"

    var xAxisLabel = "X"
    var yAxisLabel = "Y"
    var legend = true
    var xySeriesCollection: XYSeriesCollection = XYSeriesCollection()

    private fun makeChart(): JFreeChart {
        return ChartFactory.createScatterPlot(
            title,
            xAxisLabel,
            yAxisLabel,
            xySeriesCollection,
            PlotOrientation.VERTICAL,
            legend,
            true,
            false
        )
    }
}