package com.navibharat.navigation

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.RouteDao
import io.mockk.mockk
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NavigationEngineTest {

    private lateinit var navigationEngine: NavigationEngine
    private lateinit var mockRouteDao: RouteDao

    @Before
    fun setUp() {
        mockRouteDao = mockk()
        navigationEngine = NavigationEngine(mockRouteDao)
    }

    @Test
    fun testStartNavigation() {
        // Arrange
        val route = createTestRoute()

        // Act
        navigationEngine.startNavigation(route)

        // Assert
        val state = navigationEngine.navigationState.value
        assertEquals(NavigationStatus.NAVIGATION_STARTED, navigationEngine.navigationStatus.value)
        assertNotNull(state.route)
        assertEquals(route.totalDistance, state.distanceRemaining)
    }

    @Test
    fun testPauseNavigation() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        // Act
        navigationEngine.pauseNavigation()

        // Assert
        assertEquals(NavigationStatus.NAVIGATION_PAUSED, navigationEngine.navigationStatus.value)
    }

    @Test
    fun testResumeNavigation() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)
        navigationEngine.pauseNavigation()

        // Act
        navigationEngine.resumeNavigation()

        // Assert
        assertEquals(NavigationStatus.NAVIGATION_STARTED, navigationEngine.navigationStatus.value)
    }

    @Test
    fun testEndNavigation() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        // Act
        navigationEngine.endNavigation()

        // Assert
        assertEquals(NavigationStatus.NAVIGATION_ENDED, navigationEngine.navigationStatus.value)
        assertEquals(null, navigationEngine.navigationState.value.route)
    }

    @Test
    fun testLocationUpdate() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)
        val location = Location("gps").apply {
            latitude = route.origin.latitude
            longitude = route.origin.longitude
        }

        // Act
        navigationEngine.updateLocation(location)

        // Assert
        val state = navigationEngine.navigationState.value
        assertEquals(route.origin.latitude, state.currentLocation?.latitude, 0.01)
        assertEquals(route.origin.longitude, state.currentLocation?.longitude, 0.01)
    }

    @Test
    fun testNavigationInstruction() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        // Act
        val state = navigationEngine.navigationState.value

        // Assert
        assertNotNull(state.nextInstruction)
        assertTrue(state.nextInstruction?.distanceToTurn ?: 0 > 0)
    }

    @Test
    fun testDeviationDetection() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        // Create location far from route (off-road)
        val offRouteLocation = Location("gps").apply {
            latitude = route.origin.latitude + 1.0 // ~111 km away
            longitude = route.origin.longitude + 1.0
        }

        // Act
        navigationEngine.updateLocation(offRouteLocation)

        // Assert
        val state = navigationEngine.navigationState.value
        assertTrue(state.isOffRoute)
    }

    @Test
    fun testSpeedCalculation() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        val location = Location("gps").apply {
            latitude = route.origin.latitude
            longitude = route.origin.longitude
            speed = 13.8f // 13.8 m/s = ~50 km/h
        }

        // Act
        navigationEngine.updateLocation(location)

        // Assert
        val state = navigationEngine.navigationState.value
        assertEquals(50, state.currentSpeed) // Should be converted to km/h
    }

    @Test
    fun testNextTurnDistance() {
        // Arrange
        val route = createTestRoute()
        navigationEngine.startNavigation(route)

        val location = Location("gps").apply {
            latitude = route.origin.latitude
            longitude = route.origin.longitude
        }

        // Act
        navigationEngine.updateLocation(location)

        // Assert
        val state = navigationEngine.navigationState.value
        assertTrue(state.nextTurnDistance > 0)
    }

    private fun createTestRoute(): NavigationRoute {
        val origin = LatLng(28.7041, 77.1025) // Delhi
        val destination = LatLng(28.5244, 77.1855) // Noida

        val instructions = listOf(
            NavigationInstruction(
                stepIndex = 0,
                instruction = "Turn right onto MG Road",
                maneuver = "turn_right",
                distanceToTurn = 500,
                roadName = "MG Road"
            ),
            NavigationInstruction(
                stepIndex = 1,
                instruction = "Continue on NH-24",
                maneuver = "continue",
                distanceToTurn = 2000,
                roadName = "NH-24"
            )
        )

        return NavigationRoute(
            routeId = "test_route_1",
            origin = origin,
            destination = destination,
            polyline = "sample_polyline",
            totalDistance = 50000, // 50km
            totalDuration = 3600, // 1 hour
            steps = instructions,
            tollCost = 150.0,
            fuelCost = 250.0
        )
    }
}
