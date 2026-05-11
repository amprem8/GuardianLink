package session

import models.AuthResponse

/**
 * In-memory session holder for the currently logged-in user.
 * Populated after a successful signup/login API call.
 */
object UserSession {
    var currentUser: AuthResponse? = null
        private set

    fun login(response: AuthResponse) {
        currentUser = response
    }

    fun logout() {
        currentUser = null
    }

    val isLoggedIn: Boolean get() = currentUser != null
    val userName: String get() = currentUser?.name.orEmpty()
    val userEmail: String get() = currentUser?.email.orEmpty()
}
