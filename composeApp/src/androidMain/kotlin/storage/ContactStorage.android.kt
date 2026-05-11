@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKeys
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.EmergencyContact

actual object ContactStorage {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    /** Call once from [MainActivity.onCreate]. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC)
            EncryptedSharedPreferences.create(
                "resq_secure_contacts",
                masterKeyAlias,
                appContext,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM,
            )
        } catch (_: Exception) {
            // Graceful fallback – encryption may fail on rare devices / during backup-restore
            appContext.getSharedPreferences("resq_contacts_fallback", Context.MODE_PRIVATE)
        }
    }

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveContacts(contacts: List<EmergencyContact>) {
        val encoded = json.encodeToString(contacts)
        prefs.edit().putString(KEY_CONTACTS, encoded).commit()
    }

    actual fun loadContacts(): List<EmergencyContact> {
        val raw = prefs.getString(KEY_CONTACTS, null) ?: return emptyList()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            emptyList()
        }
    }

    actual fun clearContacts() {
        prefs.edit().remove(KEY_CONTACTS).commit()
    }

    private const val KEY_CONTACTS = "emergency_contacts"
}
