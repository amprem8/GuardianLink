@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package push

/**
 * Best-effort hook to refresh push registration (used before SOS trigger).
 *
 * Android actual fetches latest FCM token and syncs it with backend.
 * iOS actual is currently a no-op in this project.
 */
expect fun refreshPushRegistrationBeforeSos()

