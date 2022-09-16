package io.github.bric3.gctk

import com.microsoft.gctoolkit.GCToolKit
import com.microsoft.gctoolkit.event.GarbageCollectionTypes
import com.microsoft.gctoolkit.event.GarbageCollectionTypes.DefNew
import com.microsoft.gctoolkit.event.GarbageCollectionTypes.InitialMark
import com.microsoft.gctoolkit.event.GarbageCollectionTypes.Remark
import com.microsoft.gctoolkit.event.GarbageCollectionTypes.Young
import com.microsoft.gctoolkit.integration.aggregation.CollectionCycleCountsSummary
import com.microsoft.gctoolkit.integration.aggregation.HeapOccupancyAfterCollectionSummary
import com.microsoft.gctoolkit.integration.aggregation.PauseTimeSummary
import com.microsoft.gctoolkit.integration.collections.XYDataSet
import com.microsoft.gctoolkit.io.GCLogFile
import com.microsoft.gctoolkit.jvm.JavaVirtualMachine
import java.io.IOException
import java.util.Objects
import java.util.function.Supplier

/**
 * Analyze a gc log file.
 *
 *
 * Note this class is not thread safe.
 */
class Analyzer {
    private val gcToolKit = GCToolKit()

    @Throws(IOException::class)
    fun analyze(logFile: GCLogFile): GCReport {
        Objects.requireNonNull(logFile)
        gcToolKit.registerAggregation(HeapOccupancyAfterCollectionSummary::class.java)
        gcToolKit.registerAggregation(CollectionCycleCountsSummary::class.java)
        gcToolKit.registerAggregation(PauseTimeSummary::class.java)

        val machine = gcToolKit.analyze(logFile)

        val report = GCReport(
            commandLine = machine.commandLine,
            estimatedJVMStartTime = machine.estimatedJVMStartTime,
            timeOfFirstEvent = machine.timeOfFirstEvent,
            runtimeDuration = machine.runtimeDuration,
            gcName = gcName(machine),
        )

        val reportMissingAggregation = Supplier { IllegalStateException("Aggregation not loaded") }

        report.heapOccupancyAfterCollectionSummary = machine.getAggregation(HeapOccupancyAfterCollectionSummary::class.java).orElseThrow(reportMissingAggregation)
        report.collectionCycleCountsSummary = machine.getAggregation(CollectionCycleCountsSummary::class.java).orElseThrow(reportMissingAggregation)
        report.pauseTimeSummary = machine.getAggregation(PauseTimeSummary::class.java).orElseThrow(reportMissingAggregation)

        // val message = "The XYDataSet for %s contains %s items.\n"
        // machine.getAggregation(HeapOccupancyAfterCollectionSummary::class.java)
        //     .map { obj: HeapOccupancyAfterCollectionSummary -> obj.get() }
        //     .ifPresentOrElse({ summary: Map<GarbageCollectionTypes, XYDataSet> ->
        //                          summary.forEach { (gcType: GarbageCollectionTypes, dataSet: XYDataSet) ->
        //                              System.out.printf(message, gcType, dataSet.size())
        //                              when (gcType) {
        //                                  DefNew -> defNewCount = dataSet.size()
        //                                  InitialMark -> initialMarkCount = dataSet.size()
        //                                  Remark -> remarkCount = dataSet.size()
        //                                  Young -> youngCount = dataSet.size()
        //                                  else -> println("$gcType not managed")
        //                              }
        //                          }
        //                      }, reportMissingAggregation)
        // machine
        //     .getAggregation(CollectionCycleCountsSummary::class.java)
        //     .ifPresentOrElse({ s: CollectionCycleCountsSummary -> s.printOn(System.out) }, reportMissingAggregation)
        // machine
        //     .getAggregation(PauseTimeSummary::class.java)
        //     .ifPresentOrElse({ pauseTimeSummary: PauseTimeSummary ->
        //                          System.out.printf("Total pause time  : %.4f%n", pauseTimeSummary.totalPauseTime)
        //                          System.out.printf("Total run time    : %.4f%n", pauseTimeSummary.runtimeDuration)
        //                          System.out.printf("Percent pause time: %.2f%n", pauseTimeSummary.percentPaused)
        //                      }, reportMissingAggregation)

        return report
    }

    private fun gcName(machine: JavaVirtualMachine): String {
        if (machine.isCMS) {
            return "CMS"
        }
        if (machine.isG1GC) {
            return "G1GC"
        }
        if (machine.isParallel) {
            return "ParallelGC"
        }
        if (machine.isSerial) {
            return "SerialGC"
        }
        if (machine.isZGC) {
            return "ZGC"
        }
        return if (machine.isShenandoah) {
            "Shenandoah"
        } else "Unknown"
    }

    private var initialMarkCount = 0
    private var remarkCount = 0
    private var defNewCount = 0
    private var youngCount = 0
}