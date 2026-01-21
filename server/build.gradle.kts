plugins {
    alias(libs.plugins.kotlinJvm)
    alias(libs.plugins.ktor)
    application
}

group = "com.example.guardianlink"
version = "1.0.0"

application {
    // Lambda entry point
    mainClass.set("com.example.guardianlink.LambdaHandler")

    val isDevelopment: Boolean = project.ext.has("development")
    applicationDefaultJvmArgs = listOf("-Dio.ktor.development=$isDevelopment")
}

tasks.register<Jar>("fatJar") {
    archiveBaseName.set("${project.name}-all") // server-all.jar
    archiveVersion.set("${project.version}")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    manifest {
        attributes["Main-Class"] = "com.example.guardianlink.LambdaHandler"
    }

    // Include runtime dependencies
    from({
        configurations.runtimeClasspath.get()
            .filter { it.name.endsWith("jar") }
            .map { zipTree(it) }
    })

    // Include compiled classes
    from(sourceSets.main.get().output)
}

dependencies {
    implementation(project(":shared-core"))
    implementation(libs.logback)
    implementation(libs.ktor.serverCore)
    implementation(libs.aws.lambda.java.core)
    implementation(libs.aws.lambda.java.events)
    implementation(libs.ktor.server.cio)
    implementation(libs.ktor.serverNetty)
    implementation(libs.ktor.server.content.negotiation)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.serialization.kotlinx.json)

    // AWS SDK for DynamoDB
    implementation(libs.dynamodb)
    implementation(libs.regions)

    // JWT
    implementation(libs.java.jwt)

    // BCrypt for password hashing
    implementation(libs.jbcrypt)

    testImplementation(libs.ktor.serverTestHost)
    testImplementation(libs.kotlin.testJunit)
}
