@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import platform.Foundation.NSUserDefaults

actual object AppStorage {

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    // ── public API ──────────────────────────────────────────

    actual fun isRegistered(): Boolean =
        defaults.boolForKey(KEY_REGISTERED)

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        defaults.setBool(true, forKey = KEY_REGISTERED)
        defaults.setObject(userName, forKey = KEY_USER_NAME)
        defaults.setObject(userEmail, forKey = KEY_USER_EMAIL)
        defaults.setObject(phoneNumber, forKey = KEY_PHONE)
        defaults.synchronize()
    }

    actual fun getUserName(): String =
        defaults.stringForKey(KEY_USER_NAME) ?: ""

    actual fun getUserEmail(): String =
        defaults.stringForKey(KEY_USER_EMAIL) ?: ""

    actual fun getPhoneNumber(): String =
        defaults.stringForKey(KEY_PHONE) ?: ""

    actual fun clear() {
        listOf(KEY_REGISTERED, KEY_USER_NAME, KEY_USER_EMAIL, KEY_PHONE).forEach {
            defaults.removeObjectForKey(it)
        }
        defaults.synchronize()
    }

    // ── keys ────────────────────────────────────────────────

    private const val KEY_REGISTERED = "registered"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_PHONE = "phoneNumber"
}
