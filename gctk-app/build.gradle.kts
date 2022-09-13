plugins {
    id("jetpack-compose-desktop-application-conventions")
}

dependencies {
    implementation(project(":gctk-lib"))
}

compose.desktop {
    application {
        mainClass = "io.github.bric3.gctk.app.MainKt"
    }
}
