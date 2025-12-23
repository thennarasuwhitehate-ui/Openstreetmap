package com.navibharat.navigation

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.RouteDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class NavigationEngine @Inject constructor(
    private val routeDao: RouteDao
) {

    private val _navigationState = MutableStateFlow<NavigationState>(NavigationState())
    val navigationState: StateFlow<NavigationState> = _navigationState

    private val _navigationStatus = MutableStateFlow(NavigationStatus.IDLE)
    val navigationStatus: StateFlow<NavigationStatus> = _navigationStatus

    private val _reachingTurn = MutableStateFlow<NavigationInstruction?>(null)
    val reachingTurn: StateFlow<NavigationInstruction?> = _reachingTurn

    private val _navigationInstructions = MutableStateFlow<NavigationInstruction?>(null)
    val navigationInstructions: StateFlow<NavigationInstruction?> = _navigationInstructions

    private var deviationCounter = 0

    /**
     * Start navigation with a route
     */
    fun startNavigation(route: NavigationRoute) {
        _navigationStatus.value = NavigationStatus.NAVIGATION_STARTED
        _navigationState.value = NavigationState(
            route = route,
            distanceRemaining = route.totalDistance,
            durationRemaining = route.totalDuration,
            nextInstruction = route.steps.firstOrNull()
        )
        Timber.i("Navigation started for route: ${route.routeId}")
    }

    /**
     * Update current location and check progress
     */
    fun updateLocation(location: Location) {
        val currentState = _navigationState.value
        val route = currentState.route ?: return

        val currentLocation = LatLng(location.latitude, location.longitude)
        val currentSpeed = (location.speed * 3.6).toInt() // m/s to km/h

        // Find current position on route
        val (distanceRemaining, nextInstructionIndex) = calculateRemainingDistance(
            currentLocation,
            route
        )

        val durationRemaining = if (route.totalDistance > 0) {
            (distanceRemaining * route.totalDuration / route.totalDistance).toInt()
        } else 0

        val nextInstruction = if (nextInstructionIndex >= 0 && nextInstructionIndex < route.steps.size) {
            route.steps[nextInstructionIndex]
        } else null

        val nextTurnDistance = distanceRemaining

        // Check if reaching turn (within 100m)
        if (nextTurnDistance in 1..100 && nextInstruction != null) {
            _reachingTurn.value = nextInstruction
        } else {
            _reachingTurn.value = null
        }

        // Update current navigation instruction
        _navigationInstructions.value = nextInstruction

        // Check for off-route condition
        val isOffRoute = checkOffRoute(currentLocation, route)
        if (isOffRoute) {
            deviationCounter++
        } else {
            deviationCounter = 0
        }

        val shouldReroute = deviationCounter > 30 // Off route for 30+ updates

        _navigationState.value = NavigationState(
            route = route,
            currentLocation = currentLocation,
            currentSpeed = currentSpeed,
            distanceRemaining = distanceRemaining,
            durationRemaining = durationRemaining,
            nextInstruction = nextInstruction,
            nextTurnDistance = nextTurnDistance,
            isOffRoute = isOffRoute,
            rerouting = shouldReroute
        )

        if (shouldReroute) {
            Timber.w("User off-route for too long, triggering reroute")
            _navigationStatus.value = NavigationStatus.ERROR
        }
    }

    /**
     * Calculate remaining distance to destination
     */
    private fun calculateRemainingDistance(
        currentLocation: LatLng,
        route: NavigationRoute
    ): Pair<Int, Int> {
        var remainingDistance = route.totalDistance
        var nextInstructionIndex = 0

        // Decode polyline and find closest point
        val decodedPoints = decodePolyline(route.polyline)
        var closestIndex = 0
        var minDistance = Float.MAX_VALUE

        for (i in decodedPoints.indices) {
            val distance = distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                decodedPoints[i].latitude,
                decodedPoints[i].longitude
            )
            if (distance < minDistance) {
                minDistance = distance
                closestIndex = i
            }
        }

        // Estimate remaining distance
        if (closestIndex < decodedPoints.size) {
            remainingDistance = (route.totalDistance * (1 - closestIndex.toFloat() / decodedPoints.size)).toInt()
            nextInstructionIndex = (route.steps.size * closestIndex / decodedPoints.size).coerceAtMost(route.steps.size - 1)
        }

        return Pair(remainingDistance, nextInstructionIndex)
    }

    /**
     * Check if user is off-route (tolerance = 50m)
     */
    private fun checkOffRoute(currentLocation: LatLng, route: NavigationRoute): Boolean {
        val decodedPoints = decodePolyline(route.polyline)
        if (decodedPoints.isEmpty()) return false

        var minDistance = Float.MAX_VALUE
        for (point in decodedPoints) {
            val distance = distanceBetween(
                currentLocation.latitude,
                currentLocation.longitude,
                point.latitude,
                point.longitude
            )
            minDistance = minOf(minDistance, distance)
        }

        return minDistance > 50 // 50m tolerance
    }

    /**
     * Pause navigation
     */
    fun pauseNavigation() {
        _navigationStatus.value = NavigationStatus.NAVIGATION_PAUSED
        Timber.i("Navigation paused")
    }

    /**
     * Resume navigation
     */
    fun resumeNavigation() {
        if (_navigationStatus.value == NavigationStatus.NAVIGATION_PAUSED) {
            _navigationStatus.value = NavigationStatus.NAVIGATION_STARTED
            Timber.i("Navigation resumed")
        }
    }

    /**
     * End navigation
     */
    fun endNavigation() {
        _navigationStatus.value = NavigationStatus.NAVIGATION_ENDED
        _navigationState.value = NavigationState()
        Timber.i("Navigation ended")
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

    /**
     * Calculate distance between two coordinates
     */
    private fun distanceBetween(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val results = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, results)
        return results[0]
    }
}
