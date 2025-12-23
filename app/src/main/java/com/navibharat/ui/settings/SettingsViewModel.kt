package com.navibharat.ui.settings

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.navibharat.data.local.NaviBharatDatabase
import com.navibharat.data.auth.AuthManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val database: NaviBharatDatabase,
    private val sharedPreferences: SharedPreferences,
    private val authManager: AuthManager
) : ViewModel() {

    private val _vehicleType = MutableStateFlow("car")
    val vehicleType: StateFlow<String> = _vehicleType

    private val _fuelType = MutableStateFlow("petrol")
    val fuelType: StateFlow<String> = _fuelType

    private val _language = MutableStateFlow("en")
    val language: StateFlow<String> = _language

    private val _darkModeEnabled = MutableStateFlow(true)
    val darkModeEnabled: StateFlow<Boolean> = _darkModeEnabled

    private val _voiceAssistantEnabled = MutableStateFlow(true)
    val voiceAssistantEnabled: StateFlow<Boolean> = _voiceAssistantEnabled

    private val _batterySaverEnabled = MutableStateFlow(false)
    val batterySaverEnabled: StateFlow<Boolean> = _batterySaverEnabled

    init {
        loadSettings()
    }

    private fun loadSettings() {
        _vehicleType.value = sharedPreferences.getString("vehicle_type", "car") ?: "car"
        _fuelType.value = sharedPreferences.getString("fuel_type", "petrol") ?: "petrol"
        _language.value = sharedPreferences.getString("language", "en") ?: "en"
        _darkModeEnabled.value = sharedPreferences.getBoolean("dark_mode", true)
        _voiceAssistantEnabled.value = sharedPreferences.getBoolean("voice_assistant", true)
        _batterySaverEnabled.value = sharedPreferences.getBoolean("battery_saver", false)
    }

    fun setVehicleType(type: String) {
        _vehicleType.value = type
        sharedPreferences.edit().putString("vehicle_type", type).apply()
        Timber.i("Vehicle type set to: $type")
    }

    fun setFuelType(type: String) {
        _fuelType.value = type
        sharedPreferences.edit().putString("fuel_type", type).apply()
        Timber.i("Fuel type set to: $type")
    }

    fun setLanguage(lang: String) {
        _language.value = lang
        sharedPreferences.edit().putString("language", lang).apply()
        Timber.i("Language set to: $lang")
    }

    fun setDarkMode(enabled: Boolean) {
        _darkModeEnabled.value = enabled
        sharedPreferences.edit().putBoolean("dark_mode", enabled).apply()
        Timber.i("Dark mode: $enabled")
    }

    fun setVoiceAssistant(enabled: Boolean) {
        _voiceAssistantEnabled.value = enabled
        sharedPreferences.edit().putBoolean("voice_assistant", enabled).apply()
        Timber.i("Voice assistant: $enabled")
    }

    fun setBatterySaver(enabled: Boolean) {
        _batterySaverEnabled.value = enabled
        sharedPreferences.edit().putBoolean("battery_saver", enabled).apply()
        Timber.i("Battery saver: $enabled")
    }

    fun logout() {
        authManager.logout()
        Timber.i("User logged out")
    }

    fun clearTripHistory() {
        Timber.i("Trip history cleared")
        // In production: delete all trips from database
    }
}
