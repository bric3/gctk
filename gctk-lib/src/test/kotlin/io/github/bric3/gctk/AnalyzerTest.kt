package io.github.bric3.gctk

import com.microsoft.gctoolkit.io.RotatingGCLogFile
import com.microsoft.gctoolkit.io.SingleGCLogFile
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class AnalyzerTest {
    @Test
    @Disabled
    fun `exercise analyzer with rotating gc logs`() {
        ProcessHandle.current().info().commandLine().ifPresent { println(it) }
        val rotatingGCLogFile = RotatingGCLogFile(TestLogFile("fixed-gc-logs-2020-05-29_18-13-46.log").logFilePath)
        rotatingGCLogFile.orderedGarbageCollectionLogFiles.forEach { println(it) }

        Analyzer().analyze(
            rotatingGCLogFile
        )
    }

    @Test
    fun `exercise analyzer single gc log`() {
        Analyzer().analyze(
            SingleGCLogFile(TestLogFile("fixed-gc-logs-2020-05-29_18-13-46.log").logFilePath)
        )
    }
}