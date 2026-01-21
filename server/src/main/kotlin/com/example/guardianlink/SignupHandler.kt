package com.example.guardianlink

import auth.AuthService
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import kotlinx.serialization.json.Json
import models.SignupRequest

object SignupHandler {

    fun handle(request: APIGatewayV2HTTPEvent, json: Json) =
        runCatching {
            val body = request.body ?: return HttpResponses.badRequest("Missing body")
            val signup = json.decodeFromString<SignupRequest>(body)

            val resp = AuthService.signup(signup)

            HttpResponses.created(json.encodeToString(resp))
        }.getOrElse {
            HttpResponses.internalError()
        }
}
