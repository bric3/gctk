plugins {
    id("java-common-conventions")
    id("kotlin-common-conventions")
    id("org.jetbrains.compose")
}

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://androidx.dev/storage/compose-compiler/repository/")
}

dependencies {
    implementation(compose.desktop.currentOs)
}

// Substitute the compiler to make Compose work with Kotlin 1.7.20-RC
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.compose.compiler:compiler")).apply {
            using(module(libs.compose.compiler.get().toString()))
        }
    }
}