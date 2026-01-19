package auth

import models.User
import db.DynamoClient
import models.AuthResponse
import models.LoginRequest
import models.SignupRequest
import util.PasswordHasher
import java.util.UUID

object AuthService {
    fun signup(req: SignupRequest): AuthResponse {
        val hashedPassword = PasswordHasher.hash(req.password)
        val user = User(UUID.randomUUID().toString(), req.name, req.email, hashedPassword, System.currentTimeMillis())
        DynamoClient.saveUser(user)
        val token = JwtConfig.generateToken(user.userId)
        return AuthResponse(user.userId, user.name, user.email, token)
    }

    fun login(req: LoginRequest): AuthResponse? {
        val user = DynamoClient.getUser(req.email) ?: return null
        if (!PasswordHasher.verify(req.password, user.passwordHash)) return null
        val token = JwtConfig.generateToken(user.userId)
        return AuthResponse(user.userId, user.name, user.email, token)
    }
}
