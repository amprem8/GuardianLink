plugins {
    kotlin("multiplatform") version "2.3.0"
    kotlin("plugin.serialization") version "2.3.0"

    id("com.android.kotlin.multiplatform.library")

}

kotlin {

    // ✅ JVM target for SERVER
    jvm()

    // ✅ Android target (shared code)
    androidLibrary{
        namespace="com.example.sharedcore"
        compileSdk=36
        minSdk=24
    }

    sourceSets {

        val commonMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-core:2.3.8")
                implementation("io.ktor:ktor-client-content-negotiation:2.3.8")
                implementation("io.ktor:ktor-serialization-kotlinx-json:2.3.8")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.3")
            }
        }

        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // ✅ SERVER JVM
        val jvmMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-cio:2.3.8")
            }
        }

        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }

        // ✅ ANDROID
        val androidMain by getting {
            dependencies {
                implementation("io.ktor:ktor-client-okhttp:2.3.8")
            }
        }

    }
}

