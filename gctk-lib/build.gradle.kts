plugins {
    id("java-library-conventions")
}

dependencies {
    api(libs.gctoolkit.api)
    implementation(libs.bundles.gctoolkit)
}

tasks.withType<Test> {
    maxHeapSize = providers.gradleProperty("test.maxHeapSize").getOrElse("512m")
    jvmArgs(
        "-XX:+UseZGC",
    )
    if (providers.gradleProperty("test.jfrRecording").orNull == "true") {
        jvmArgs(
            "-XX:+HeapDumpOnOutOfMemoryError",
            "-XX:StartFlightRecording=settings=profile,dumponexit=true,filename=${projectDir}-${maxHeapSize}.jfr",
            "-XX:FlightRecorderOptions=stackdepth=256",
        )
    }
}
