package io.github.bric3.gctk.app

import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import kotlin.system.exitProcess


fun main() = application {
    Window(
        title = "GCTK",
        onCloseRequest = {
            exitProcess(0)
        }
    ) {
        val counter = remember { mutableStateOf(0) }
        MaterialTheme {
            Button(onClick = { counter.value++ }) {
                Text("I've been clicked ${counter.value} times")
            }
        }
    }
}
