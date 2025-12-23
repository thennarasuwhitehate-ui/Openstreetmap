package com.navibharat.features.fuel

import com.navibharat.data.local.FuelPriceDao
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class FuelCostCalculatorTest {

    private lateinit var fuelCostCalculator: FuelCostCalculator
    private lateinit var mockFuelPriceDao: FuelPriceDao

    @Before
    fun setUp() {
        mockFuelPriceDao = mockk()
        fuelCostCalculator = FuelCostCalculator(mockFuelPriceDao)
    }

    @Test
    fun testFuelCostCalculation_Car_Petrol() = runBlocking {
        // Arrange
        val distance = 100
        val mileage = 15 // km/l
        val fuelType = "petrol"
        val expectedLiters = (distance / mileage).toFloat()
        val pricePerLiter = 96.0

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, mileage, fuelType)

        // Assert
        assertEquals(distance, result.distanceKm)
        assertEquals(expectedLiters, result.litersNeeded, 0.1f)
        assertEquals(mileage, result.mileageUsed)
    }

    @Test
    fun testFuelCostCalculation_Bike_Petrol() = runBlocking {
        // Arrange
        val distance = 100
        val mileage = 40 // km/l for bike
        val fuelType = "petrol"
        val expectedLiters = (distance / mileage).toFloat()

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, mileage, fuelType)

        // Assert
        assertEquals(expectedLiters, result.litersNeeded, 0.1f)
        assert(result.estimatedCost < 500) // Should be cheaper than car
    }

    @Test
    fun testFuelCostCalculation_Commercial_Diesel() = runBlocking {
        // Arrange
        val distance = 500
        val mileage = 8 // km/l for truck
        val fuelType = "diesel"
        val expectedLiters = (distance / mileage).toFloat()

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, mileage, fuelType)

        // Assert
        assertEquals(expectedLiters, result.litersNeeded, 0.1f)
        assertEquals(500, result.distanceKm)
    }

    @Test
    fun testFuelCostCalculation_ZeroDistance() = runBlocking {
        // Arrange
        val distance = 0
        val mileage = 15
        val fuelType = "petrol"

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, mileage, fuelType)

        // Assert
        assertEquals(0, result.distanceKm)
        assertEquals(0f, result.litersNeeded, 0.1f)
        assertEquals(0.0, result.estimatedCost, 0.1)
    }

    @Test
    fun testEVChargingCost() = runBlocking {
        // Arrange
        val distance = 100
        val fuelType = "ev"
        val expectedUnits = (distance / 5).toFloat() // 5 km per kWh

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, 15, fuelType)

        // Assert
        assertEquals(expectedUnits, result.litersNeeded, 1f)
    }

    @Test
    fun testCNGCost() = runBlocking {
        // Arrange
        val distance = 200
        val fuelType = "cng"
        val expectedKgs = (distance / 20).toFloat() // 20km per kg

        coEvery { mockFuelPriceDao.getLatestFuelPrice(any(), fuelType) } returns null

        // Act
        val result = fuelCostCalculator.calculateFuelCost(distance, 15, fuelType)

        // Assert
        assertEquals(expectedKgs, result.litersNeeded, 0.1f)
    }
}
