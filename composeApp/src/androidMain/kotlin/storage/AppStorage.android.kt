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

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        prefs.edit()
            .putBoolean(KEY_REGISTERED, true)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_PHONE, phoneNumber)
            .commit()  // commit() is synchronous — guarantees data is written to disk
    }

    actual fun getUserName(): String =
        prefs.getString(KEY_USER_NAME, "").orEmpty()

    actual fun getUserEmail(): String =
        prefs.getString(KEY_USER_EMAIL, "").orEmpty()

    actual fun getPhoneNumber(): String =
        prefs.getString(KEY_PHONE, "").orEmpty()

    actual fun clear() {
        prefs.edit().clear().commit()
    }

    // ── keys ────────────────────────────────────────────────

    private const val KEY_REGISTERED = "registered"
    private const val KEY_USER_NAME = "userName"
    private const val KEY_USER_EMAIL = "userEmail"
    private const val KEY_PHONE = "phoneNumber"
}
