package com.imaniapp.uganda.presentation.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.imaniapp.uganda.domain.model.PrayerTime
import com.imaniapp.uganda.domain.model.PrayerTimeStatus
import com.imaniapp.uganda.domain.repository.PrayerTimeRepository
import com.imaniapp.uganda.presentation.utils.LocationHelper
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

data class PrayerTimesUiState(
    val isLoading: Boolean = false,
    val prayerTime: PrayerTime? = null,
    val prayerStatus: PrayerTimeStatus? = null,
    val error: String? = null,
    val locationPermissionGranted: Boolean = false,
    val currentLocation: String = "Unknown Location"
)

@HiltViewModel
class PrayerTimesViewModel @Inject constructor(
    private val prayerTimeRepository: PrayerTimeRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(PrayerTimesUiState())
    val uiState: StateFlow<PrayerTimesUiState> = _uiState.asStateFlow()
    
    init {
        checkLocationPermission()
    }
    
    private fun checkLocationPermission() {
        val hasPermission = locationHelper.hasLocationPermission()
        _uiState.value = _uiState.value.copy(locationPermissionGranted = hasPermission)
        
        if (hasPermission) {
            getCurrentLocationAndFetchPrayerTimes()
        }
    }
    
    fun onLocationPermissionGranted() {
        _uiState.value = _uiState.value.copy(locationPermissionGranted = true)
        getCurrentLocationAndFetchPrayerTimes()
    }
    
    private fun getCurrentLocationAndFetchPrayerTimes() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
            
            try {
                val location = locationHelper.getCurrentLocation()
                if (location != null) {
                    val locationName = "Lat: ${String.format("%.4f", location.latitude)}, " +
                            "Lng: ${String.format("%.4f", location.longitude)}"
                    
                    _uiState.value = _uiState.value.copy(currentLocation = locationName)
                    
                    // Fetch prayer times for current location
                    fetchPrayerTimes(location.latitude, location.longitude)
                } else {
                    // Fallback to Uganda default coordinates
                    _uiState.value = _uiState.value.copy(currentLocation = "Kampala, Uganda (Default)")
                    fetchPrayerTimes(
                        LocationHelper.UGANDA_DEFAULT_LATITUDE,
                        LocationHelper.UGANDA_DEFAULT_LONGITUDE
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Failed to get location: ${e.message}",
                    currentLocation = "Kampala, Uganda (Default)"
                )
                // Fallback to default location
                fetchPrayerTimes(
                    LocationHelper.UGANDA_DEFAULT_LATITUDE,
                    LocationHelper.UGANDA_DEFAULT_LONGITUDE
                )
            }
        }
    }
    
    private fun fetchPrayerTimes(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            try {
                val result = prayerTimeRepository.getPrayerTimes(latitude, longitude)
                result.fold(
                    onSuccess = { prayerTime ->
                        val prayerStatus = prayerTimeRepository.getCurrentPrayerStatus(prayerTime)
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            prayerTime = prayerTime,
                            prayerStatus = prayerStatus,
                            error = null
                        )
                    },
                    onFailure = { exception ->
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = "Error: ${exception.message}"
                        )
                    }
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = "Unexpected error: ${e.message}"
                )
            }
        }
    }
    
    fun updateLocation(latitude: Double, longitude: Double) {
        fetchPrayerTimes(latitude, longitude)
    }
    
    fun refreshPrayerTimes() {
        if (_uiState.value.locationPermissionGranted) {
            getCurrentLocationAndFetchPrayerTimes()
        } else {
            // Use default location if no permission
            fetchPrayerTimes(
                LocationHelper.UGANDA_DEFAULT_LATITUDE,
                LocationHelper.UGANDA_DEFAULT_LONGITUDE
            )
        }
    }
    
    fun startLocationUpdates() {
        if (!locationHelper.hasLocationPermission()) return
        
        viewModelScope.launch {
            locationHelper.getLocationUpdates()
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = "Location updates failed: ${exception.message}"
                    )
                }
                .collect { location ->
                    val locationName = "Lat: ${String.format("%.4f", location.latitude)}, " +
                            "Lng: ${String.format("%.4f", location.longitude)}"
                    
                    _uiState.value = _uiState.value.copy(currentLocation = locationName)
                    
                    // Update prayer times if location changed significantly
                    fetchPrayerTimes(location.latitude, location.longitude)
                }
        }
    }
    
    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    private fun getCurrentDateString(): String {
        val sdf = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
        return sdf.format(Date())
    }
} 