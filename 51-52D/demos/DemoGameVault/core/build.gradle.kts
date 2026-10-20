plugins {
    alias(libs.plugins.kotlinJvm)
}

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
}

dependencies {
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}
