package io.github.bric3.gctk

import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary
import com.microsoft.gctoolkit.integration.aggregation.PauseTimeSummary
import com.microsoft.gctoolkit.integration.collections.XYDataSet
import com.microsoft.gctoolkit.time.DateTimeStamp
import io.github.bric3.gctk.app.units.Size
import io.github.bric3.gctk.app.units.SizeUnit

class GCReport(
    val commandLine: String? = null,
    val estimatedJVMStartTime: DateTimeStamp? = null,
    val timeOfFirstEvent: DateTimeStamp? = null,
    val runtimeDuration: Double,
    val gcName: String,
) {
    var pauseTimeSummary: PauseTimeSummary? = null
    var collectionCycleCountsSummary: CollectionCycleCountsSummary? = null
    var heapOccupancyAfterCollectionSummary: HeapOccupancyAfterCollectionSummary? = null

    fun heapOccupancyAfterGC(): List<XYDataSet> {
        val allSeries = heapOccupancyAfterCollectionSummary!!.get().values.stream().toList()

        val maxY = allSeries.maxOf { xyDataSet ->
            xyDataSet.maxOfY().orElse(0.0)
        }

        val measure = Size(maxY, SizeUnit.KiBi)

        return allSeries.map { xyDataSet ->
            xyDataSet.scaleSeries(measure.scaleFactor())
        }
    }
}
