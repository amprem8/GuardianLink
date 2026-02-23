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

    /** Persist registration data after successful signup + OTP. */
    fun markRegistered(
        userName: String,
        userEmail: String,
        phoneNumber: String,
    )

    fun getUserName(): String
    fun getUserEmail(): String
    fun getPhoneNumber(): String

    /** Wipe all persisted data (used on logout / profile reset). */
    fun clear()
}
