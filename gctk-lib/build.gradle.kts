plugins {
    id("java-library-conventions")
}

dependencies {
    api(libs.gctoolkit.api)
    api(libs.gctoolkit.integration) {
        because("com.microsoft.gctoolkit.integration.collections.XYDataSet is used in the GCReport API")
    }
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
