package com.navibharat.data.network

import okhttp3.Interceptor
import okhttp3.Response
import timber.log.Timber

/**
 * Interceptor for adding API keys and custom headers
 */
class ApiKeyInterceptor(
    private val googleMapsKey: String,
    private val googlePlacesKey: String,
    private val geminiKey: String
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val url = originalRequest.url

        val newUrl = when {
            url.host.contains("maps.googleapis.com") -> {
                url.newBuilder()
                    .addQueryParameter("key", googleMapsKey)
                    .build()
            }
            url.host.contains("places.googleapis.com") -> {
                originalRequest.newBuilder()
                    .header("Authorization", "Bearer $googlePlacesKey")
                    .build()
                url
            }
            url.host.contains("generativelanguage.googleapis.com") -> {
                url.newBuilder()
                    .addQueryParameter("key", geminiKey)
                    .build()
            }
            else -> url
        }

        val request = originalRequest.newBuilder()
            .url(newUrl)
            .addHeader("User-Agent", "NaviBharat/1.0.0")
            .build()

        val startTime = System.currentTimeMillis()

        return try {
            val response = chain.proceed(request)
            val duration = System.currentTimeMillis() - startTime

            Timber.d("${response.code} ${originalRequest.url} (${duration}ms)")

            if (!response.isSuccessful) {
                Timber.w("API Error: ${response.code} ${response.message}")
            }

            response
        } catch (e: Exception) {
            Timber.e(e, "Network error: ${originalRequest.url}")
            throw e
        }
    }
}

/**
 * Interceptor for handling network errors and retries
 */
class ErrorHandlingInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        return try {
            val response = chain.proceed(request)

            when (response.code) {
                400 -> Timber.e("Bad Request: ${response.message}")
                401 -> Timber.e("Unauthorized: Invalid API key")
                403 -> Timber.e("Forbidden: Access denied")
                404 -> Timber.e("Not Found: ${response.message}")
                429 -> Timber.e("Rate Limited: Too many requests")
                500 -> Timber.e("Server Error: ${response.message}")
                503 -> Timber.e("Service Unavailable")
            }

            response
        } catch (e: Exception) {
            Timber.e(e, "Network request failed")
            throw e
        }
    }
}
