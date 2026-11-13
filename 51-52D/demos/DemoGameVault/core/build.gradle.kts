import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(libs.plugins.kotlinJvm)
    jacoco
}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

jacoco {
    toolVersion = libs.versions.jacoco.get()
}

tasks.withType<KotlinCompile>().configureEach {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

dependencies {
    // The repositories' contracts expose flows, so their users need coroutines too
    api(libs.kotlinx.coroutines.core)
    // The domain types expose dates, so their users need the date types too
    api(libs.kotlinx.datetime)
    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
}
