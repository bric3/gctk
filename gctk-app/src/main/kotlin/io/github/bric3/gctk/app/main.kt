package io.github.bric3.gctk.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Button
import androidx.compose.material.MaterialTheme
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.awt.SwingPanel
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.microsoft.gctoolkit.io.GCLogFile
import com.microsoft.gctoolkit.io.RotatingGCLogFile
import com.microsoft.gctoolkit.io.SingleGCLogFile
import io.github.bric3.gctk.Analyzer
import io.github.bric3.gctk.GCReport
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import org.jfree.chart.JFreeChart
import org.jfree.chart.axis.DateAxis
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer
import org.jfree.data.xy.XYDataItem
import org.jfree.data.xy.XYSeries
import org.jfree.data.xy.XYSeriesCollection
import java.awt.BorderLayout
import java.awt.Color
import java.awt.Component
import java.io.File
import java.util.Arrays
import javax.swing.*
import javax.swing.filechooser.FileFilter
import javax.swing.filechooser.FileSystemView

// In IntelliJ run with the gradle task `:gctk-app:run` otherwise
// gradle's jvmArgs are not passed to the JVM
fun main() {
    ProcessHandle.current().info().command().ifPresent(::println)
    ProcessHandle.current().info().arguments().stream().flatMap(Arrays::stream).forEach(::println)
    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName())

    application {
        val gcFilename = remember { mutableStateOf(null as GCLogFile?) }

        Window(
            title = """${extractFileName(gcFilename)}GCTK""",
            state = rememberWindowState(size = DpSize(900.dp, 700.dp)),
            onCloseRequest = ::exitApplication
        ) {
            App(gcFilename)
        }
    }
}

private fun extractFileName(gcFilename: MutableState<GCLogFile?>): String {
    val gcLogFile = gcFilename.value ?: return ""

    return buildString {
        append(gcLogFile.path.fileName)

        val hasContainer = gcLogFile.metaData.isZip || gcLogFile.metaData.isGZip || gcLogFile.metaData.isDirectory
        val numberOfFiles = gcLogFile.metaData.numberOfFiles
        if (hasContainer || numberOfFiles > 1) {
            append(" (")
            append(numberOfFiles)
            append(")")
        }
        append(" - ")
    }
}

@Composable
fun App(gcFilePath: MutableState<GCLogFile?>) {
    val gcReport = remember { mutableStateOf(null as GCReport?) }

    MaterialTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedTextField(
                value = if (gcFilePath.value != null) gcFilePath.value.toString() else "",
                onValueChange = { },
                readOnly = true,
                label = { Text("Selected GC File") },
                modifier = Modifier.fillMaxWidth().padding(5.dp),
                singleLine = true,
                trailingIcon = {
                    Button(
                        onClick = {
                            gcLogFileChooserDialog("Select GC File") {
                                gcFilePath.value = it
                                GlobalScope.launch {
                                    gcReport.value = Analyzer().analyze(it)
                                }
                            }
                        },
                        modifier = Modifier.padding(5.dp)
                    ) {
                        Text("Open")
                    }
                }
            )

            if (gcReport.value != null) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(5.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(top = 20.dp, bottom = 20.dp)
                    ) {
                        ReportFieldText("Command line", gcReport.value?.commandLine ?: "N/A")
                        ReportFieldText("Estimated JVM start", gcReport.value?.estimatedJVMStartTime)
                        ReportFieldText("First event", gcReport.value?.timeOfFirstEvent)
                        ReportFieldText("Runtime duration", "%.4f".format(gcReport.value?.runtimeDuration))
                        ReportFieldText("GC", gcReport.value?.gcName)

                        Spacer(modifier = Modifier.height(10.dp))

                        SwingPanel(
                            modifier = Modifier.fillMaxSize(),
                            factory = {
                                JPanel().apply {
                                    layout = BoxLayout(this, BoxLayout.Y_AXIS)
                                }
                            },
                            update = displayReport(gcReport)
                        )
                    }
                }
            }
        }
    }
}

fun displayReport(gcReportState: MutableState<GCReport?>): (JPanel) -> Unit {
    return displayReportLambda@ {
        it.removeAll()
        val gcReport = gcReportState.value ?: return@displayReportLambda

        val heapOccupancyAfterGC = gcReport.heapOccupancyAfterGC()

        val convertedXYSeries = heapOccupancyAfterGC
            .map { xyDataSet ->
                XYSeries("Heap After GC").apply {
                    xyDataSet.items.forEach { item -> add(XYDataItem(item.x, item.y)) }
                }
            }
            .fold(XYSeriesCollection()) { acc, xySeries -> acc.apply { addSeries(xySeries) } }

        val chartPanel = XYPlotViewFactory().apply {
            title = "Heap occupancy after GC"
            xAxisLabel = "Time"
            yAxisLabel = "Occupancy"
            xySeriesCollection = convertedXYSeries
        }.makeChartPanel()

        it.add(chartPanel)
    }
}

