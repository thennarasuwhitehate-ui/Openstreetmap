package com.navibharat.maps

import android.content.Context
import android.content.SharedPreferences
import timber.log.Timber

class MapProviderFactory(
    private val context: Context,
    private val sharedPreferences: SharedPreferences
) {

    companion object {
        private const val PREF_MAP_PROVIDER = "map_provider"
        private const val PROVIDER_GOOGLE = "google_maps"
        private const val PROVIDER_MAPMYINDIA = "mapmyindia"
    }

    fun createMapProvider(): MapProvider {
        val provider = getPreferredProvider()
        return createProvider(provider)
    }

    private fun createProvider(providerName: String): MapProvider {
        return when (providerName) {
            PROVIDER_MAPMYINDIA -> {
                Timber.i("Using MapMyIndia provider")
                MapMyIndiaProvider()
            }
            PROVIDER_GOOGLE -> {
                Timber.i("Using Google Maps provider")
                GoogleMapsProvider()
            }
            else -> {
                Timber.i("Using default Google Maps provider")
                GoogleMapsProvider()
            }
        }
    }

    private fun getPreferredProvider(): String {
        return sharedPreferences.getString(PREF_MAP_PROVIDER, PROVIDER_GOOGLE) ?: PROVIDER_GOOGLE
    }

    fun setPreferredProvider(provider: String) {
        sharedPreferences.edit().putString(PREF_MAP_PROVIDER, provider).apply()
        Timber.i("Preferred provider set to: $provider")
    }

    fun switchToFallback() {
        val currentProvider = getPreferredProvider()
        val fallback = if (currentProvider == PROVIDER_GOOGLE) PROVIDER_MAPMYINDIA else PROVIDER_GOOGLE
        setPreferredProvider(fallback)
        Timber.i("Switched to fallback provider: $fallback")
    }

    fun shouldUseMapMyIndiaForLocation(lat: Double, lng: Double): Boolean {
        // Rural India detection: areas outside major metro regions
        // This is a simple heuristic; in production, use more sophisticated logic
        return false // For now, always prefer Google Maps
    }
}
