package com.navibharat.analytics

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics
import timber.log.Timber

class FirebaseAnalyticsWrapper(context: Context) {

    private val firebaseAnalytics = FirebaseAnalytics.getInstance(context)

    /**
     * Log navigation started event
     */
    fun logNavigationStarted(
        origin: String,
        destination: String,
        vehicleType: String,
        distance: Int,
        duration: Int
    ) {
        val bundle = Bundle().apply {
            putString("origin", origin)
            putString("destination", destination)
            putString("vehicle_type", vehicleType)
            putInt("distance_meters", distance)
            putInt("duration_seconds", duration)
        }
        firebaseAnalytics.logEvent("navigation_started", bundle)
        Timber.i("Event logged: navigation_started")
    }

    /**
     * Log navigation ended event
     */
    fun logNavigationEnded(
        distance: Int,
        duration: Int,
        fuelCost: Double,
        tollCost: Double
    ) {
        val bundle = Bundle().apply {
            putInt("distance_meters", distance)
            putInt("duration_seconds", duration)
            putDouble("fuel_cost", fuelCost)
            putDouble("toll_cost", tollCost)
        }
        firebaseAnalytics.logEvent("navigation_ended", bundle)
        Timber.i("Event logged: navigation_ended")
    }

    /**
     * Log route calculation event
     */
    fun logRouteCalculated(
        provider: String,
        distance: Int,
        duration: Int,
        tollCost: Double?
    ) {
        val bundle = Bundle().apply {
            putString("provider", provider)
            putInt("distance_meters", distance)
            putInt("duration_seconds", duration)
            if (tollCost != null) {
                putDouble("toll_cost", tollCost)
            }
        }
        firebaseAnalytics.logEvent("route_calculated", bundle)
        Timber.i("Event logged: route_calculated")
    }

    /**
     * Log voice command event
     */
    fun logVoiceCommand(
        command: String,
        intent: String,
        success: Boolean
    ) {
        val bundle = Bundle().apply {
            putString("command", command)
            putString("intent", intent)
            putBoolean("success", success)
        }
        firebaseAnalytics.logEvent("voice_command", bundle)
        Timber.i("Event logged: voice_command - $command")
    }

    /**
     * Log feature usage event
     */
    fun logFeatureUsed(featureName: String) {
        val bundle = Bundle().apply {
            putString("feature_name", featureName)
        }
        firebaseAnalytics.logEvent("feature_used", bundle)
        Timber.i("Event logged: feature_used - $featureName")
    }

    /**
     * Log crash event
     */
    fun logCrash(errorMessage: String, stackTrace: String) {
        val bundle = Bundle().apply {
            putString("error_message", errorMessage)
            putString("stack_trace", stackTrace)
        }
        firebaseAnalytics.logEvent("app_crash", bundle)
        Timber.e("Event logged: app_crash - $errorMessage")
    }

    /**
     * Log settings change
     */
    fun logSettingChanged(settingName: String, newValue: String) {
        val bundle = Bundle().apply {
            putString("setting_name", settingName)
            putString("new_value", newValue)
        }
        firebaseAnalytics.logEvent("setting_changed", bundle)
        Timber.i("Event logged: setting_changed - $settingName")
    }

    /**
     * Log user property
     */
    fun setUserProperty(name: String, value: String) {
        firebaseAnalytics.setUserProperty(name, value)
        Timber.i("User property set: $name = $value")
    }

    /**
     * Log app version
     */
    fun setAppVersion(versionName: String) {
        setUserProperty("app_version", versionName)
    }

    /**
     * Log vehicle type
     */
    fun setVehicleType(vehicleType: String) {
        setUserProperty("vehicle_type", vehicleType)
    }

    /**
     * Log preferred language
     */
    fun setPreferredLanguage(language: String) {
        setUserProperty("preferred_language", language)
    }
}
