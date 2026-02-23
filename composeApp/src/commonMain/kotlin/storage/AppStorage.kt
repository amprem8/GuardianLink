@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

/**
 * Platform-specific persistent key-value storage.
 *
 * - Android: SharedPreferences
 * - iOS: NSUserDefaults
 *
 * Data survives app restarts and is cleared only on uninstall (or explicit [clear]).
 */
expect object AppStorage {

    /** Returns `true` if the user completed signup + OTP verification. */
    fun isRegistered(): Boolean

    /** Returns `true` if the user is currently logged in (session active). */
    fun isLoggedIn(): Boolean

    /** Mark the user as logged in (called after successful login or signup+OTP). */
    fun setLoggedIn(loggedIn: Boolean)

    /** Persist registration data after successful signup + OTP. */
    fun markRegistered(
        userName: String,
        userEmail: String,
        phoneNumber: String,
    )

    fun getUserName(): String
    fun getUserEmail(): String
    fun getPhoneNumber(): String

    /** Returns `true` if the user has configured the minimum emergency contacts. */
    fun isContactsConfigured(): Boolean

    /** Mark the emergency-contacts setup step as complete. */
    fun markContactsConfigured()

    /** Clear login session but keep registration data (user can re-login). */
    fun logout()

    /** Wipe ALL persisted data (full reset, e.g. uninstall-equivalent). */
    fun clear()
}
