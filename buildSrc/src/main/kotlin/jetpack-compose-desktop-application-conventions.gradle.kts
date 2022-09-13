plugins {
    id("java-common-conventions")
    kotlin("jvm")
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

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "$JVM_LANGUAGE_LEVEL"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xjvm-default=all",
    )
}

// Substitute the compiler to make Compose work with Kotlin 1.7.20-RC
configurations.all {
    resolutionStrategy.dependencySubstitution {
        substitute(module("org.jetbrains.compose.compiler:compiler")).apply {
            using(module(libs.compose.compiler.get().toString()))
        }
    }
}