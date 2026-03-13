@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import platform.Foundation.NSUserDefaults
import ui.OFFLINE_MODE_NORMAL_SMS

actual object AppStorage {

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    actual fun isRegistered(): Boolean = defaults.boolForKey(KEY_REGISTERED)
    actual fun isLoggedIn(): Boolean = defaults.boolForKey(KEY_LOGGED_IN)
    actual fun setLoggedIn(loggedIn: Boolean) {
        defaults.setBool(loggedIn, forKey = KEY_LOGGED_IN); defaults.synchronize()
    }

    actual fun markRegistered(userName: String, userEmail: String, phoneNumber: String) {
        defaults.setBool(true, forKey = KEY_REGISTERED)
        defaults.setBool(true, forKey = KEY_LOGGED_IN)
        defaults.setObject(userName, forKey = KEY_USER_NAME)
        defaults.setObject(userEmail, forKey = KEY_USER_EMAIL)
        defaults.setObject(phoneNumber, forKey = KEY_PHONE)
        defaults.synchronize()
    }

    actual fun getUserName(): String = defaults.stringForKey(KEY_USER_NAME) ?: ""
    actual fun getUserEmail(): String = defaults.stringForKey(KEY_USER_EMAIL) ?: ""
    actual fun getPhoneNumber(): String = defaults.stringForKey(KEY_PHONE) ?: ""

    actual fun isContactsConfigured(): Boolean = defaults.boolForKey(KEY_CONTACTS_CONFIGURED)
    actual fun markContactsConfigured() {
        defaults.setBool(true, forKey = KEY_CONTACTS_CONFIGURED); defaults.synchronize()
    }

    // ── Safe PIN ────────────────────────────────────────────
    actual fun getSafePin(): String = defaults.stringForKey(KEY_SAFE_PIN) ?: ""
    actual fun setSafePin(pin: String) {
        defaults.setObject(pin, forKey = KEY_SAFE_PIN); defaults.synchronize()
    }

    // ── Monitoring toggles ──────────────────────────────────
    actual fun isContinuousMonitoring(): Boolean =
        if (defaults.objectForKey(KEY_CONTINUOUS_MONITORING) == null) true
        else defaults.boolForKey(KEY_CONTINUOUS_MONITORING)

    actual fun setContinuousMonitoring(enabled: Boolean) {
        defaults.setBool(enabled, forKey = KEY_CONTINUOUS_MONITORING); defaults.synchronize()
    }

    actual fun isVoiceChoice(): Boolean =
        if (defaults.objectForKey(KEY_VOICE_CHOICE) == null) false
        else defaults.boolForKey(KEY_VOICE_CHOICE)

    actual fun setVoiceChoice(enabled: Boolean) {
        defaults.setBool(enabled, forKey = KEY_VOICE_CHOICE); defaults.synchronize()
    }

    actual fun getOfflineFallbackMode(): String =
        defaults.stringForKey(KEY_OFFLINE_FALLBACK_MODE) ?: OFFLINE_MODE_NORMAL_SMS

    actual fun setOfflineFallbackMode(mode: String) {
        defaults.setObject(mode, forKey = KEY_OFFLINE_FALLBACK_MODE); defaults.synchronize()
    }

    actual fun getLastKnownOnline(): Boolean =
        if (defaults.objectForKey(KEY_LAST_KNOWN_ONLINE) == null) true
        else defaults.boolForKey(KEY_LAST_KNOWN_ONLINE)

    actual fun setLastKnownOnline(isOnline: Boolean) {
        defaults.setBool(isOnline, forKey = KEY_LAST_KNOWN_ONLINE); defaults.synchronize()
    }

    actual fun getLastGestureTriggeredText(): String =
        defaults.stringForKey(KEY_LAST_GESTURE_TRIGGERED_TEXT) ?: ""

    actual fun setLastGestureTriggeredText(value: String) {
        defaults.setObject(value, forKey = KEY_LAST_GESTURE_TRIGGERED_TEXT); defaults.synchronize()
    }

    actual fun getLastSosSentText(): String =
        defaults.stringForKey(KEY_LAST_SOS_SENT_TEXT) ?: ""

    actual fun setLastSosSentText(value: String) {
        defaults.setObject(value, forKey = KEY_LAST_SOS_SENT_TEXT); defaults.synchronize()
    }

    // ── Session ─────────────────────────────────────────────
    actual fun logout() {
        defaults.setBool(false, forKey = KEY_LOGGED_IN); defaults.synchronize()
    }

    actual fun clear() {
        listOf(KEY_REGISTERED, KEY_LOGGED_IN, KEY_USER_NAME, KEY_USER_EMAIL, KEY_PHONE,
               KEY_CONTACTS_CONFIGURED, KEY_SAFE_PIN, KEY_CONTINUOUS_MONITORING, KEY_VOICE_CHOICE,
               KEY_OFFLINE_FALLBACK_MODE, KEY_LAST_KNOWN_ONLINE,
               KEY_LAST_GESTURE_TRIGGERED_TEXT, KEY_LAST_SOS_SENT_TEXT
        ).forEach { defaults.removeObjectForKey(it) }
        defaults.synchronize()
    }

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
    private const val KEY_LAST_GESTURE_TRIGGERED_TEXT = "lastGestureTriggeredText"
    private const val KEY_LAST_SOS_SENT_TEXT = "lastSosSentText"
}
