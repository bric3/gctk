package io.github.bric3.gctk

import java.nio.file.Files
import java.nio.file.Path
import java.util.Arrays
import java.util.Objects

class TestLogFile(relativePath: Path) {
    val logFilePath: Path

    init {
        Objects.requireNonNull(relativePath, "relativePath")
        logFilePath = Arrays.stream(subfolders)
            .flatMap { path: String ->
                Arrays.stream(arrayOf(
                    "./$path",
                    "../$path",
                    "../../$path",
                    "./gclogs/$path",
                    "../gclogs/$path",
                    "../../gclogs/$path"
                )
                )
            }
            .map { path -> Path.of(path).resolve(relativePath) }
            .filter { path: Path -> Files.exists(path) }
            .findFirst()
            .orElseThrow { IllegalArgumentException("$relativePath not found") }
    }

    internal constructor(relativePath: String) : this(Path.of(relativePath)) {}

    private val logFileName: String
        get() = logFilePath.fileName.toString()
    val logFileNameWithoutExtension: String
        get() {
            val fileName = logFileName
            val dotIndex = fileName.lastIndexOf('.')
            return if (dotIndex == -1) fileName else fileName.substring(0, dotIndex)
        }
    val folder: Path
        get() = if (Files.isDirectory(logFilePath)) {
            logFilePath
        } else logFilePath.parent

    companion object {
        private val subfolders = arrayOf(
            "./"
        )
    }
}