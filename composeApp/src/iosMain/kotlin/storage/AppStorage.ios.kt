@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import platform.Foundation.NSUserDefaults

actual object AppStorage {

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    // ── public API ──────────────────────────────────────────

    actual fun isRegistered(): Boolean =
        defaults.boolForKey(KEY_REGISTERED)

    actual fun isLoggedIn(): Boolean =
        defaults.boolForKey(KEY_LOGGED_IN)

    actual fun setLoggedIn(loggedIn: Boolean) {
        defaults.setBool(loggedIn, forKey = KEY_LOGGED_IN)
        defaults.synchronize()
    }

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        defaults.setBool(true, forKey = KEY_REGISTERED)
        defaults.setBool(true, forKey = KEY_LOGGED_IN)
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

    actual fun isContactsConfigured(): Boolean =
        defaults.boolForKey(KEY_CONTACTS_CONFIGURED)

    actual fun markContactsConfigured() {
        defaults.setBool(true, forKey = KEY_CONTACTS_CONFIGURED)
        defaults.synchronize()
    }

    actual fun logout() {
        defaults.setBool(false, forKey = KEY_LOGGED_IN)
        defaults.synchronize()
    }

    actual fun clear() {
        listOf(KEY_REGISTERED, KEY_LOGGED_IN, KEY_USER_NAME, KEY_USER_EMAIL, KEY_PHONE, KEY_CONTACTS_CONFIGURED).forEach {
            defaults.removeObjectForKey(it)
        }
        defaults.synchronize()
    }

    // ── keys ────────────────────────────────────────────────

    private const val KEY_REGISTERED = "registered"
    private const val KEY_LOGGED_IN = "loggedIn"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_PHONE = "phoneNumber"
    private const val KEY_CONTACTS_CONFIGURED = "contactsConfigured"
}
