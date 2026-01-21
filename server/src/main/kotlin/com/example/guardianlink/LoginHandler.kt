package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import kotlinx.serialization.json.Json
import models.LoginRequest

object LoginHandler {

    fun handle(request: APIGatewayV2HTTPEvent, json: Json) =
        runCatching {
            val body = request.body ?: return HttpResponses.badRequest("Missing body")
            val login = json.decodeFromString<LoginRequest>(body)

            val resp = AuthService.login(login)
                ?: return HttpResponses.unauthorized()

            HttpResponses.ok(json.encodeToString(resp))
        }.getOrElse {
            HttpResponses.internalError()
        }
}
