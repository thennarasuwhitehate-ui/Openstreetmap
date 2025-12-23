package com.navibharat.features.alerts

import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.RoadHazardDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

data class RoadHazard(
    val id: String,
    val lat: Double,
    val lng: Double,
    val type: HazardType,
    val severity: HazardSeverity = HazardSeverity.LOW,
    val description: String,
    val reportedBy: String? = null,
    val reportedTime: Long = System.currentTimeMillis(),
    val expiryTime: Long? = null
)

enum class HazardType(val displayName: String, val icon: String) {
    SPEED_BREAKER("Speed Breaker", "ic_speed_breaker"),
    NARROW_ROAD("Narrow Road", "ic_narrow_road"),
    DIVERSION("Road Diversion", "ic_diversion"),
    ACCIDENT("Accident", "ic_accident"),
    ROAD_WORKS("Road Works", "ic_road_works"),
    POT_HOLE("Pothole", "ic_pothole"),
    LANDSLIDE("Landslide", "ic_landslide")
}

enum class HazardSeverity(val priority: Int) {
    LOW(1),
    MEDIUM(2),
    HIGH(3)
}

@ActivityScoped
class RoadHazardManager @Inject constructor(
    private val roadHazardDao: RoadHazardDao
) {

    /**
     * Get hazards in a specific area
     */
    suspend fun getHazardsInArea(
        lat: Double,
        lng: Double,
        radiusKm: Int = 5
    ): List<RoadHazard> {
        return try {
            val radiusDegrees = radiusKm / 111.0 // Rough approximation

            val hazards = roadHazardDao.getHazardsInArea(
                lat - radiusDegrees,
                lat + radiusDegrees,
                lng - radiusDegrees,
                lng + radiusDegrees
            )

            hazards.map { entity ->
                RoadHazard(
                    id = entity.hazardId,
                    lat = entity.lat,
                    lng = entity.lng,
                    type = HazardType.valueOf(entity.type.uppercase()),
                    severity = HazardSeverity.valueOf(entity.severity.uppercase()),
                    description = entity.description,
                    reportedBy = entity.reportedBy,
                    reportedTime = entity.reportedTime,
                    expiryTime = entity.expiryTime
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting hazards in area")
            emptyList()
        }
    }

    /**
     * Get hazards on a route
     */
    suspend fun getHazardsOnRoute(polylineString: String): List<RoadHazard> {
        return try {
            val points = decodePolyline(polylineString)
            if (points.isEmpty()) return emptyList()

            val minLat = points.minOf { it.latitude }
            val maxLat = points.maxOf { it.latitude }
            val minLng = points.minOf { it.longitude }
            val maxLng = points.maxOf { it.longitude }

            val hazards = roadHazardDao.getHazardsInArea(minLat, maxLat, minLng, maxLng)

            hazards.map { entity ->
                RoadHazard(
                    id = entity.hazardId,
                    lat = entity.lat,
                    lng = entity.lng,
                    type = HazardType.valueOf(entity.type.uppercase()),
                    severity = HazardSeverity.valueOf(entity.severity.uppercase()),
                    description = entity.description,
                    reportedBy = entity.reportedBy,
                    reportedTime = entity.reportedTime,
                    expiryTime = entity.expiryTime
                )
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting hazards on route")
            emptyList()
        }
    }

    /**
     * Report a hazard
     */
    suspend fun reportHazard(
        lat: Double,
        lng: Double,
        hazardType: HazardType,
        severity: HazardSeverity,
        description: String,
        userId: String? = null,
        photoUrl: String? = null,
        expiryTime: Long? = null
    ): Boolean {
        return try {
            val entity = com.navibharat.data.local.RoadHazardEntity(
                hazardId = "${lat}_${lng}_${System.currentTimeMillis()}",
                lat = lat,
                lng = lng,
                type = hazardType.name.lowercase(),
                severity = severity.name.lowercase(),
                description = description,
                reportedBy = userId,
                reportedTime = System.currentTimeMillis(),
                expiryTime = expiryTime
            )

            roadHazardDao.insertHazard(entity)
            Timber.i("Hazard reported: ${hazardType.displayName} at $lat, $lng")
            true
        } catch (e: Exception) {
            Timber.e(e, "Error reporting hazard")
            false
        }
    }

    /**
     * Get active hazards (not expired)
     */
    fun getActiveHazards(): Flow<List<RoadHazard>> {
        return roadHazardDao.getActiveHazards(System.currentTimeMillis())
            .let { hazardFlow ->
                // Map to RoadHazard - implement proper transformation
                hazardFlow
            }
    }

    /**
     * Update hazard confirmation
     */
    suspend fun confirmHazard(hazardId: String): Boolean {
        return try {
            // In production, increment confirmation count via API
            true
        } catch (e: Exception) {
            Timber.e(e, "Error confirming hazard")
            false
        }
    }

    /**
     * Get hazard alert message for voice
     */
    fun getHazardAlertMessage(hazard: RoadHazard, language: String = "en"): String {
        val type = when (hazard.type) {
            HazardType.SPEED_BREAKER -> {
                when (language) {
                    "ta" -> "வேக குறைப்பான் முன்னால் உள்ளது"
                    "hi" -> "स्पीड ब्रेकर आगे है"
                    else -> "Speed breaker ahead"
                }
            }
            HazardType.NARROW_ROAD -> {
                when (language) {
                    "ta" -> "குறுகிய சாலை முன்னால் உள்ளது"
                    "hi" -> "सड़क आगे संकरी है"
                    else -> "Narrow road ahead"
                }
            }
            HazardType.DIVERSION -> {
                when (language) {
                    "ta" -> "சாலை மாற்று வழி உள்ளது"
                    "hi" -> "आगे मार्ग परिवर्तन है"
                    else -> "Road diversion ahead"
                }
            }
            HazardType.ACCIDENT -> {
                when (language) {
                    "ta" -> "முன்னால் விபத்து உள்ளது"
                    "hi" -> "आगे दुर्घटना है"
                    else -> "Accident ahead"
                }
            }
            else -> hazard.type.displayName
        }

        val distance = distanceToHazard(hazard)
        return "$type in ${distance}m"
    }

    private fun distanceToHazard(hazard: RoadHazard): Int {
        // Calculate distance to hazard from current location
        // This would be implemented with current location
        return 0
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
}
