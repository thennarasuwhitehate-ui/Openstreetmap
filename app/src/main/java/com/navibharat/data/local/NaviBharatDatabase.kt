package com.navibharat.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters

@Database(
    entities = [
        UserEntity::class,
        RouteEntity::class,
        TripEntity::class,
        FavoriteRouteEntity::class,
        RecentSearchEntity::class,
        RoadHazardEntity::class,
        TollPlazaEntity::class,
        FuelPriceEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class NaviBharatDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun routeDao(): RouteDao
    abstract fun tripDao(): TripDao
    abstract fun favoriteRouteDao(): FavoriteRouteDao
    abstract fun recentSearchDao(): RecentSearchDao
    abstract fun roadHazardDao(): RoadHazardDao
    abstract fun tollPlazaDao(): TollPlazaDao
    abstract fun fuelPriceDao(): FuelPriceDao
}
