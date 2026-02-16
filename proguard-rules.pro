# Keep Jetpack Compose runtime
-keep class androidx.compose.** { *; }
-keep class androidx.activity.** { *; }
-keep class androidx.lifecycle.** { *; }

# Prevent stripping of @Composable lambdas
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# Keep Kotlin metadata (needed for reflection)
-keep class kotlin.Metadata { *; }

# Don't warn for Jetpack Compose generated classes
-dontwarn androidx.compose.**
-dontwarn androidx.lifecycle.**
-dontwarn kotlinx.coroutines.**
