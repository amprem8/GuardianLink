@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package push

import android.util.Log
import com.example.guardianlink.PushRegistrationSync
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

actual suspend fun refreshPushRegistrationBeforeSos() {
    // Suspend until Firebase returns the current token, then await the backend sync.
    val token: String? = suspendCancellableCoroutine { cont ->
        FirebaseMessaging.getInstance().token
            .addOnSuccessListener { token -> cont.resume(token) }
            .addOnFailureListener { error ->
                Log.w("FCM_REGISTER", "Unable to fetch FCM token before SOS", error)
                cont.resume(null)
            }
    }

    if (token.isNullOrBlank()) {
        Log.w("FCM_REGISTER", "Skipping pre-SOS registration: no token available")
        return
    }

    // Sync is now suspend-aware: it returns after the backend /push/register call completes.
    PushRegistrationSync.syncAndAwait(fcmToken = token, reason = "before_sos")
}

