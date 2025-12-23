package com.navibharat.data.network

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import timber.log.Timber
import java.util.concurrent.TimeUnit

object ApiClient {

    private const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/"
    private const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/"
    private const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
    private const val MAPMY_INDIA_BASE_URL = "https://api.mapmyindia.com/"

    private fun createOkHttpClient(): OkHttpClient {
        val loggingInterceptor = HttpLoggingInterceptor { message ->
            Timber.d(message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .build()
    }

    private fun createRetrofit(baseUrl: String): Retrofit {
        val gson = GsonBuilder()
            .setLenient()
            .create()

        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .client(createOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
    }

    fun getGoogleMapsService(): GoogleMapsAPI {
        return createRetrofit(GOOGLE_MAPS_BASE_URL).create(GoogleMapsAPI::class.java)
    }

    fun getGooglePlacesService(): GooglePlacesAPI {
        return createRetrofit(GOOGLE_PLACES_BASE_URL).create(GooglePlacesAPI::class.java)
    }

    fun getGeminiService(): GeminiAPI {
        return createRetrofit(GEMINI_BASE_URL).create(GeminiAPI::class.java)
    }

    fun getMapMyIndiaService(): MapMyIndiaAPI {
        return createRetrofit(MAPMY_INDIA_BASE_URL).create(MapMyIndiaAPI::class.java)
    }
}
