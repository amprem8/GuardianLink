@file:Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")

package network

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

actual object NetworkConnectivityObserver {

    @SuppressLint("StaticFieldLeak")
    private lateinit var appContext: Context
    private var connectivityManager: ConnectivityManager? = null

    private val _isOnline = MutableStateFlow(false)
    actual val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private var isStarted = false

    /**
     * Must be called from [MainActivity.onCreate] with app context.
     */
    fun init(context: Context) {
        appContext = context.applicationContext
        connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        // Read initial state immediately
        _isOnline.value = checkCurrentConnectivity()
    }

    actual fun start() {
        if (isStarted) return
        isStarted = true

        val cm = connectivityManager ?: return

        // Register callback for real-time updates
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            .build()

        cm.registerNetworkCallback(request, networkCallback)
    }

    actual fun stop() {
        if (!isStarted) return
        isStarted = false
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    private fun checkCurrentConnectivity(): Boolean {
        val cm = connectivityManager ?: return false
        val network = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(network) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    // Track active networks for accurate state
    private val activeNetworks = mutableSetOf<Network>()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            activeNetworks.add(network)
            _isOnline.value = true
        }

        override fun onLost(network: Network) {
            activeNetworks.remove(network)
            _isOnline.value = activeNetworks.isNotEmpty()
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
            if (hasInternet) {
                activeNetworks.add(network)
            } else {
                activeNetworks.remove(network)
            }
            _isOnline.value = activeNetworks.isNotEmpty()
        }
    }
}
