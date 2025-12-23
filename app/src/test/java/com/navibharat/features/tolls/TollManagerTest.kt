package com.navibharat.features.tolls

import android.location.Location
import com.google.android.gms.maps.model.LatLng
import com.navibharat.data.local.TollPlazaDao
import com.navibharat.data.local.TollPlazaEntity
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TollManagerTest {

    private lateinit var tollManager: TollManager
    private lateinit var mockTollPlazaDao: TollPlazaDao

    @Before
    fun setUp() {
        mockTollPlazaDao = mockk()
        tollManager = TollManager(mockTollPlazaDao)
    }

    @Test
    fun testCalculateTollCostForCar() = runBlocking {
        // Arrange
        val polyline = "sample_encoded_polyline"
        val vehicleType = "car"
        val mockTollPlaza = TollPlazaEntity(
            tollId = "toll_1",
            name = "Delhi Expressway Toll",
            lat = 28.7041,
            lng = 77.1025,
            highway = "Delhi Expressway",
            rates = mapOf("car" to 100.0, "bike" to 50.0, "commercial" to 200.0)
        )

        coEvery { mockTollPlazaDao.getTollPlazasInArea(any(), any(), any(), any()) } returns listOf(mockTollPlaza)

        // Act
        val cost = tollManager.calculateRouteTollCost(polyline, vehicleType)

        // Assert
        assertEquals(100.0, cost, 0.1)
    }

    @Test
    fun testCalculateTollCostForBike() = runBlocking {
        // Arrange
        val polyline = "sample_encoded_polyline"
        val vehicleType = "bike"
        val mockTollPlaza = TollPlazaEntity(
            tollId = "toll_1",
            name = "Mumbai Pune Expressway Toll",
            lat = 19.0176,
            lng = 72.8479,
            highway = "Mumbai Pune Expressway",
            rates = mapOf("car" to 85.0, "bike" to 45.0, "commercial" to 180.0)
        )

        coEvery { mockTollPlazaDao.getTollPlazasInArea(any(), any(), any(), any()) } returns listOf(mockTollPlaza)

        // Act
        val cost = tollManager.calculateRouteTollCost(polyline, vehicleType)

        // Assert
        assertEquals(45.0, cost, 0.1)
    }

    @Test
    fun testMultipleTollBooths() = runBlocking {
        // Arrange
        val polyline = "sample_encoded_polyline"
        val vehicleType = "car"
        val mockTolls = listOf(
            TollPlazaEntity(
                tollId = "toll_1",
                name = "Toll 1",
                lat = 28.0,
                lng = 77.0,
                highway = "NH1",
                rates = mapOf("car" to 100.0)
            ),
            TollPlazaEntity(
                tollId = "toll_2",
                name = "Toll 2",
                lat = 27.0,
                lng = 77.0,
                highway = "NH1",
                rates = mapOf("car" to 80.0)
            )
        )

        coEvery { mockTollPlazaDao.getTollPlazasInArea(any(), any(), any(), any()) } returns mockTolls

        // Act
        val cost = tollManager.calculateRouteTollCost(polyline, vehicleType)

        // Assert
        assertEquals(180.0, cost, 0.1) // 100 + 80
    }

    @Test
    fun testNoTollBooths() = runBlocking {
        // Arrange
        val polyline = "sample_encoded_polyline"
        val vehicleType = "car"

        coEvery { mockTollPlazaDao.getTollPlazasInArea(any(), any(), any(), any()) } returns emptyList()

        // Act
        val cost = tollManager.calculateRouteTollCost(polyline, vehicleType)

        // Assert
        assertEquals(0.0, cost, 0.1)
    }

    @Test
    fun testTollBoothNotApplicableForVehicle() = runBlocking {
        // Arrange
        val polyline = "sample_encoded_polyline"
        val vehicleType = "motorcycle"
        val mockTollPlaza = TollPlazaEntity(
            tollId = "toll_1",
            name = "Toll",
            lat = 28.0,
            lng = 77.0,
            highway = "NH1",
            rates = mapOf("car" to 100.0) // No rate for motorcycle
        )

        coEvery { mockTollPlazaDao.getTollPlazasInArea(any(), any(), any(), any()) } returns listOf(mockTollPlaza)

        // Act
        val tolls = tollManager.findTollsOnRoute(polyline, vehicleType)

        // Assert
        assertEquals(0, tolls.size) // No tolls found since vehicle type not in rates
    }

    @Test
    fun testDistanceCalculation() {
        // Arrange
        val lat1 = 28.7041
        val lng1 = 77.1025
        val lat2 = 28.5244
        val lng2 = 77.1855

        // Act
        val distance = tollManager.getDistanceBetween(lat1, lng1, lat2, lng2)

        // Assert
        assert(distance > 0) // Should have positive distance
    }
}
