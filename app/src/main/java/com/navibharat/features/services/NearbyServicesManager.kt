package com.navibharat.features.services

import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

data class NearbyService(
    val id: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val address: String,
    val rating: Float? = null,
    val isOpenNow: Boolean? = null,
    val distance: Int = 0, // meters
    val serviceType: String,
    val photos: List<String>? = null,
    val priceLevel: Int? = null // 1-4 for Google Places
)

enum class ServiceType(val displayName: String, val placesType: String) {
    PETROL_BUNK("Petrol Bunk", "gas_station"),
    EV_CHARGER("EV Charger", "electric_vehicle_charging_station"),
    MECHANIC("Mechanic", "car_repair"),
    REST_AREA("Rest Area", "rest_stop"),
    HOSPITAL("Hospital", "hospital"),
    POLICE("Police", "police"),
    ATM("ATM", "atm"),
    HOTEL("Hotel", "lodging"),
    RESTAURANT("Restaurant", "restaurant")
}

@ActivityScoped
class NearbyServicesManager @Inject constructor() {

    /**
     * Find nearby services of a specific type
     * In production, integrate with Google Places API or MapMyIndia API
     */
    suspend fun findNearbyServices(
        lat: Double,
        lng: Double,
        serviceType: ServiceType,
        radiusMeters: Int = 5000
    ): List<NearbyService> {
        return try {
            // Placeholder implementation
            // In production:
            // 1. Call Google Places API nearbySearch()
            // 2. Parse results and convert to NearbyService objects
            // 3. Sort by distance
            // 4. Cache results

            val services = mutableListOf<NearbyService>()

            // Example: Hardcoded data for demo purposes
            // In production, replace with actual API calls
            when (serviceType) {
                ServiceType.PETROL_BUNK -> {
                    services.add(
                        NearbyService(
                            id = "petrol_1",
                            name = "Indian Oil Petrol Pump",
                            lat = lat + 0.01,
                            lng = lng + 0.01,
                            address = "Near Railway Station",
                            rating = 4.2f,
                            isOpenNow = true,
                            distance = 2000,
                            serviceType = "petrol"
                        )
                    )
                }
                ServiceType.EV_CHARGER -> {
                    services.add(
                        NearbyService(
                            id = "ev_1",
                            name = "Tesla Supercharger",
                            lat = lat - 0.01,
                            lng = lng - 0.01,
                            address = "Shopping Mall",
                            rating = 4.8f,
                            isOpenNow = true,
                            distance = 3500,
                            serviceType = "ev"
                        )
                    )
                }
                ServiceType.MECHANIC -> {
                    services.add(
                        NearbyService(
                            id = "mechanic_1",
                            name = "ABC Auto Repairs",
                            lat = lat + 0.005,
                            lng = lng - 0.005,
                            address = "Main Street",
                            rating = 4.0f,
                            isOpenNow = true,
                            distance = 1500,
                            serviceType = "mechanic"
                        )
                    )
                }
                ServiceType.REST_AREA -> {
                    services.add(
                        NearbyService(
                            id = "rest_1",
                            name = "Highway Rest Stop",
                            lat = lat + 0.02,
                            lng = lng + 0.02,
                            address = "Highway NH1",
                            rating = 3.5f,
                            isOpenNow = true,
                            distance = 5000,
                            serviceType = "rest"
                        )
                    )
                }
                else -> {
                    // Return empty for other types (would be implemented)
                }
            }

            Timber.i("Found ${services.size} ${serviceType.displayName} services")
            services
        } catch (e: Exception) {
            Timber.e(e, "Error finding nearby services")
            emptyList()
        }
    }

    /**
     * Find multiple service types in one call
     */
    suspend fun findMultipleServices(
        lat: Double,
        lng: Double,
        serviceTypes: List<ServiceType> = listOf(
            ServiceType.PETROL_BUNK,
            ServiceType.EV_CHARGER,
            ServiceType.MECHANIC
        ),
        radiusMeters: Int = 5000
    ): Map<ServiceType, List<NearbyService>> {
        return serviceTypes.associate { serviceType ->
            serviceType to findNearbyServices(lat, lng, serviceType, radiusMeters)
        }
    }

    /**
     * Find services along a route
     */
    suspend fun findServicesAlongRoute(
        polylineString: String,
        serviceType: ServiceType,
        radiusMeters: Int = 3000
    ): List<NearbyService> {
        return try {
            val decodedPoints = decodePolyline(polylineString)
            if (decodedPoints.isEmpty()) return emptyList()

            val allServices = mutableListOf<NearbyService>()

            // Check services at multiple points along route
            for (i in decodedPoints.indices step (decodedPoints.size / 5)) {
                val point = decodedPoints[i]
                val services = findNearbyServices(point.latitude, point.longitude, serviceType, radiusMeters)
                allServices.addAll(services)
            }

            // Remove duplicates based on ID
            allServices.distinctBy { it.id }
        } catch (e: Exception) {
            Timber.e(e, "Error finding services along route")
            emptyList()
        }
    }

    /**
     * Get service details (would fetch from Google Places)
     */
    suspend fun getServiceDetails(serviceId: String): NearbyService? {
        // Placeholder - in production, call Google Places API details endpoint
        return null
    }

    /**
     * Get reviews for a service
     */
    suspend fun getServiceReviews(serviceId: String): List<ServiceReview> {
        // Placeholder - in production, call Google Places API reviews endpoint
        return emptyList()
    }

    private fun decodePolyline(encoded: String): List<android.location.Location> {
        val locations = mutableListOf<android.location.Location>()
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

            val location = android.location.Location("").apply {
                latitude = lat / 1e5
                longitude = lng / 1e5
            }
            locations.add(location)
        }

        return locations
    }
}

data class ServiceReview(
    val authorName: String,
    val rating: Int,
    val text: String,
    val time: Long
)
