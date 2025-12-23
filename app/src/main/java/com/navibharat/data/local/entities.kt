package com.navibharat.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val userId: String,
    val email: String?,
    val phone: String?,
    val name: String,
    val vehicleType: String = "car",
    val fuelType: String = "petrol",
    val vehicleMileage: Float = 15f,
    val preferredLanguage: String = "en",
    val defaultMapProvider: String = "google_maps",
    val darkModeEnabled: Boolean = true,
    val voiceAssistantEnabled: Boolean = true,
    val avatarUrl: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "routes")
data class RouteEntity(
    @PrimaryKey val routeId: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val polylineEncoded: String,
    val totalDistance: Long, // meters
    val totalDuration: Long, // seconds
    val vehicleType: String = "car",
    val tollCost: Double? = null,
    val fuelCost: Double? = null,
    val alternativeRoutes: String? = null,
    val timestamp: Long = System.currentTimeMillis(),
    val createdBy: String? = null
)

@Entity(tableName = "trips")
data class TripEntity(
    @PrimaryKey val localTripId: String,
    val remoteTripId: String? = null,
    val userId: String,
    val originLat: Double,
    val originLng: Double,
    val destLat: Double,
    val destLng: Double,
    val originAddress: String,
    val destinationAddress: String,
    val startTime: Long,
    val endTime: Long? = null,
    val distanceM: Int = 0,
    val durationSec: Int = 0,
    val fuelCost: Float = 0f,
    val tollCost: Float = 0f,
    val avgSpeed: Float = 0f,
    val polyline: String? = null,
    val vehicleType: String = "car",
    val fuelType: String = "petrol",
    val isSynced: Boolean = false,
    val syncedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "favorite_routes")
data class FavoriteRouteEntity(
    @PrimaryKey val favoriteId: String,
    val userId: String,
    val name: String,
    val originLat: Double,
    val originLng: Double,
    val originAddress: String,
    val destLat: Double,
    val destLng: Double,
    val destAddress: String,
    val polyline: String? = null,
    val totalDistance: Int = 0,
    val estimatedDuration: Int = 0,
    val savedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "recent_searches")
data class RecentSearchEntity(
    @PrimaryKey val searchId: String,
    val userId: String,
    val searchType: String, // "destination", "waypoint", "service"
    val address: String,
    val lat: Double,
    val lng: Double,
    val resultType: String = "address",
    val searchCount: Int = 1,
    val lastSearchedAt: Long = System.currentTimeMillis()
)

@Entity(tableName = "road_hazards")
data class RoadHazardEntity(
    @PrimaryKey val hazardId: String,
    val lat: Double,
    val lng: Double,
    val type: String, // "speed_breaker", "narrow_road", "diversion", "accident"
    val severity: String = "low",
    val description: String,
    val reportedBy: String? = null,
    val reportedTime: Long = System.currentTimeMillis(),
    val expiryTime: Long? = null
)

@Entity(tableName = "toll_plazas")
data class TollPlazaEntity(
    @PrimaryKey val tollId: String,
    val name: String,
    val lat: Double,
    val lng: Double,
    val highway: String,
    val rates: Map<String, Double>, // "car" -> 100.0
    val operationalHours: String? = null,
    val lastUpdated: Long = System.currentTimeMillis()
)

@Entity(tableName = "fuel_prices")
data class FuelPriceEntity(
    @PrimaryKey val priceId: String,
    val state: String,
    val fuelType: String, // "petrol", "diesel", "cng", "ev"
    val pricePerUnit: Double,
    val unit: String = "liter", // "liter" or "kg" for CNG
    val timestamp: Long = System.currentTimeMillis()
)
