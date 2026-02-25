package auth

import io.ktor.server.application.*
import io.ktor.server.routing.*
import io.ktor.server.response.*
import io.ktor.server.request.*
import io.ktor.http.*
import models.LoginRequest
import models.SignupRequest

fun Route.authRoutes() {
    route("/signup") {
        post {
            val req = call.receive<SignupRequest>()
            val resp = AuthService.signup(req)
            call.respond(HttpStatusCode.Created, resp)
        }
    }
    route("/login") {
        post {
            val req = call.receive<LoginRequest>()
            val resp = AuthService.login(req)
            if (resp == null) call.respond(HttpStatusCode.Unauthorized)
            else call.respond(HttpStatusCode.OK, resp)
        }
    }
}
