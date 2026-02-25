plugins {
    // this is necessary to avoid the plugins to be loaded multiple times
    // in each subproject's classloader
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinJvm) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
    alias(libs.plugins.ktor) apply false
    alias(libs.plugins.androidKotlinMultiplatformLibrary) apply false
    alias(libs.plugins.androidLint) apply false

    id("org.sonarqube") version "7.2.2.6593"
}

sonar {
    properties {
        property("sonar.projectKey", "amprem8_ResQ")
        property("sonar.organization", "amprem8")
        property("sonar.host.url", "https://sonarcloud.io")
        property("sonar.sourceEncoding", "UTF-8")

        // Coverage report path (JaCoCo XML from server module)
        property("sonar.coverage.jacoco.xmlReportPaths",
            "${project(":server").layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml")

        // Exclude UI/platform code from coverage measurement
        property("sonar.coverage.exclusions", listOf(
            "composeApp/src/**",
            "shared/src/**",
            "**/bin/**",
            "**/ui/**",
            "**/screens/**",
            "**/screenmodel/**"
        ).joinToString(","))

        // Exclude build output duplicates from analysis
        property("sonar.exclusions", listOf(
            "**/bin/**",
            "**/build/**"
        ).joinToString(","))
    }
}
