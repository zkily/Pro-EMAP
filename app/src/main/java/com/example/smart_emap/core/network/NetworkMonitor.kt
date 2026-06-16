package com.example.smart_emap.core.network

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class NetworkMonitor(context: Context) {
    private val connectivity = context.applicationContext
        .getSystemService(ConnectivityManager::class.java)

    private val _isOnline = MutableStateFlow(checkOnline())
    val isOnline: StateFlow<Boolean> = _isOnline.asStateFlow()

    private val callback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            _isOnline.value = evaluateOnline(connectivity.getNetworkCapabilities(network))
        }

        override fun onLost(network: Network) {
            _isOnline.value = checkOnline()
        }

        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) {
            _isOnline.value = evaluateOnline(caps)
        }
    }

    init {
        val request = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()
        connectivity.registerNetworkCallback(request, callback)
    }

    fun currentOnline(): Boolean = _isOnline.value

    fun checkOnline(): Boolean {
        val network = connectivity.activeNetwork ?: return false
        return evaluateOnline(connectivity.getNetworkCapabilities(network))
    }

    private fun evaluateOnline(caps: NetworkCapabilities?): Boolean {
        if (caps == null) return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
            caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }

    fun dispose() {
        runCatching { connectivity.unregisterNetworkCallback(callback) }
    }
}
