@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package storage

import model.TriggerConfig

/**
 * Persists the trigger configuration (voice phrase + gesture type).
 *
 * - **Android**: SharedPreferences
 * - **iOS**: NSUserDefaults
 */
expect object TriggerConfigStorage {
    fun saveConfig(config: TriggerConfig)
    fun loadConfig(): TriggerConfig
    fun clearConfig()
    fun isConfigured(): Boolean
}
