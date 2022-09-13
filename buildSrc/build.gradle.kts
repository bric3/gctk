plugins {
    `kotlin-dsl`
}

dependencies {
    implementation(libs.gradleplugin.kotlin.jvm)
    implementation(libs.gradleplugin.jetbrains.compose)
    implementation(files(libs.javaClass.superclass.protectionDomain.codeSource.location))
}
