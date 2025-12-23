package com.navibharat.di

import android.content.Context
import android.content.SharedPreferences
import com.navibharat.analytics.FirebaseAnalyticsWrapper
import com.navibharat.data.local.NaviBharatDatabase
import com.navibharat.features.fuel.FuelCostCalculator
import com.navibharat.features.services.NearbyServicesManager
import com.navibharat.features.tolls.TollManager
import com.navibharat.features.alerts.RoadHazardManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Singleton
    @Provides
    fun provideTollManager(
        database: NaviBharatDatabase
    ): TollManager {
        return TollManager(database.tollPlazaDao())
    }

    @Singleton
    @Provides
    fun provideFuelCostCalculator(
        database: NaviBharatDatabase
    ): FuelCostCalculator {
        return FuelCostCalculator(database.fuelPriceDao())
    }

    @Singleton
    @Provides
    fun provideNearbyServicesManager(): NearbyServicesManager {
        return NearbyServicesManager()
    }

    @Singleton
    @Provides
    fun provideRoadHazardManager(
        database: NaviBharatDatabase
    ): RoadHazardManager {
        return RoadHazardManager(database.roadHazardDao())
    }
}
