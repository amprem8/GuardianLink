package com.example.guardianlink

import io.ktor.server.engine.*
import io.ktor.server.cio.*
import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import auth.authRoutes
import kotlinx.serialization.json.Json

object KtorServer {
    private val server: EmbeddedServer<CIOApplicationEngine, CIOApplicationEngine.Configuration> by lazy {
        embeddedServer(CIO, port = 8080) {
            install(ContentNegotiation) {
                json(Json { ignoreUnknownKeys = true; prettyPrint = true })
            }
            routing {
                authRoutes()
            }
        }.start(wait = false)
    }

    fun start() {
        server // lazy-start Ktor
    }
}
