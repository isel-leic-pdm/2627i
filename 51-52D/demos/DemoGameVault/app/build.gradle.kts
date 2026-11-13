import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.util.Properties
import kotlin.apply

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.ksp)
    alias(libs.plugins.androidx.room)
    alias(libs.plugins.kotlin.serialization)
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
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // Empty by default, so that the fields exist in every build type. Only debug builds
        // override them with the dev-time secrets (see below).
        buildConfigField("String", "IGDB_CLIENT_ID", "\"\"")
        buildConfigField("String", "IGDB_CLIENT_SECRET", "\"\"")
        buildConfigField("String", "IGDB_ACCESS_TOKEN", "\"\"")
    }

    buildTypes {
        debug {
            enableUnitTestCoverage = true
            enableAndroidTestCoverage = true

            // Dev-time only secrets, loaded from local.properties (not in the repo). They are set
            // only for debug builds so that they are never embedded in release APKs.
            buildConfigField("String", "IGDB_CLIENT_ID", "\"${localProperties.getProperty("igdb.client.id") ?: ""}\"")
            buildConfigField("String", "IGDB_CLIENT_SECRET", "\"${localProperties.getProperty("igdb.client.secret") ?: ""}\"")
            buildConfigField("String", "IGDB_ACCESS_TOKEN", "\"${localProperties.getProperty("igdb.access.token") ?: ""}\"")
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

    // The JaCoCo version used for coverage: to instrument the classes for instrumented tests, as the
    // agent of unit tests, and to generate the reports. AGP sets the jacoco extension's toolVersion
    // from it, so setting the latter in this module has no effect (unlike in core).
    testCoverage {
        jacocoVersion = libs.versions.jacoco.get()
    }
}

room {
    // The exported schemas are versioned in the repo: they document the database evolution and
    // are required to write migration tests.
    schemaDirectory("$projectDir/schemas")
}

dependencies {
    implementation(project(":core"))

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.collection)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.datastore.preferences)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.client.auth)
    implementation(libs.kotlinx.serialization.json)

    testImplementation(libs.junit)
    testImplementation(kotlin("test"))
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.ktor.client.mock)

    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.kotlinx.coroutines.test)

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
        // Generated by the build tools
        "**/R.class", "**/R$*.class", "**/BuildConfig.*", "**/Manifest*.*", "android/**/*.*",
        "**/*ComposableSingletons*.*",
        // Generated by Room (e.g. GameVaultDatabase_Impl, PlatformDao_Impl)
        "**/*_Impl*.*",
        // Project conventions: test tags, previews (in *Previews classes), theme and the fakes,
        // which are only used at dev time
        "**/*TestTags*.*",
        "**/*Previews*.*",
        "**/ui/theme/**/*.*",
        "**/adapters/fakes/**"
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
