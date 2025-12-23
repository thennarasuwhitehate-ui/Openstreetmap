package com.navibharat.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM users WHERE userId = :userId")
    suspend fun getUserById(userId: String): UserEntity?

    @Query("SELECT * FROM users LIMIT 1")
    fun getCurrentUser(): Flow<UserEntity?>

    @Update
    suspend fun updateUser(user: UserEntity)

    @Delete
    suspend fun deleteUser(user: UserEntity)
}

@Dao
interface RouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoute(route: RouteEntity)

    @Query("SELECT * FROM routes WHERE routeId = :routeId")
    suspend fun getRouteById(routeId: String): RouteEntity?

    @Query("SELECT * FROM routes ORDER BY timestamp DESC LIMIT 10")
    fun getRecentRoutes(): Flow<List<RouteEntity>>

    @Update
    suspend fun updateRoute(route: RouteEntity)

    @Delete
    suspend fun deleteRoute(route: RouteEntity)

    @Query("DELETE FROM routes WHERE timestamp < :cutoffTime")
    suspend fun deleteOldRoutes(cutoffTime: Long)
}

@Dao
interface TripDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTrip(trip: TripEntity)

    @Query("SELECT * FROM trips WHERE localTripId = :tripId")
    suspend fun getTripById(tripId: String): TripEntity?

    @Query("SELECT * FROM trips WHERE isSynced = 0 ORDER BY createdAt")
    suspend fun getUnsyncedTrips(): List<TripEntity>

    @Query("SELECT * FROM trips WHERE userId = :userId ORDER BY startTime DESC LIMIT 50")
    fun getTripHistory(userId: String): Flow<List<TripEntity>>

    @Update
    suspend fun updateTrip(trip: TripEntity)

    @Delete
    suspend fun deleteTrip(trip: TripEntity)

    @Query("DELETE FROM trips WHERE createdAt < :cutoffTime")
    suspend fun deleteOldTrips(cutoffTime: Long)
}

@Dao
interface FavoriteRouteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavoriteRoute(route: FavoriteRouteEntity)

    @Query("SELECT * FROM favorite_routes WHERE userId = :userId ORDER BY savedAt DESC")
    fun getFavoriteRoutes(userId: String): Flow<List<FavoriteRouteEntity>>

    @Query("SELECT * FROM favorite_routes WHERE favoriteId = :favoriteId")
    suspend fun getFavoriteRoute(favoriteId: String): FavoriteRouteEntity?

    @Update
    suspend fun updateFavoriteRoute(route: FavoriteRouteEntity)

    @Delete
    suspend fun deleteFavoriteRoute(route: FavoriteRouteEntity)
}

@Dao
interface RecentSearchDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecentSearch(search: RecentSearchEntity)

    @Query("SELECT * FROM recent_searches WHERE userId = :userId ORDER BY lastSearchedAt DESC LIMIT 20")
    fun getRecentSearches(userId: String): Flow<List<RecentSearchEntity>>

    @Delete
    suspend fun deleteRecentSearch(search: RecentSearchEntity)

    @Query("DELETE FROM recent_searches WHERE lastSearchedAt < :cutoffTime")
    suspend fun deleteOldSearches(cutoffTime: Long)
}

@Dao
interface RoadHazardDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHazard(hazard: RoadHazardEntity)

    @Query("SELECT * FROM road_hazards WHERE lat BETWEEN :minLat AND :maxLat AND lng BETWEEN :minLng AND :maxLng")
    suspend fun getHazardsInArea(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<RoadHazardEntity>

    @Query("SELECT * FROM road_hazards WHERE expiryTime IS NULL OR expiryTime > :currentTime")
    fun getActiveHazards(currentTime: Long): Flow<List<RoadHazardEntity>>

    @Update
    suspend fun updateHazard(hazard: RoadHazardEntity)

    @Delete
    suspend fun deleteHazard(hazard: RoadHazardEntity)

    @Query("DELETE FROM road_hazards WHERE expiryTime < :currentTime AND expiryTime IS NOT NULL")
    suspend fun deleteExpiredHazards(currentTime: Long)
}

@Dao
interface TollPlazaDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTollPlaza(plaza: TollPlazaEntity)

    @Query("SELECT * FROM toll_plazas WHERE lat BETWEEN :minLat AND :maxLat AND lng BETWEEN :minLng AND :maxLng")
    suspend fun getTollPlazasInArea(minLat: Double, maxLat: Double, minLng: Double, maxLng: Double): List<TollPlazaEntity>

    @Query("SELECT * FROM toll_plazas ORDER BY lastUpdated DESC")
    fun getAllTollPlazas(): Flow<List<TollPlazaEntity>>

    @Update
    suspend fun updateTollPlaza(plaza: TollPlazaEntity)

    @Delete
    suspend fun deleteTollPlaza(plaza: TollPlazaEntity)
}

@Dao
interface FuelPriceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFuelPrice(price: FuelPriceEntity)

    @Query("SELECT * FROM fuel_prices WHERE state = :state AND fuelType = :fuelType ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLatestFuelPrice(state: String, fuelType: String): FuelPriceEntity?

    @Query("SELECT * FROM fuel_prices WHERE state = :state")
    fun getFuelPricesByState(state: String): Flow<List<FuelPriceEntity>>

    @Update
    suspend fun updateFuelPrice(price: FuelPriceEntity)

    @Delete
    suspend fun deleteFuelPrice(price: FuelPriceEntity)
}
