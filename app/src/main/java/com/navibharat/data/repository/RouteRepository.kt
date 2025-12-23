package com.navibharat.data.repository

import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.NaviBharatDatabase
import com.navibharat.data.local.RouteEntity
import com.navibharat.data.network.ApiClient
import com.navibharat.navigation.NavigationRoute
import com.navibharat.navigation.NavigationInstruction
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class RouteRepository @Inject constructor(
    private val database: NaviBharatDatabase,
    private val apiKey: String = "YOUR_GOOGLE_MAPS_API_KEY"
) {

    private val mapsApi = ApiClient.getGoogleMapsService()

    /**
     * Calculate route using Google Maps Directions API
     */
    suspend fun calculateRoute(
        origin: LatLng,
        destination: LatLng,
        avoid: String? = null
    ): Result<NavigationRoute> {
        return try {
            val originStr = "${origin.latitude},${origin.longitude}"
            val destStr = "${destination.latitude},${destination.longitude}"

            val response = mapsApi.getDirections(
                origin = originStr,
                destination = destStr,
                apiKey = apiKey,
                avoid = avoid
            )

            if (response.status != "OK" || response.routes.isEmpty()) {
                return Result.failure(Exception("No routes found"))
            }

            val route = response.routes[0]
            val navigationRoute = convertToNavigationRoute(route, origin, destination)

            // Save to local database
            saveRouteLocally(navigationRoute)

            Result.success(navigationRoute)
        } catch (e: Exception) {
            Timber.e(e, "Error calculating route")
            Result.failure(e)
        }
    }

    /**
     * Get cached route from local database
     */
    suspend fun getCachedRoute(routeId: String): NavigationRoute? {
        return try {
            database.routeDao().getRouteById(routeId)?.let { entity ->
                convertEntityToNavigationRoute(entity)
            }
        } catch (e: Exception) {
            Timber.e(e, "Error getting cached route")
            null
        }
    }

    /**
     * Get recent routes
     */
    suspend fun getRecentRoutes() = database.routeDao().getRecentRoutes()

    private suspend fun saveRouteLocally(route: NavigationRoute) {
        try {
            val entity = RouteEntity(
                routeId = route.routeId,
                originLat = route.origin.latitude,
                originLng = route.origin.longitude,
                destLat = route.destination.latitude,
                destLng = route.destination.longitude,
                polylineEncoded = route.polyline,
                totalDistance = route.totalDistance.toLong(),
                totalDuration = route.totalDuration.toLong(),
                tollCost = route.tollCost,
                fuelCost = route.fuelCost
            )
            database.routeDao().insertRoute(entity)
            Timber.i("Route saved locally: ${route.routeId}")
        } catch (e: Exception) {
            Timber.e(e, "Error saving route locally")
        }
    }

    private fun convertToNavigationRoute(
        route: com.navibharat.data.network.DirectionRoute,
        origin: LatLng,
        destination: LatLng
    ): NavigationRoute {
        val instructions = mutableListOf<NavigationInstruction>()
        var stepIndex = 0

        for (leg in route.legs) {
            for (step in leg.steps) {
                instructions.add(
                    NavigationInstruction(
                        stepIndex = stepIndex++,
                        instruction = cleanHtmlInstructions(step.html_instructions),
                        maneuver = step.maneuver ?: "continue",
                        distanceToTurn = step.distance.value,
                        roadName = extractRoadName(step.html_instructions)
                    )
                )
            }
        }

        return NavigationRoute(
            routeId = "${System.currentTimeMillis()}_${(0..9999).random()}",
            origin = origin,
            destination = destination,
            polyline = route.polyline.points,
            totalDistance = route.distance.value,
            totalDuration = route.duration.value,
            steps = instructions,
            tollCost = null, // Will be calculated by TollManager
            fuelCost = null  // Will be calculated by FuelCostCalculator
        )
    }

    private fun convertEntityToNavigationRoute(entity: RouteEntity): NavigationRoute {
        return NavigationRoute(
            routeId = entity.routeId,
            origin = LatLng(entity.originLat, entity.originLng),
            destination = LatLng(entity.destLat, entity.destLng),
            polyline = entity.polylineEncoded,
            totalDistance = entity.totalDistance.toInt(),
            totalDuration = entity.totalDuration.toInt(),
            steps = emptyList(), // Steps not stored in entity
            tollCost = entity.tollCost,
            fuelCost = entity.fuelCost
        )
    }

    private fun cleanHtmlInstructions(html: String): String {
        return html
            .replace("<b>", "")
            .replace("</b>", "")
            .replace("<div.*?>".toRegex(), "")
            .replace("</div>", "")
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&amp;", "&")
            .trim()
    }

    private fun extractRoadName(instruction: String): String? {
        val pattern = "(?:onto|in|on|towards)\\s+([\\w\\s]+?)(?:\\s+|$)".toRegex()
        val match = pattern.find(instruction)
        return match?.groupValues?.get(1)?.trim()
    }
}
