package com.example.guardianlink

import io.ktor.server.engine.*
import io.ktor.server.cio.*

fun main() {
    embeddedServer(CIO, port = 8080) {
        KtorServer.start() // uses the same KtorServer singleton
    }.start(wait = true)
}
