package api

import io.ktor.client.call.*
import io.ktor.client.request.*
import models.*
import network.createHttpClient
import config.AppConfig

class AuthApi {
    private val client = createHttpClient()

    suspend fun signup(req: SignupRequest): AuthResponse =
        client.post("${AppConfig.BASE_URL}/signup") {
            setBody(req)
        }.body()

    suspend fun login(req: LoginRequest): AuthResponse =
        client.post("${AppConfig.BASE_URL}/login") {
            setBody(req)
        }.body()
}
