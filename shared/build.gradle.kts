plugins {
    kotlin("multiplatform")
    id("com.android.library")
    id("org.jetbrains.compose")
    id("org.jetbrains.kotlin.plugin.compose")
}

kotlin {

    androidTarget {
        compilations.all {
            compilerOptions.configure {
                jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_11)
            }
        }
    }

    iosX64()
    iosArm64()
    iosSimulatorArm64()

    targets.withType<org.jetbrains.kotlin.gradle.plugin.mpp.KotlinNativeTarget>()
        .configureEach {
            binaries.framework {
                baseName = "Shared"
                isStatic = false
            }
        }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation(project(":shared-core"))
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.ui)

                implementation(compose.materialIconsExtended)

                implementation(compose.components.resources)

                // Navigation
                implementation(libs.voyager.navigator)
                implementation(libs.voyager.screenmodel)

                implementation(libs.kotlinx.coroutines.core)
            }
        }
    }
}

android {
    namespace = "com.example.sos.shared"
    compileSdk = 36

    defaultConfig {
        minSdk = 24
    }

    buildFeatures {
        compose = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}
compose.resources {
    publicResClass = true
}
