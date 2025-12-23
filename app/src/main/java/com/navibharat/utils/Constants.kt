package com.navibharat.utils

object Constants {

    // API Configuration
    object API {
        const val GOOGLE_MAPS_BASE_URL = "https://maps.googleapis.com/"
        const val GOOGLE_PLACES_BASE_URL = "https://places.googleapis.com/"
        const val GEMINI_BASE_URL = "https://generativelanguage.googleapis.com/"
        const val MAPMY_INDIA_BASE_URL = "https://api.mapmyindia.com/"

        // Timeout in seconds
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
    }

    // Location Configuration
    object Location {
        const val GPS_UPDATE_INTERVAL_MS = 1000L
        const val GPS_MIN_DISTANCE_M = 5f
        const val GPS_ACCURACY_THRESHOLD_M = 20f
        const val LOCATION_UPDATE_BATTERY_SAVER_MS = 5000L
    }

    // Navigation Configuration
    object Navigation {
        const val TURN_INSTRUCTION_DISTANCE_M = 100
        const val OFF_ROUTE_TOLERANCE_M = 50
        const val OFF_ROUTE_TIME_THRESHOLD_S = 30
        const val ALTERNATIVE_ROUTES_LIMIT = 3
    }

    // Voice Configuration
    object Voice {
        const val STT_TIMEOUT_MS = 8000L
        const val WAKE_WORD = "Hey Navi"
        const val WAKE_WORD_ALTERNATIVE = "Navi"
        const val VOICE_COMMAND_DEBOUNCE_MS = 2000L
        const val CRITICAL_MOMENT_DURATION_S = 15
    }

    // Services Configuration
    object Services {
        const val NEARBY_SEARCH_RADIUS_M = 5000
        const val NEARBY_SEARCH_LIMIT = 10
        const val SERVICE_CACHE_DURATION_MS = 30 * 60 * 1000L // 30 minutes
    }

    // Sync Configuration
    object Sync {
        const val SYNC_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
        const val DATA_CACHE_EXPIRY_DAYS = 30
    }

    // UI Configuration
    object UI {
        const val ANIMATION_DURATION_MS = 300
        const val BOTTOM_SHEET_PEEK_HEIGHT_DP = 120
        const val MAP_ZOOM_LEVEL_NAVIGATION = 18f
        const val MAP_ZOOM_LEVEL_OVERVIEW = 15f
    }

    // Feature Flags
    object Features {
        const val ENABLE_OFFLINE_MAPS = true
        const val ENABLE_VOICE_ASSISTANT = true
        const val ENABLE_CROWDSOURCED_HAZARDS = true
        const val ENABLE_ALTERNATIVE_ROUTES = true
        const val ENABLE_TOLL_CALCULATION = true
        const val ENABLE_FUEL_ESTIMATION = true
    }

    // Error Messages
    object Errors {
        const val NO_ROUTE_FOUND = "Unable to calculate route. Please check destination."
        const val NETWORK_ERROR = "Network error. Please check your connection."
        const val LOCATION_NOT_AVAILABLE = "Location services not available."
        const val PERMISSION_DENIED = "Required permissions not granted."
        const val API_ERROR = "API error. Please try again later."
        const val INVALID_DESTINATION = "Invalid destination address."
    }

    // Success Messages
    object Success {
        const val ROUTE_CALCULATED = "Route calculated successfully"
        const val NAVIGATION_STARTED = "Navigation started"
        const val NAVIGATION_ENDED = "Navigation ended"
        const val LOCATION_UPDATED = "Location updated"
        const val SETTINGS_SAVED = "Settings saved"
    }

    // Default Values
    object Defaults {
        const val DEFAULT_VEHICLE_TYPE = "car"
        const val DEFAULT_FUEL_TYPE = "petrol"
        const val DEFAULT_MILEAGE = 15 // km/l
        const val DEFAULT_LANGUAGE = "en"
        const val DEFAULT_DARK_MODE = true
    }
}
