package com.navibharat.data.repository

import com.navibharat.data.network.ApiClient
import com.navibharat.features.services.NearbyService
import com.navibharat.features.services.ServiceType
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class ServicesRepository @Inject constructor(
    private val apiKey: String = "YOUR_GOOGLE_PLACES_API_KEY"
) {

    private val placesApi = ApiClient.getGooglePlacesService()

    /**
     * Search for nearby services using Google Places API
     */
    suspend fun searchNearbyServices(
        latitude: Double,
        longitude: Double,
        serviceType: ServiceType,
        radiusMeters: Int = 5000
    ): Result<List<NearbyService>> {
        return try {
            val response = placesApi.searchNearby(
                latitude = latitude,
                longitude = longitude,
                radiusMeters = radiusMeters,
                type = serviceType.placesType,
                apiKey = apiKey
            )

            val services = response.places.map { place ->
                NearbyService(
                    id = place.placeId,
                    name = place.name,
                    lat = place.location.latitude,
                    lng = place.location.longitude,
                    address = place.address ?: "",
                    rating = place.rating,
                    distance = 0, // Calculate based on user location
                    serviceType = serviceType.name.lowercase(),
                    photos = place.photos?.map { it.name }
                )
            }

            Timber.i("Found ${services.size} ${serviceType.displayName} services")
            Result.success(services)
        } catch (e: Exception) {
            Timber.e(e, "Error searching nearby services")
            Result.failure(e)
        }
    }

    /**
     * Get multiple service types in parallel
     */
    suspend fun searchMultipleServices(
        latitude: Double,
        longitude: Double,
        serviceTypes: List<ServiceType> = listOf(
            ServiceType.PETROL_BUNK,
            ServiceType.EV_CHARGER,
            ServiceType.MECHANIC
        )
    ): Result<Map<ServiceType, List<NearbyService>>> {
        return try {
            val results = mutableMapOf<ServiceType, List<NearbyService>>()

            for (serviceType in serviceTypes) {
                val result = searchNearbyServices(latitude, longitude, serviceType)
                if (result.isSuccess) {
                    results[serviceType] = result.getOrNull() ?: emptyList()
                }
            }

            Result.success(results)
        } catch (e: Exception) {
            Timber.e(e, "Error searching multiple services")
            Result.failure(e)
        }
    }

    /**
     * Get place details (requires additional API call)
     */
    suspend fun getPlaceDetails(placeId: String): Result<Map<String, Any>> {
        return try {
            // In production, call Google Places Details API
            // val response = placesApi.getPlaceDetails(placeId, apiKey)
            Result.success(emptyMap())
        } catch (e: Exception) {
            Timber.e(e, "Error getting place details")
            Result.failure(e)
        }
    }
}
