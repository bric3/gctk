plugins {
    id("jetpack-compose-desktop-application-conventions")
}

dependencies {
    implementation(project(":gctk-lib"))
    implementation(libs.jfree.chart)
    implementation(libs.bundles.log)
}

compose.desktop {
    application {
        mainClass = "io.github.bric3.gctk.app.MainKt"
        jvmArgs(
            "--show-version",
            "--enable-preview",
            "-XX:+UseZGC",
            "-Xms512m",
            "-Xmx2048m",
            "-Dskiko.fps.enabled=true",
            "-Dskiko.fps.periodSeconds=2.0",
            "-Dskiko.fps.longFrames.show=true",
        )
    }
}

tasks.withType<JavaExec> {
    doFirst {
        println("Running ${name} with JVM args: ${jvmArgs}")
    }
    jvmArgs(
        "--show-version",
        "--enable-preview",
        "-XX:+UseZGC",
        "-Xms512m",
        "-Xmx2048m",
        "-Dskiko.fps.enabled=true",
        "-Dskiko.fps.periodSeconds=2.0",
        "-Dskiko.fps.longFrames.show=true",
    )
}
