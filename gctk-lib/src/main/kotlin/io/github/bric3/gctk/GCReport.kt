package io.github.bric3.gctk

import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary
import com.microsoft.gctoolkit.integration.aggregation.PauseTimeSummary
import com.microsoft.gctoolkit.time.DateTimeStamp

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
}
