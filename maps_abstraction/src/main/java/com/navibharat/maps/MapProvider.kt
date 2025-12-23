package com.navibharat.maps

import android.content.Context
import android.view.View
import com.google.android.gms.maps.model.LatLng

/**
 * Abstracted interface for map providers (Google Maps, MapMyIndia, etc.)
 */
interface MapProvider {

    /**
     * Initialize the map with API key
     */
    fun initializeMap(context: Context, apiKey: String)

    /**
     * Get the underlying map view
     */
    fun getMapView(): View

    /**
     * Draw a route on the map
     */
    fun drawRoute(route: Route, color: Int)

    /**
     * Clear all routes from the map
     */
    fun clearRoutes()

    /**
     * Add a marker to the map
     */
    fun addMarker(lat: Double, lng: Double, title: String, icon: Int? = null): Marker

    /**
     * Remove a marker from the map
     */
    fun removeMarker(marker: Marker)

    /**
     * Animate camera to specific location
     */
    fun animateCamera(lat: Double, lng: Double, zoom: Float, duration: Int = 500)

    /**
     * Set camera change listener
     */
    fun setCameraListener(listener: CameraListener)

    /**
     * Clear all markers and routes
     */
    fun clearMap()

    /**
     * Enable/disable traffic overlay
     */
    fun enableTraffic(enabled: Boolean)

    /**
     * Geocode address to coordinates
     */
    suspend fun geocode(address: String): LatLng?

    /**
     * Reverse geocode coordinates to address
     */
    suspend fun reverseGeocode(lat: Double, lng: Double): String?

    /**
     * Get distance between two coordinates
     */
    fun getDistanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float
}

data class Route(
    val polyline: String,
    val distanceMeters: Int,
    val durationSeconds: Int,
    val steps: List<RouteStep> = emptyList()
)

data class RouteStep(
    val instruction: String,
    val distance: Int,
    val duration: Int
)

data class Marker(
    val id: String,
    val lat: Double,
    val lng: Double,
    val title: String,
    val description: String? = null
)

interface CameraListener {
    fun onCameraChange(lat: Double, lng: Double, zoom: Float)
}
