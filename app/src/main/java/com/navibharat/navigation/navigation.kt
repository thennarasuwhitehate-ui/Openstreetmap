package com.navibharat.navigation

import com.google.android.gms.maps.model.LatLng

/**
 * Data models for navigation
 */
data class NavigationInstruction(
    val stepIndex: Int,
    val instruction: String,
    val maneuver: String, // "turn_left", "turn_right", "go_straight", "continue", "roundabout", "exit_highway"
    val distanceToTurn: Int, // meters
    val roadName: String? = null,
    val laneGuidance: String? = null,
    val speedLimit: Int? = null,
    val icon: String? = null // drawable resource name
)

data class NavigationRoute(
    val routeId: String,
    val origin: LatLng,
    val destination: LatLng,
    val polyline: String, // encoded polyline
    val totalDistance: Int, // meters
    val totalDuration: Int, // seconds
    val steps: List<NavigationInstruction>,
    val tollCost: Double? = null,
    val fuelCost: Double? = null,
    val alternativeRoutes: List<NavigationRoute>? = null
)

data class NavigationState(
    val route: NavigationRoute? = null,
    val currentLocation: LatLng? = null,
    val currentSpeed: Int = 0, // km/h
    val distanceRemaining: Int = 0, // meters
    val durationRemaining: Int = 0, // seconds
    val nextInstruction: NavigationInstruction? = null,
    val nextTurnDistance: Int = 0, // meters
    val isOffRoute: Boolean = false,
    val rerouting: Boolean = false
)

enum class NavigationStatus {
    IDLE,
    CALCULATING_ROUTE,
    ROUTE_CALCULATED,
    NAVIGATION_STARTED,
    NAVIGATION_PAUSED,
    NAVIGATION_ENDED,
    ERROR
}

data class RouteAlternative(
    val name: String, // "fastest", "shortest", "avoid_tolls"
    val route: NavigationRoute,
    val metadata: Map<String, Any>? = null
)
