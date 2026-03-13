package location

import android.annotation.SuppressLint
import android.content.Context
import android.location.LocationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@SuppressLint("MissingPermission")
actual suspend fun getCurrentLocationOrNull(): CurrentLocation? = withContext(Dispatchers.Default) {
    val context = MainActivityHolder.context ?: return@withContext null
    val manager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager ?: return@withContext null

    val gpsEnabled = runCatching { manager.isProviderEnabled(LocationManager.GPS_PROVIDER) }.getOrDefault(false)
    val networkEnabled = runCatching { manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) }.getOrDefault(false)
    if (!gpsEnabled && !networkEnabled) return@withContext null

    val candidates = buildList {
        runCatching { manager.getLastKnownLocation(LocationManager.GPS_PROVIDER) }.getOrNull()?.let { add(it) }
        runCatching { manager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) }.getOrNull()?.let { add(it) }
        runCatching { manager.getLastKnownLocation(LocationManager.PASSIVE_PROVIDER) }.getOrNull()?.let { add(it) }
    }

    val best = candidates.maxByOrNull { it.time } ?: return@withContext null
    CurrentLocation(latitude = best.latitude, longitude = best.longitude)
}

internal object MainActivityHolder {
    @Volatile
    var context: Context? = null
}


