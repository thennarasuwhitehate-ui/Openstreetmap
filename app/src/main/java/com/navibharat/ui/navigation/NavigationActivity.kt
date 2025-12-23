package com.navibharat.ui.navigation

import android.Manifest
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.FrameLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.navibharat.R
import com.navibharat.data.location.LocationManager
import com.navibharat.maps.MapProvider
import com.navibharat.maps.MapProviderFactory
import com.navibharat.navigation.NavigationEngine
import com.navibharat.voice.VoiceNavigationManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject
import android.content.SharedPreferences

@AndroidEntryPoint
class NavigationActivity : AppCompatActivity() {

    @Inject
    lateinit var locationManager: LocationManager

    @Inject
    lateinit var navigationEngine: NavigationEngine

    @Inject
    lateinit var voiceNavigationManager: VoiceNavigationManager

    @Inject
    lateinit var sharedPreferences: SharedPreferences

    private lateinit var mapProvider: MapProvider
    private lateinit var mapProviderFactory: MapProviderFactory
    private var isMuted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation)

        Timber.i("NavigationActivity created")

        // Initialize map provider factory
        mapProviderFactory = MapProviderFactory(this, sharedPreferences)
        mapProvider = mapProviderFactory.createMapProvider()

        // Initialize map with API key
        mapProvider.initializeMap(this, "YOUR_GOOGLE_MAPS_API_KEY")

        // Add map to container
        val mapContainer = findViewById<FrameLayout>(R.id.map_container)
        mapContainer.addView(mapProvider.getMapView())

        // Setup bottom sheet
        setupBottomSheet()

        // Request necessary permissions
        requestNavigationPermissions()
    }

    private fun setupBottomSheet() {
        val bottomSheet = findViewById<BottomSheetBehavior<*>>(R.id.bottom_sheet)
        val instructionText = findViewById<TextView>(R.id.instruction_text)
        val distanceInfo = findViewById<TextView>(R.id.distance_info)
        val btnMute = findViewById<Button>(R.id.btn_mute)
        val btnDestination = findViewById<Button>(R.id.btn_destination)
        val btnMore = findViewById<Button>(R.id.btn_more)

        // Listen to navigation instructions
        lifecycleScope.launch {
            navigationEngine.navigationInstructions.collect { instruction ->
                instructionText.text = instruction?.instruction ?: "Loading..."
                distanceInfo.text = "${instruction?.distanceToTurn}m • ${instruction?.roadName ?: "Unknown"}"
            }
        }

        btnMute.setOnClickListener {
            toggleMute()
            btnMute.text = if (isMuted) "Unmute" else "Mute"
        }

        btnDestination.setOnClickListener {
            Timber.i("Change destination clicked")
            // In production: show destination picker
        }

        btnMore.setOnClickListener {
            Timber.i("More options clicked")
            // In production: show menu
        }
    }

    private fun toggleMute() {
        isMuted = !isMuted
        if (isMuted) {
            voiceNavigationManager.mute()
        } else {
            voiceNavigationManager.unmute()
        }
    }

    private fun requestNavigationPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val permissions = arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            )
            requestPermissions(permissions, 100)
        }
    }

    override fun onStart() {
        super.onStart()
        // Start location updates
        locationManager.startLocationUpdates(batteryOptimizationMode = false)

        // Listen to location updates
        lifecycleScope.launch {
            locationManager.locationFlow.collect { location ->
                navigationEngine.updateLocation(location)

                // Animate map to current location
                mapProvider.animateCamera(location.latitude, location.longitude, 17f)
            }
        }

        // Listen to navigation state updates
        lifecycleScope.launch {
            navigationEngine.navigationState.collect { state ->
                state.route?.let { route ->
                    // Draw route on map
                    mapProvider.drawRoute(
                        com.navibharat.maps.Route(
                            polyline = route.polyline,
                            distanceMeters = route.totalDistance,
                            durationSeconds = route.totalDuration,
                            steps = emptyList()
                        ),
                        android.graphics.Color.BLUE
                    )
                }
            }
        }
    }

    override fun onStop() {
        super.onStop()
        locationManager.stopLocationUpdates()
    }

    override fun onDestroy() {
        super.onDestroy()
        voiceNavigationManager.destroy()
        navigationEngine.endNavigation()
    }
}
