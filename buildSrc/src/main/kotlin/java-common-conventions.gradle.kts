plugins {
    java
}

repositories {
    mavenCentral()
}

dependencies {
    constraints {
        // Dependency versions as constraints
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(JVM_LANGUAGE_LEVEL))
    }
}

tasks.withType<JavaCompile> {
    options.encoding = "UTF-8"
    options.release.set(JVM_LANGUAGE_LEVEL)
    options.compilerArgs = listOf("--enable-preview")
}

tasks.withType<JavaExec> {
    jvmArgs = listOf("--enable-preview")
    javaLauncher.set(javaToolchains.launcherFor(java.toolchain))
}
