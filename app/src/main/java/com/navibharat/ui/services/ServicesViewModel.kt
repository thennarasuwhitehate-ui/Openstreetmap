package com.navibharat.ui.services

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.navibharat.data.repository.ServicesRepository
import com.navibharat.features.services.NearbyService
import com.navibharat.features.services.ServiceType
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class ServicesViewModel @Inject constructor(
    private val servicesRepository: ServicesRepository
) : ViewModel() {

    private val _services = MutableStateFlow<List<NearbyService>>(emptyList())
    val services: StateFlow<List<NearbyService>> = _services

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var lastUserLat = 28.7041 // Default: Delhi
    private var lastUserLng = 77.1025

    fun setUserLocation(latitude: Double, longitude: Double) {
        lastUserLat = latitude
        lastUserLng = longitude
    }

    fun searchServices(serviceType: ServiceType) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = servicesRepository.searchNearbyServices(
                    latitude = lastUserLat,
                    longitude = lastUserLng,
                    serviceType = serviceType
                )

                if (result.isSuccess) {
                    _services.value = result.getOrNull() ?: emptyList()
                    Timber.i("Found ${_services.value.size} services")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                    Timber.e(result.exceptionOrNull(), "Error searching services")
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in searchServices")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun searchMultipleServices() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _errorMessage.value = null

                val result = servicesRepository.searchMultipleServices(
                    latitude = lastUserLat,
                    longitude = lastUserLng
                )

                if (result.isSuccess) {
                    val allServices = result.getOrNull()?.values?.flatten() ?: emptyList()
                    _services.value = allServices
                    Timber.i("Found ${allServices.size} services across all types")
                } else {
                    _errorMessage.value = result.exceptionOrNull()?.message
                }
            } catch (e: Exception) {
                Timber.e(e, "Error in searchMultipleServices")
                _errorMessage.value = e.message
            } finally {
                _isLoading.value = false
            }
        }
    }
}
