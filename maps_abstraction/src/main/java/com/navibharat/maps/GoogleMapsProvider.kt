package com.navibharat.maps

import android.content.Context
import android.graphics.Color
import android.view.View
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import timber.log.Timber

class GoogleMapsProvider : MapProvider, OnMapReadyCallback {

    private var mapView: MapView? = null
    private var googleMap: GoogleMap? = null
    private var cameraListener: CameraListener? = null
    private val markers = mutableMapOf<String, com.google.android.gms.maps.model.Marker>()
    private var currentPolyline: com.google.android.gms.maps.model.Polyline? = null

    override fun initializeMap(context: Context, apiKey: String) {
        try {
            mapView = MapView(context)
            mapView?.onCreate(null)
            mapView?.getMapAsync(this)
            Timber.i("GoogleMapsProvider initialized")
        } catch (e: Exception) {
            Timber.e(e, "Failed to initialize GoogleMapsProvider")
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        googleMap?.apply {
            mapType = GoogleMap.MAP_TYPE_NORMAL
            setTrafficEnabled(false)
            setOnCameraChangeListener { cameraPosition ->
                cameraListener?.onCameraChange(
                    cameraPosition.target.latitude,
                    cameraPosition.target.longitude,
                    cameraPosition.zoom
                )
            }
        }
        Timber.i("Google Map ready")
    }

    override fun getMapView(): View {
        return mapView ?: throw IllegalStateException("MapView not initialized")
    }

    override fun drawRoute(route: Route, color: Int) {
        clearRoutes()
        try {
            googleMap?.let { map ->
                val polylineOptions = PolylineOptions()
                    .color(color)
                    .width(8f)

                val points = decodePolyline(route.polyline)
                polylineOptions.addAll(points)

                currentPolyline = map.addPolyline(polylineOptions)
                Timber.i("Route drawn with ${points.size} points")
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to draw route")
        }
    }

    override fun clearRoutes() {
        currentPolyline?.remove()
        currentPolyline = null
    }

    override fun addMarker(lat: Double, lng: Double, title: String, icon: Int?): Marker {
        val id = "${System.currentTimeMillis()}-${Math.random()}"
        try {
            googleMap?.let { map ->
                val markerOptions = MarkerOptions()
                    .position(LatLng(lat, lng))
                    .title(title)

                val googleMarker = map.addMarker(markerOptions)
                if (googleMarker != null) {
                    markers[id] = googleMarker
                    Timber.i("Marker added: $title")
                }
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to add marker")
        }

        return Marker(id, lat, lng, title)
    }

    override fun removeMarker(marker: Marker) {
        markers.remove(marker.id)?.remove()
    }

    override fun animateCamera(lat: Double, lng: Double, zoom: Float, duration: Int) {
        try {
            googleMap?.let { map ->
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(LatLng(lat, lng), zoom)
                map.animateCamera(cameraUpdate, duration, null)
            }
        } catch (e: Exception) {
            Timber.e(e, "Failed to animate camera")
        }
    }

    override fun setCameraListener(listener: CameraListener) {
        cameraListener = listener
    }

    override fun clearMap() {
        clearRoutes()
        markers.values.forEach { it.remove() }
        markers.clear()
    }

    override fun enableTraffic(enabled: Boolean) {
        googleMap?.setTrafficEnabled(enabled)
    }

    override suspend fun geocode(address: String): LatLng? {
        // Implement with Google Places API or Geocoding API
        Timber.d("Geocode called for: $address")
        return null // Placeholder
    }

    override suspend fun reverseGeocode(lat: Double, lng: Double): String? {
        // Implement with Geocoding API
        Timber.d("Reverse geocode called for: $lat, $lng")
        return null // Placeholder
    }

    override fun getDistanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }

    /**
     * Decode polyline string (Google Maps Encoded Polyline Format)
     */
    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = mutableListOf<LatLng>()
        var index = 0
        var lat = 0
        var lng = 0

        while (index < encoded.length) {
            var result = 0
            var shift = 0
            var b: Int
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lat += dlat

            result = 0
            shift = 0
            do {
                b = encoded[index++].code - 63
                result = result or ((b and 0x1f) shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if ((result and 1) != 0) (result shr 1).inv() else (result shr 1)
            lng += dlng

            poly.add(LatLng(lat / 1e5, lng / 1e5))
        }

        return poly
    }
}
