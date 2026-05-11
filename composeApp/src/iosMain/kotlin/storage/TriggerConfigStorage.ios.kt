@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import model.TriggerConfig
import platform.Foundation.NSUserDefaults

actual object TriggerConfigStorage {

    private val defaults: NSUserDefaults
        get() = NSUserDefaults.standardUserDefaults

    private val json = Json { ignoreUnknownKeys = true }

    actual fun saveConfig(config: TriggerConfig) {
        val encoded = json.encodeToString(config)
        defaults.setObject(encoded, forKey = KEY_CONFIG)
        defaults.setBool(true, forKey = KEY_CONFIGURED)
        defaults.synchronize()
    }

    actual fun loadConfig(): TriggerConfig {
        val raw = defaults.stringForKey(KEY_CONFIG) ?: return TriggerConfig()
        return try {
            json.decodeFromString(raw)
        } catch (_: Exception) {
            TriggerConfig()
        }
    }

    actual fun clearConfig() {
        defaults.removeObjectForKey(KEY_CONFIG)
        defaults.removeObjectForKey(KEY_CONFIGURED)
        defaults.synchronize()
    }

    actual fun isConfigured(): Boolean =
        defaults.boolForKey(KEY_CONFIGURED)

    private const val KEY_CONFIG = "trigger_config"
    private const val KEY_CONFIGURED = "trigger_configured"
}
