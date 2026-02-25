@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.TriggerConfig

actual object TriggerConfigStorage {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context

    /** Call once from [MainActivity.onCreate]. */
    fun init(context: Context) {
        appContext = context.applicationContext
    }

    private val prefs: SharedPreferences
        get() = appContext.getSharedPreferences("resq_trigger_config", Context.MODE_PRIVATE)

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveConfig(config: TriggerConfig) {
        val encoded = json.encodeToString(config)
        prefs.edit()
            .putString(KEY_CONFIG, encoded)
            .putBoolean(KEY_CONFIGURED, true)
            .commit()
    }

    actual fun loadConfig(): TriggerConfig {
        val raw = prefs.getString(KEY_CONFIG, null) ?: return TriggerConfig()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            TriggerConfig()
        }
    }

    actual fun clearConfig() {
        prefs.edit().clear().commit()
    }

    actual fun isConfigured(): Boolean =
        prefs.getBoolean(KEY_CONFIGURED, false)

    private const val KEY_CONFIG = "trigger_config"
    private const val KEY_CONFIGURED = "trigger_configured"
}
