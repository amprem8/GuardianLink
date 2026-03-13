@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package push

import android.util.Log
import com.example.guardianlink.PushRegistrationSync
import com.google.firebase.messaging.FirebaseMessaging

actual fun refreshPushRegistrationBeforeSos() {
    FirebaseMessaging.getInstance().token
        .addOnSuccessListener { token ->
            PushRegistrationSync.sync(fcmToken = token, reason = "before_sos")
        }
        .addOnFailureListener { error ->
            Log.w("FCM_REGISTER", "Unable to refresh token before SOS", error)
        }
}

