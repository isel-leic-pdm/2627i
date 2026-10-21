import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    jacoco
}

val localProperties = Properties().apply {
    val file = rootProject.file("local.properties")
    if (file.exists()) {
        file.inputStream().use { load(it) }
    }
}

android {
    namespace = "isel.dei.pdm.demos.gamevault"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "isel.dei.pdm.demos.demogamevault"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "IGDB_CLIENT_ID", "\"${localProperties.getProperty("igdb.client.id") ?: ""}\"")
        buildConfigField("String", "IGDB_CLIENT_SECRET", "\"${localProperties.getProperty("igdb.client.secret") ?: ""}\"")
        buildConfigField("String", "IGDB_ACCESS_TOKEN", "\"${localProperties.getProperty("igdb.access.token") ?: ""}\"")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true
        }
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    tasks.withType<KotlinCompile>().configureEach {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    testImplementation(libs.junit)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)

    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

tasks.register<JacocoReport>("jacocoTestReport") {
    description = "Generates Jacoco coverage reports for both unit and instrumentation tests (app & core)."
    group = "Reporting"
    dependsOn(":core:test", "testDebugUnitTest", "connectedDebugAndroidTest")
    reports {
        xml.required.set(true)
        html.required.set(true)
    }

    val fileFilter = listOf(
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*",
        "**/*Test*.*", "android/**/*.*",
        "**/ui/theme/**/*.*",
        "**/*ComposableSingletons*.*",
        "**/*Previews*.*"
    )
    val debugTree = files(
        fileTree("${project.layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
            exclude(fileFilter)
        },
        fileTree("${project.layout.buildDirectory.get()}/intermediates/built_in_kotlinc/debug/compileDebugKotlin/classes") {
            exclude(fileFilter)
        },
        fileTree("${project(":core").layout.buildDirectory.get()}/classes/java/main") {
            exclude(fileFilter)
        },
        fileTree("${project(":core").layout.buildDirectory.get()}/classes/kotlin/main") {
            exclude(fileFilter)
        }
    )
    val appSrc = "${project.projectDir}/src/main/java"
    val coreSrc = "${project(":core").projectDir}/src/main/java"

    sourceDirectories.setFrom(files(appSrc, coreSrc))
    classDirectories.setFrom(files(debugTree))
    executionData.setFrom(files(
        fileTree(project.layout.buildDirectory.get()) {
            include(
                "outputs/unit_test_code_coverage/debugUnitTest/*.exec",
                "outputs/code_coverage/debugAndroidTest/**/*.ec"
            )
        },
        fileTree(project(":core").layout.buildDirectory.get()) {
            include("jacoco/test.exec")
        }
    ))
}
