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
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
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
import java.awt.Component
import javax.swing.*

fun main() = application {
    Window(
        title = "GCTK",
        state = rememberWindowState(size = DpSize(900.dp, 700.dp)),
        onCloseRequest = ::exitApplication
    ) {
        val counter = remember { mutableStateOf(0) }

        val inc: () -> Unit = { counter.value++ }
        val dec: () -> Unit = { counter.value-- }

        Box(
            modifier = Modifier.fillMaxWidth().height(60.dp).padding(top = 20.dp),
            contentAlignment = Alignment.Center
        ) {
            Text("Counter: ${counter.value}")
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

@Composable
private fun windowState() = rememberWindowState(size = DpSize(900.dp, 700.dp))

@Composable
fun Button(text: String = "", action: (() -> Unit)? = null) {
    Button(
        modifier = Modifier.size(270.dp, 30.dp),
        onClick = { action?.invoke() }
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
