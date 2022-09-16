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
import androidx.compose.ui.graphics.Color
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
import java.awt.BorderLayout
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
    val counter = remember { mutableStateOf(0) }
    val inc: () -> Unit = { counter.value++ }
    val dec: () -> Unit = { counter.value-- }

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
                modifier = Modifier.fillMaxWidth(),
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
                        Text("Select GC File")
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
                        Text("Command line : %s".format(gcReport.value?.commandLine ?: "N/A"))
                        Text("Estimated JVM start : %s".format(gcReport.value?.estimatedJVMStartTime))
                        Text("First event         : %s".format(gcReport.value?.timeOfFirstEvent))
                        Text("Runtime duration    : %.4f".format(gcReport.value?.runtimeDuration))
                        Text("GC                  : %s".format(gcReport.value?.gcName))
                    }
                }
            }

            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    modifier = Modifier.padding(top = 80.dp, bottom = 20.dp)
                ) {
                    Button("1. Compose Button: increment", inc)
                    Spacer(modifier = Modifier.height(20.dp))

                    SwingPanel(
                        background = Color.White,
                        modifier = Modifier.size(270.dp, 90.dp),
                        factory = {
                            JPanel().apply {
                                layout = BoxLayout(this, BoxLayout.Y_AXIS)
                                add(actionButton("1. Swing Button: decrement", dec))
                                add(actionButton("2. Swing Button: decrement", dec))
                                add(actionButton("3. Swing Button: decrement", dec))
                            }
                        },
                        update = {
                            // called when the composable state changes
                        }
                    )

                    Spacer(modifier = Modifier.height(20.dp))
                    Button("2. Compose Button: increment", inc)
                }
            }
        }
    }
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

            override fun accept(f: File?): Boolean {
                if (f == null) return false

                if (f.isDirectory) {
                    return true
                }
                val fileName = f.name
                val i = fileName.lastIndexOf('.')

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