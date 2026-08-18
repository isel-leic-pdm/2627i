plugins {
    alias(libs.plugins.kotlin.jvm)
}

dependencies {
    // Domain logic module, minimal dependencies
    implementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.junit)
}
