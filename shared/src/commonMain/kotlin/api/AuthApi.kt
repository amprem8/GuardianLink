package api

import SendOtpRequest
import VerifyOtpRequest
import config.AppConfig
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.json.Json
import models.*
import network.createHttpClient
import util.ApiError
import util.ApiException

class AuthApi {
    private val client = createHttpClient()
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun signup(req: SignupRequest): Result<AuthResponse> = runCatching {
        val response = client.post("${AppConfig.BASE_URL}/signup") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (response.status.isSuccess()) response.body<AuthResponse>()
        else throw parseError(response)
    }

    suspend fun login(req: LoginRequest): Result<AuthResponse> = runCatching {
        val response = client.post("${AppConfig.BASE_URL}/login") {
            contentType(ContentType.Application.Json)
            setBody(req)
        }
        if (response.status.isSuccess()) response.body<AuthResponse>()
        else throw parseError(response)
    }

    suspend fun sendOtp(phone: String): Result<Unit> = runCatching {
        val response = client.post("${AppConfig.BASE_URL}/otp/send") {
            contentType(ContentType.Application.Json)
            setBody(SendOtpRequest(phone))
        }
        if (!response.status.isSuccess()) throw parseError(response)
    }

    suspend fun verifyOtp(phone: String, otp: String): Result<Boolean> = runCatching {
        val response = client.post("${AppConfig.BASE_URL}/otp/verify") {
            contentType(ContentType.Application.Json)
            setBody(VerifyOtpRequest(phone, otp))
        }

        if (response.status.isSuccess()) response.body<Boolean>()
        else throw parseError(response)
    }


    private suspend fun parseError(response: HttpResponse): ApiException {
        val apiError = try {
            json.decodeFromString(ApiError.serializer(), response.bodyAsText())
        } catch (_: Exception) {
            ApiError("UNKNOWN_ERROR", "Something went wrong")
        }
        return ApiException(apiError)
    }

}
