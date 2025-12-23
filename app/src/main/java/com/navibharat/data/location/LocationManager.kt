package com.navibharat.data.location

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.Priority
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.receiveAsFlow
import timber.log.Timber
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LocationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val fusedLocationClient: FusedLocationProviderClient
) {

    private val locationChannel = Channel<Location>(capacity = Channel.UNLIMITED)
    val locationFlow: Flow<Location> = locationChannel.receiveAsFlow()

    private var locationCallback: LocationCallback? = null
    private var isTracking = false

    fun startLocationUpdates(batteryOptimizationMode: Boolean = false) {
        if (isTracking) return
        if (!hasLocationPermission()) {
            Timber.w("Location permission not granted")
            return
        }

        try {
            val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000)
                .setMinUpdateDistanceMeters(5f)
                .setMinUpdateIntervalMillis(1000)
                .apply {
                    if (batteryOptimizationMode) {
                        setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                        setMinUpdateIntervalMillis(5000)
                    }
                }
                .build()

            locationCallback = object : LocationCallback() {
                override fun onLocationResult(locationResult: LocationResult) {
                    for (location in locationResult.locations) {
                        try {
                            locationChannel.trySend(location)
                            Timber.d("Location update: lat=${location.latitude}, lng=${location.longitude}, accuracy=${location.accuracy}")
                        } catch (e: Exception) {
                            Timber.e(e, "Error sending location")
                        }
                    }
                }
            }

            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback!!, null)
            isTracking = true
            Timber.i("Location tracking started")
        } catch (e: Exception) {
            Timber.e(e, "Failed to start location updates")
        }
    }

    fun stopLocationUpdates() {
        if (!isTracking) return
        try {
            locationCallback?.let {
                fusedLocationClient.removeLocationUpdates(it)
            }
            isTracking = false
            Timber.i("Location tracking stopped")
        } catch (e: Exception) {
            Timber.e(e, "Error stopping location updates")
        }
    }

    suspend fun getCurrentLocation(): Location? {
        return try {
            if (!hasLocationPermission()) {
                Timber.w("Location permission not granted")
                return null
            }

            val task = fusedLocationClient.lastLocation
            val result = task.result
            Timber.d("Got current location: lat=${result?.latitude}, lng=${result?.longitude}")
            result
        } catch (e: Exception) {
            Timber.e(e, "Failed to get current location")
            null
        }
    }

    private fun hasLocationPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == android.content.pm.PackageManager.PERMISSION_GRANTED
    }

    fun isTrackingActive(): Boolean = isTracking
}
