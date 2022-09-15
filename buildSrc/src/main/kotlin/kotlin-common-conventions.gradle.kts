import org.gradle.kotlin.dsl.withType

plugins {
    kotlin("jvm")
}

repositories {
    mavenCentral()
}

tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>() {
    kotlinOptions.jvmTarget = "$JVM_LANGUAGE_LEVEL"
    kotlinOptions.freeCompilerArgs += listOf(
        "-Xjvm-default=all",
    )
}


