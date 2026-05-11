package location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import storage.AppStorage

@SuppressLint("MissingPermission")
actual suspend fun getCurrentLocationOrNull(): CurrentLocation? = withContext(Dispatchers.Default) {
    val context = MainActivityHolder.context ?: return@withContext storedLocationOrNull()
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        ?: return@withContext storedLocationOrNull()

    val gpsEnabled = runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
    val networkEnabled = runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)

    if (!gpsEnabled && !networkEnabled) {
        // Neither provider is on — return last cached location so SOS still includes GPS
        return@withContext storedLocationOrNull()
    }

    val candidates = buildList {
        runCatching { manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()?.let { add(it) }
        runCatching { manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()?.let { add(it) }
        runCatching { manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()?.let { add(it) }
    }

    val best = candidates.maxByOrNull { it.time }

    return@withContext if (best != null) {
        // Persist fresh location so it's available next time GPS is off
        AppStorage.setLastKnownLocation(best.latitude, best.longitude)
        CurrentLocation(latitude = best.latitude, longitude = best.longitude)
    } else {
        // Providers are enabled but no fix yet — use stored fallback
        storedLocationOrNull()
    }
}

/** Returns the last location that was successfully fetched and persisted inside the app. */
fun storedLocationOrNull(): CurrentLocation? {
    val lat = AppStorage.getLastKnownLat() ?: return null
    val lng = AppStorage.getLastKnownLng() ?: return null
    return CurrentLocation(latitude = lat, longitude = lng)
}

internal object MainActivityHolder {
    @Volatile
    var context: Context? = null
}


