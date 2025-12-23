package com.navibharat.features.fuel

import com.navibharat.data.local.FuelPriceDao
import dagger.hilt.android.scopes.ActivityScoped
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

data class FuelCostEstimate(
    val fuelType: String,
    val distanceKm: Int,
    val litersNeeded: Float,
    val pricePerLiter: Double,
    val estimatedCost: Double,
    val mileageUsed: Int
)

@ActivityScoped
class FuelCostCalculator @Inject constructor(
    private val fuelPriceDao: FuelPriceDao
) {

    // Default mileage (km/l) for different vehicle types
    private val defaultMileage = mapOf(
        "car" to 15,
        "bike" to 40,
        "commercial" to 10,
        "auto" to 12
    )

    // Default fuel prices (in INR per liter) - for offline fallback
    private val defaultFuelPrices = mapOf(
        "petrol" to 96.0,
        "diesel" to 89.0,
        "cng" to 75.0, // per kg
        "ev" to 4.0   // per km (charging cost)
    )

    /**
     * Calculate fuel cost for a trip
     */
    suspend fun calculateFuelCost(
        distanceKm: Int,
        mileageKmPerLiter: Int? = null,
        fuelType: String = "petrol",
        state: String = "Karnataka"
    ): FuelCostEstimate {
        try {
            val mileage = mileageKmPerLiter ?: defaultMileage["car"] ?: 15

            // Get current fuel price
            val pricePerLiter = getFuelPrice(fuelType, state)

            // Calculate liters/kg needed
            val unitsNeeded = when (fuelType) {
                "cng" -> distanceKm / 20f // CNG: ~20km per kg
                "ev" -> distanceKm / 5f  // EV: 5km per unit (kWh)
                else -> distanceKm / mileage.toFloat()
            }

            // Calculate total cost
            val estimatedCost = unitsNeeded * pricePerLiter

            return FuelCostEstimate(
                fuelType = fuelType,
                distanceKm = distanceKm,
                litersNeeded = unitsNeeded,
                pricePerLiter = pricePerLiter,
                estimatedCost = estimatedCost,
                mileageUsed = mileage
            )
        } catch (e: Exception) {
            Timber.e(e, "Error calculating fuel cost")
            return FuelCostEstimate(
                fuelType = fuelType,
                distanceKm = distanceKm,
                litersNeeded = 0f,
                pricePerLiter = 0.0,
                estimatedCost = 0.0,
                mileageUsed = 0
            )
        }
    }

    /**
     * Get current fuel price for a fuel type and state
     */
    private suspend fun getFuelPrice(fuelType: String, state: String): Double {
        return try {
            val latestPrice = fuelPriceDao.getLatestFuelPrice(state, fuelType)
            latestPrice?.pricePerUnit ?: defaultFuelPrices[fuelType] ?: 80.0
        } catch (e: Exception) {
            Timber.w("Error fetching fuel price, using default: ${e.message}")
            defaultFuelPrices[fuelType] ?: 80.0
        }
    }

    /**
     * Compare fuel costs between different vehicle types for same distance
     */
    suspend fun compareFuelCosts(
        distanceKm: Int,
        vehicleTypes: List<String> = listOf("car", "bike", "auto")
    ): Map<String, FuelCostEstimate> {
        return vehicleTypes.associate { vehicleType ->
            val mileage = defaultMileage[vehicleType] ?: 15
            val fuelType = when (vehicleType) {
                "bike" -> "petrol"
                "auto" -> "petrol"
                else -> "petrol"
            }
            vehicleType to calculateFuelCost(distanceKm, mileage, fuelType)
        }
    }

    /**
     * Calculate cost difference between fuel types
     */
    suspend fun compareFuelTypes(
        distanceKm: Int,
        vehicleType: String = "car"
    ): Map<String, FuelCostEstimate> {
        val mileage = defaultMileage[vehicleType] ?: 15
        val fuelTypes = listOf("petrol", "diesel", "cng", "ev")

        return fuelTypes.associate { fuelType ->
            fuelType to calculateFuelCost(distanceKm, mileage, fuelType)
        }
    }

    /**
     * Get fuel price trend
     */
    fun getFuelPriceTrend(state: String, fuelType: String): Flow<List<Double>> {
        return fuelPriceDao.getFuelPricesByState(state)
            .let { flow ->
                // Map to list of prices - in production, implement proper transformation
                flow
            }
    }

    /**
     * Get all fuel prices for a state
     */
    fun getFuelPricesByState(state: String): Flow<Map<String, Double>> {
        return fuelPriceDao.getFuelPricesByState(state)
            .let { flow ->
                // Map to state-by-fuelType - in production, implement proper transformation
                flow
            }
    }

    /**
     * Get eco-friendly comparison
     */
    suspend fun getEcoFriendlyComparison(distanceKm: Int): Map<String, Any> {
        val petrolCost = calculateFuelCost(distanceKm, fuelType = "petrol")
        val evCost = calculateFuelCost(distanceKm, fuelType = "ev")
        val cnCost = calculateFuelCost(distanceKm, fuelType = "cng")

        return mapOf(
            "petrol" to petrolCost,
            "ev" to evCost,
            "cng" to cnCost,
            "savings_ev" to (petrolCost.estimatedCost - evCost.estimatedCost),
            "savings_cng" to (petrolCost.estimatedCost - cnCost.estimatedCost),
            "best_option" to when {
                evCost.estimatedCost < cnCost.estimatedCost && evCost.estimatedCost < petrolCost.estimatedCost -> "EV"
                cnCost.estimatedCost < petrolCost.estimatedCost -> "CNG"
                else -> "Petrol"
            }
        )
    }
}