fun formatPlot(chart: JFreeChart) {
    var useDates = true

    val plot = chart.xyPlot
    if (useDates) {
        val dateAxis = DateAxis()
        plot.domainAxis = dateAxis
    }
    plot.backgroundPaint = Color.WHITE
    plot.domainGridlinePaint = Color.GRAY
    plot.rangeGridlinePaint = Color.GRAY
    // if (this.properties.getLogDuration() != null) {
    //     val end: Double
    //     val start: Double
    //     if (useDates) {
    //         start = this.properties.getDateTimeMap().getDateStamp().getMillis()
    //         end = this.properties.getDateTimeMap().getGcEndDateTime().getMillis()
    //     } else {
    //         start = this.properties.getDateTimeMap().getStartTime()
    //         end = this.properties.getDateTimeMap().getLastKnownTime()
    //     }
    //     val margin = (end - start) * 0.02
    //     plot.domainAxis.lowerBound = Math.max(0.0, start - margin)
    //     plot.domainAxis.upperBound = end + margin
    // }
    val renderer = plot.renderer
    if (renderer is XYLineAndShapeRenderer) {
        renderer.drawOutlines = false
    }
    plot.domainAxis.isAutoRange = true
    plot.rangeAxis.isAutoRange = true
}

@Composable
private fun ReportFieldText(label: String, value: Any?, charEndPadding : Int = 20) {
    val annotatedString = AnnotatedString.Builder().apply {
        pushStyle(SpanStyle(fontFamily = FontFamily.Monospace))
        append(label.padEnd(charEndPadding) + " : ")
        pushStyle(SpanStyle(fontWeight = FontWeight.Bold))
        append(value.toString())
        pop()
    }.toAnnotatedString()
    return Text(annotatedString)
}

@Composable
fun Button(text: String = "", action: (() -> Unit)) {
    Button(
        modifier = Modifier.size(270.dp, 30.dp),
        onClick = { action.invoke() }
    ) {
        Text(text)
    }
}

fun actionButton(
    text: String,
    action: () -> Unit,
): JButton {
    return JButton(text).apply {
        alignmentX = Component.CENTER_ALIGNMENT
        addActionListener { action() }
    }
}

/**
 * Opens a file chooser dialog and returns a selected file path or null.
 *
 * @see [JetBrains/compose-jb#1003](https://github.com/JetBrains/compose-jb/issues/1003)
 */
fun gcLogFileChooserDialog(
    title: String,
    onFileSelected: (GCLogFile) -> Unit,
) {
    val gcFileOpenMode = ButtonGroup()
    val fileChooser = JFileChooser(FileSystemView.getFileSystemView()).apply {
        addChoosableFileFilter(object : FileFilter() {
            var extensions = arrayOf(
                "log",
                "txt",
                "log.0",
                "log.1",
                "log.2",
                "log.3",
                "log.4",
                "log.5",
                "log.6",
                "log.7",
                "log.8",
                "log.9",
                "zip",
            )

            override fun accept(file: File?): Boolean {
                if (file == null) return false

                if (file.isDirectory) {
                    return true
                }
                val fileName = file.name
                for (extension in extensions) {
                    if (fileName.endsWith(extension, true)) {
                        return true
                    }
                }
                return false
            }

            override fun getDescription(): String {
                return "GC Logs, (.log, .txt), directories, zipped logs"
            }
        })
        currentDirectory = File(System.getProperty("user.dir"))
        dialogTitle = title
        isAcceptAllFileFilterUsed = true
        selectedFile = null

        val chooser = this
        accessory = JPanel().also {
            it.layout = BoxLayout(it, BoxLayout.Y_AXIS)

            JToggleButton("Single file").apply {
                actionCommand = "single"
                addActionListener { chooser.fileSelectionMode = JFileChooser.FILES_ONLY }
                gcFileOpenMode.add(this)
                isSelected = true
                chooser.fileSelectionMode = JFileChooser.FILES_ONLY

                it.add(this, BorderLayout.NORTH)
            }

            JToggleButton("Rotating logs").apply {
                actionCommand = "rotating"
                addActionListener { chooser.fileSelectionMode = JFileChooser.FILES_AND_DIRECTORIES }
                
                gcFileOpenMode.add(this)
                it.add(this, BorderLayout.CENTER)
            }
        }
    }

    if (fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
        val path = fileChooser.selectedFile.toPath()
        val gcLogFile = when (gcFileOpenMode.selection.actionCommand) {
            "single" -> SingleGCLogFile(path)
            "rotating" -> RotatingGCLogFile(path)
            else -> throw IllegalStateException("Unsupported file open mode")
        }
        onFileSelected(gcLogFile)
    }
}