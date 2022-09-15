import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    id("jvm-test-suite")
}

testing {
    suites {
        @Suppress("UNUSED_VARIABLE")
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter(libs.versions.jupiter.get())
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging {
        events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
    }
    
    filter {
        isFailOnNoMatchingTests = false
    }
}