package com.navibharat.di

import android.content.Context
import com.navibharat.data.repository.RouteRepository
import com.navibharat.data.repository.ServicesRepository
import com.navibharat.data.repository.VoiceCommandRepository
import com.navibharat.data.local.NaviBharatDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Singleton
    @Provides
    fun provideRouteRepository(
        database: NaviBharatDatabase
    ): RouteRepository {
        return RouteRepository(
            database = database,
            apiKey = "YOUR_GOOGLE_MAPS_API_KEY" // Load from BuildConfig in production
        )
    }

    @Singleton
    @Provides
    fun provideServicesRepository(): ServicesRepository {
        return ServicesRepository(
            apiKey = "YOUR_GOOGLE_PLACES_API_KEY"
        )
    }

    @Singleton
    @Provides
    fun provideVoiceCommandRepository(): VoiceCommandRepository {
        return VoiceCommandRepository(
            geminiApiKey = "YOUR_GEMINI_API_KEY"
        )
    }
}
