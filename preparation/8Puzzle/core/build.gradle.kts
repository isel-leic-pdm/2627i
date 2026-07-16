plugins {
    alias(libs.plugins.kotlin.library)
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

kotlin {
    jvmToolchain(11)
}

dependencies {
    testImplementation(libs.junit)
}
