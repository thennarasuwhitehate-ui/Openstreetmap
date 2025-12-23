package com.navibharat.voice

import android.content.Context
import com.navibharat.data.repository.RouteRepository
import com.navibharat.data.repository.ServicesRepository
import com.navibharat.features.fuel.FuelCostCalculator
import com.navibharat.features.tolls.TollManager
import com.navibharat.navigation.NavigationEngine
import timber.log.Timber

/**
 * Executes voice commands based on recognized intents
 */
class VoiceCommandHandler(
    private val context: Context,
    private val navigationEngine: NavigationEngine,
    private val voiceNavigationManager: VoiceNavigationManager,
    private val routeRepository: RouteRepository,
    private val servicesRepository: ServicesRepository,
    private val fuelCalculator: FuelCostCalculator,
    private val tollManager: TollManager
) {

    private var isMuted = false

    /**
     * Handle a recognized voice command
     */
    suspend fun handleCommand(command: VoiceCommand) {
        if (command.confidence < 0.7f) {
            Timber.w("Low confidence command, ignoring: ${command.intent}")
            return
        }

        when (command.intent) {
            "navigate" -> {
                command.parameters.destination?.let {
                    handleNavigate(it)
                }
            }
            "avoid_tolls" -> handleAvoidTolls()
            "show_fuel_cost" -> handleShowFuelCost()
            "nearby_petrol" -> handleNearbyPetrol()
            "nearby_ev" -> handleNearbyEV()
            "nearby_mechanic" -> handleNearbyMechanic()
            "nearby_rest" -> handleNearbyRest()
            "mute" -> handleMute()
            "unmute" -> handleUnmute()
            "language" -> {
                command.parameters.languageCode?.let {
                    handleLanguageSwitch(it)
                }
            }
            "reroute" -> handleReroute()
            "repeat" -> handleRepeat()
            "stop" -> handleStopNavigation()
            "check_toll" -> handleCheckToll()
            "speed_limit" -> handleSpeedLimit()
            "help" -> handleHelp()
            else -> {
                Timber.w("Unknown intent: ${command.intent}")
            }
        }
    }

    private suspend fun handleNavigate(destination: String) {
        try {
            Timber.i("Navigating to: $destination")
            // In production: calculate route from current location to destination
            voiceNavigationManager.speak("Routing to $destination")
        } catch (e: Exception) {
            Timber.e(e, "Error handling navigate command")
            voiceNavigationManager.speak("Could not route to that location")
        }
    }

    private suspend fun handleAvoidTolls() {
        try {
            Timber.i("Recalculating route avoiding tolls")
            voiceNavigationManager.speak("Recalculating route without tolls")
        } catch (e: Exception) {
            Timber.e(e, "Error handling avoid tolls")
        }
    }

    private suspend fun handleShowFuelCost() {
        try {
            // In production: calculate fuel cost based on current route
            voiceNavigationManager.speak("Estimated fuel cost for this route is two hundred rupees")
        } catch (e: Exception) {
            Timber.e(e, "Error showing fuel cost")
        }
    }

    private suspend fun handleNearbyPetrol() {
        try {
            Timber.i("Finding nearby petrol stations")
            voiceNavigationManager.speak("Finding petrol stations nearby")
        } catch (e: Exception) {
            Timber.e(e, "Error finding nearby petrol")
        }
    }

    private suspend fun handleNearbyEV() {
        try {
            Timber.i("Finding nearby EV chargers")
            voiceNavigationManager.speak("Finding EV chargers nearby")
        } catch (e: Exception) {
            Timber.e(e, "Error finding nearby EV chargers")
        }
    }

    private suspend fun handleNearbyMechanic() {
        try {
            Timber.i("Finding nearby mechanics")
            voiceNavigationManager.speak("Finding mechanics nearby")
        } catch (e: Exception) {
            Timber.e(e, "Error finding nearby mechanics")
        }
    }

    private suspend fun handleNearbyRest() {
        try {
            Timber.i("Finding nearby rest areas")
            voiceNavigationManager.speak("Finding rest areas on or near your route")
        } catch (e: Exception) {
            Timber.e(e, "Error finding nearby rest areas")
        }
    }

    private fun handleMute() {
        isMuted = true
        Timber.i("Navigation voice muted")
        voiceNavigationManager.mute()
    }

    private fun handleUnmute() {
        isMuted = false
        Timber.i("Navigation voice unmuted")
        voiceNavigationManager.unmute()
    }

    private fun handleLanguageSwitch(languageCode: String) {
        try {
            Timber.i("Switching language to: $languageCode")
            voiceNavigationManager.setLanguage(languageCode)
            voiceNavigationManager.speak("Language switched")
        } catch (e: Exception) {
            Timber.e(e, "Error switching language")
        }
    }

    private suspend fun handleReroute() {
        try {
            Timber.i("Finding faster route")
            voiceNavigationManager.speak("Finding faster route")
        } catch (e: Exception) {
            Timber.e(e, "Error finding faster route")
        }
    }

    private fun handleRepeat() {
        Timber.i("Repeating last instruction")
        voiceNavigationManager.repeatLastInstruction()
    }

    private fun handleStopNavigation() {
        Timber.i("Stopping navigation")
        navigationEngine.endNavigation()
        voiceNavigationManager.speak("Navigation ended")
    }

    private suspend fun handleCheckToll() {
        try {
            Timber.i("Checking remaining toll cost")
            voiceNavigationManager.speak("Checking remaining toll cost")
        } catch (e: Exception) {
            Timber.e(e, "Error checking toll")
        }
    }

    private fun handleSpeedLimit() {
        try {
            Timber.i("Checking speed limit")
            voiceNavigationManager.speak("Current speed limit is 60 kilometers per hour")
        } catch (e: Exception) {
            Timber.e(e, "Error checking speed limit")
        }
    }

    private fun handleHelp() {
        voiceNavigationManager.speak("Try saying navigate to a place, avoid tolls, show fuel cost, find petrol, or stop navigation")
    }
}
