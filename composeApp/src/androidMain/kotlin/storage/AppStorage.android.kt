@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences

actual object AppStorage {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    /** Must be called once from [MainActivity.onCreate] before any composable reads. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val prefs: SharedPreferences
        get() = appContext.getSharedPreferences("resq_prefs", Context.MODE_PRIVATE)

    // ── public API ──────────────────────────────────────────

    actual fun isRegistered(): Boolean =
        prefs.getBoolean(KEY_REGISTERED, false)

    actual fun isLoggedIn(): Boolean =
        prefs.getBoolean(KEY_LOGGED_IN, false)

    actual fun setLoggedIn(loggedIn: Boolean) {
        prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).commit()
    }

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        prefs.edit()
            .putBoolean(KEY_REGISTERED, true)
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_PHONE, phoneNumber)
            .commit()
    }

    actual fun getUserName(): String =
        prefs.getString(KEY_USER_NAME, "").orEmpty()

    actual fun getUserEmail(): String =
        prefs.getString(KEY_USER_EMAIL, "").orEmpty()

    actual fun getPhoneNumber(): String =
        prefs.getString(KEY_PHONE, "").orEmpty()

    actual fun isContactsConfigured(): Boolean =
        prefs.getBoolean(KEY_CONTACTS_CONFIGURED, false)

    actual fun markContactsConfigured() {
        prefs.edit().putBoolean(KEY_CONTACTS_CONFIGURED, true).commit()
    }

    actual fun logout() {
        prefs.edit().putBoolean(KEY_LOGGED_IN, false).commit()
    }

    actual fun clear() {
        prefs.edit().clear().commit()
    }

    // ── keys ────────────────────────────────────────────────

    private const val KEY_REGISTERED = "registered"
    private const val KEY_LOGGED_IN = "loggedIn"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_PHONE = "phoneNumber"
    private const val KEY_CONTACTS_CONFIGURED = "contactsConfigured"
}
