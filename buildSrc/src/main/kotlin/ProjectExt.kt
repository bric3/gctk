import org.gradle.kotlin.dsl.the

/**
 * Workaround for accessing the version catalog from the buildSrc project.
 *
 * Allows to use libs directly in the kotlin conventions scripts.
 *
 * Still needed to set in `buildSrc/settings.gradle.kts`
 * ```kotlin
 * dependencyResolutionManagement {
 *   versionCatalogs {
 *     create("libs") {
 *       from(files("../gradle/libs.versions.toml"))
 *     }
 *   }
 * }
 * ```
 *
 * And te following dependency in `buildSrc/build.gradle.kts`
 * ```kotlin
 * dependencies {
 *   implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
 * }
 * ```
 *
 * @see https://github.com/gradle/gradle/issues/15383
 */
val org.gradle.api.Project.libs get() = the<org.gradle.accessors.dm.LibrariesForLibs>()