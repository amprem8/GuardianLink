package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse
import kotlinx.serialization.json.Json
import models.AuthResponse
import models.LoginRequest

object LoginHandler {

    fun handle(request: APIGatewayV2HTTPEvent, json: Json): APIGatewayV2HTTPResponse {
        val body = request.body ?: return badRequest("Missing body")
        val login = json.decodeFromString<LoginRequest>(body)

        // Authenticate user
        val resp: AuthResponse? = AuthService.login(login)

        return if (resp == null) {
            unauthorized()
        } else {
            ok(json.encodeToString(resp))
        }
    }

    private fun ok(body: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(200)
            .withHeaders(defaultHeaders())
            .withBody(body)
            .build()

    private fun unauthorized() =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(401)
            .withHeaders(defaultHeaders())
            .withBody("""{"message":"Invalid credentials"}""")
            .build()

    private fun badRequest(msg: String) =
        APIGatewayV2HTTPResponse.builder()
            .withStatusCode(400)
            .withHeaders(defaultHeaders())
            .withBody("""{"message":"$msg"}""")
            .build()

    private fun defaultHeaders() = mapOf(
        "Content-Type" to "application/json",
        "Access-Control-Allow-Origin" to "*"
    )
}
