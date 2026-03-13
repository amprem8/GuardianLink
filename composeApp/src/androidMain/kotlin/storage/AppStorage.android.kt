@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import com.example.guardianlink.MonitoringServiceController
import ui.OFFLINE_MODE_NORMAL_SMS

actual object AppStorage {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val prefs: SharedPreferences
        get() = appContext.getSharedPreferences("resq_prefs", Context.MODE_PRIVATE)

    actual fun isRegistered(): Boolean = prefs.getBoolean(KEY_REGISTERED, false)
    actual fun isLoggedIn(): Boolean = prefs.getBoolean(KEY_LOGGED_IN, false)
    actual fun setLoggedIn(loggedIn: Boolean) { prefs.edit().putBoolean(KEY_LOGGED_IN, loggedIn).commit() }

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        prefs.edit()
            .putBoolean(KEY_REGISTERED, true)
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_USER_NAME, userName)
            .putString(KEY_USER_EMAIL, userEmail)
            .putString(KEY_PHONE, phoneNumber)
            .commit()
    }

    actual fun getUserName(): String = prefs.getString(KEY_USER_NAME, "").orEmpty()
    actual fun getUserEmail(): String = prefs.getString(KEY_USER_EMAIL, "").orEmpty()
    actual fun getPhoneNumber(): String = prefs.getString(KEY_PHONE, "").orEmpty()

    actual fun isContactsConfigured(): Boolean = prefs.getBoolean(KEY_CONTACTS_CONFIGURED, false)
    actual fun markContactsConfigured() { prefs.edit().putBoolean(KEY_CONTACTS_CONFIGURED, true).commit() }

    // ── Safe PIN ────────────────────────────────────────────
    actual fun getSafePin(): String = prefs.getString(KEY_SAFE_PIN, "").orEmpty()
    actual fun setSafePin(pin: String) { prefs.edit().putString(KEY_SAFE_PIN, pin).commit() }

    // ── Monitoring toggles ──────────────────────────────────
    actual fun isContinuousMonitoring(): Boolean = prefs.getBoolean(KEY_CONTINUOUS_MONITORING, true)
    actual fun setContinuousMonitoring(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CONTINUOUS_MONITORING, enabled).commit()
        MonitoringServiceController.applyContinuousMonitoring(appContext, enabled)
    }

    actual fun isVoiceChoice(): Boolean = prefs.getBoolean(KEY_VOICE_CHOICE, false)
    actual fun setVoiceChoice(enabled: Boolean) { prefs.edit().putBoolean(KEY_VOICE_CHOICE, enabled).commit() }

    actual fun getOfflineFallbackMode(): String =
        prefs.getString(KEY_OFFLINE_FALLBACK_MODE, OFFLINE_MODE_NORMAL_SMS).orEmpty()

    actual fun setOfflineFallbackMode(mode: String) {
        prefs.edit().putString(KEY_OFFLINE_FALLBACK_MODE, mode).commit()
    }

    actual fun getLastKnownOnline(): Boolean = prefs.getBoolean(KEY_LAST_KNOWN_ONLINE, true)
    actual fun setLastKnownOnline(isOnline: Boolean) {
        prefs.edit().putBoolean(KEY_LAST_KNOWN_ONLINE, isOnline).commit()
    }

    // ── Session ─────────────────────────────────────────────
    actual fun logout() { prefs.edit().putBoolean(KEY_LOGGED_IN, false).commit() }
    actual fun clear() { prefs.edit().clear().commit() }

    // ── Backend ─────────────────────────────────────────────
    actual fun getBackendBaseUrl(): String = config.AppConfig.BASE_URL.trimEnd('/')

    // ── keys ────────────────────────────────────────────────
    private const val KEY_REGISTERED            = "registered"
    private const val KEY_LOGGED_IN             = "loggedIn"
    private const val KEY_USER_NAME             = "userName"
    private const val KEY_USER_EMAIL            = "userEmail"
    private const val KEY_PHONE                 = "phoneNumber"
    private const val KEY_CONTACTS_CONFIGURED   = "contactsConfigured"
    private const val KEY_SAFE_PIN              = "safePin"
    private const val KEY_CONTINUOUS_MONITORING = "continuousMonitoring"
    private const val KEY_VOICE_CHOICE          = "voiceChoice"
    private const val KEY_OFFLINE_FALLBACK_MODE = "offlineFallbackMode"
    private const val KEY_LAST_KNOWN_ONLINE     = "lastKnownOnline"
}
