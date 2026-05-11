@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package push

/**
 * Refreshes push registration (FCM token → SNS endpoint) before an SOS is dispatched.
 * Suspends until registration completes so the SOS trigger always uses a fresh endpoint ARN.
 *
 * Android actual: fetches latest FCM token and syncs with backend (awaited).
 * iOS actual: no-op in this project.
 */
expect suspend fun refreshPushRegistrationBeforeSos()

