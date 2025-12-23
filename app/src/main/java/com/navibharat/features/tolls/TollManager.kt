package com.navibharat.features.tolls

import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.TollPlazaDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

data class TollBooth(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val highway: String,
    val vehicleType: String,
    val rates: Map<String, Double>, // "car" → 100.0, "bike" → 50.0, "truck" → 200.0
    val operationalHours: String? = null
)

data class TollCost(
    val boothId: String,
    val boothName: String,
    val vehicleType: String,
    val costInINR: Double,
    val distance: Int // meters from current location
)

@ActivityScoped
class TollManager @Inject constructor(
    private val tollPlazaDao: TollPlazaDao
) {

    // Default toll rates for major highways (in INR) - will be replaced with API data
    private val defaultTollRates = mapOf(
        "Delhi Expressway" to mapOf("car" to 100.0, "bike" to 50.0, "commercial" to 200.0),
        "Mumbai Pune Expressway" to mapOf("car" to 85.0, "bike" to 45.0, "commercial" to 180.0),
        "NH 1" to mapOf("car" to 75.0, "bike" to 40.0, "commercial" to 150.0),
        "NH 2" to mapOf("car" to 70.0, "bike" to 35.0, "commercial" to 140.0)
    )

    /**
     * Find toll booths on a route (polyline)
     */
    suspend fun findTollsOnRoute(
        polylineString: String,
        vehicleType: String = "car"
    ): List<TollCost> {
        try {
            val points = decodePolyline(polylineString)
            if (points.isEmpty()) return emptyList()

            // Get bounding box of route
            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLng = points.minOf { it.longitude }
            val maxLng = points.maxOf { it.longitude }

            // Query toll plazas in area
            val tollPlazas = tollPlazaDao.getTollPlazasInArea(minLat, maxLat, minLng, maxLng)

            // Calculate costs for each toll
            return tollPlazas.mapNotNull { plaza ->
                val rate = plaza.rates[vehicleType] ?: return@mapNotNull null
                TollCost(
                    boothId = plaza.tollId,
                    boothName = plaza.name,
                    vehicleType = vehicleType,
                    costInINR = rate,
                    distance = 0
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error finding tolls on route")
            return emptyList()
        }
    }

    /**
     * Calculate total toll cost for route
     */
    suspend fun calculateRouteTollCost(
        polylineString: String,
        vehicleType: String = "car"
    ): Double {
        return findTollsOnRoute(polylineString, vehicleType).sumOf { it.costInINR }
    }

    /**
     * Calculate remaining toll cost from current position
     */
    suspend fun calculateRemainingTollCost(
        currentLocation: LatLng,
        polylineString: String,
        vehicleType: String = "car"
    ): Double {
        try {
            val tolls = findTollsOnRoute(polylineString, vehicleType)
            val points = decodePolyline(polylineString)

            // Find closest point on route
            var closestIndex = 0
            var minDistance = Float.MAX_VALUE

            for (i in points.indices) {
                val distance = distanceBetween(
                    currentLocation.latitude,
                    currentLocation.longitude,
                    points[i].latitude,
                    points[i].longitude
                )
                if (distance < minDistance) {
                    minDistance = distance
                    closestIndex = i
                }
            }

            // Only count tolls ahead of current position
            val remainingRatio = (points.size - closestIndex).toFloat() / points.size
            return calculateRouteTollCost(polylineString, vehicleType) * remainingRatio
        } catch (e: Exception) {
            Timber.e(e, "Error calculating remaining toll")
            return 0.0
        }
    }

    /**
     * Get toll booths along a route
     */
    suspend fun getTollBoothsOnRoute(
        polylineString: String
    ): List<TollBooth> {
        return try {
            val points = decodePolyline(polylineString)
            if (points.isEmpty()) return emptyList()

            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLng = points.minOf { it.longitude }
            val maxLng = points.maxOf { it.longitude }

            val tollPlazas = tollPlazaDao.getTollPlazasInArea(minLat, maxLat, minLng, maxLng)

            tollPlazas.map { plaza ->
                TollBooth(
                    id = plaza.tollId,
                    name = plaza.name,
                    lat = plaza.lat,
                    lng = plaza.lng,
                    highway = plaza.highway,
                    vehicleType = "car",
                    rates = plaza.rates,
                    operationalHours = plaza.operationalHours
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting toll booths on route")
            emptyList()
        }
    }

    /**
     * Initialize with default toll data
     */
    suspend fun initializeDefaultTolls() {
        // Will be replaced with API integration
        Timber.i("Initializing default toll data")
    }

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

    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        android.location.Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }
}
