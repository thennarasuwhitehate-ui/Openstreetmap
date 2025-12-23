package com.navibharat.voice

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.navibharat.data.network.ApiClient
import com.navibharat.data.network.GeminiContent
import com.navibharat.data.network.GeminiPart
import com.navibharat.data.network.GeminiRequest
import com.navibharat.data.network.GenerationConfig
import timber.log.Timber

/**
 * Recognizes voice intents using Gemini API
 */
class IntentRecognizer(
    private val geminiApiKey: String = "YOUR_GEMINI_API_KEY"
) {

    private val geminiApi = ApiClient.getGeminiService()
    private val gson = Gson()

    /**
     * Recognize voice command using Gemini API
     */
    suspend fun recognizeCommand(
        speechText: String,
        context: NaviAIContext? = null
    ): Result<VoiceCommand> {
        return try {
            val prompt = buildCommandPrompt(speechText, context)

            val request = GeminiRequest(
                contents = listOf(
                    GeminiContent(
                        role = "user",
                        parts = listOf(GeminiPart(text = prompt))
                    )
                ),
                generationConfig = GenerationConfig(
                    temperature = 0.3f,
                    topP = 1.0f,
                    topK = 40,
                    maxOutputTokens = 256
                )
            )

            val response = geminiApi.generateContent(geminiApiKey, request)

            val responseText = response.candidates?.firstOrNull()
                ?.content?.parts?.firstOrNull()?.text
                ?: return Result.failure(Exception("Empty response from Gemini"))

            val command = parseCommandResponse(responseText, context?.userLanguage ?: "en")
            Result.success(command)
        } catch (e: Exception) {
            Timber.e(e, "Error recognizing command")
            Result.failure(e)
        }
    }

    private fun buildCommandPrompt(speechText: String, context: NaviAIContext?): String {
        val contextInfo = if (context != null) {
            """
            Navigation Context:
            - Location: ${context.currentLat}, ${context.currentLng}
            - Destination: ${context.currentDestination ?: "None"}
            - Vehicle: ${context.vehicleType}
            - Fuel type: ${context.fuelType}
            - Language: ${context.userLanguage}
            - Route active: ${context.routeInProgress}
            - Remaining: ${context.remainingDistance}m, ${context.remainingTime}s
            """.trimIndent()
        } else {
            "No active route"
        }

        return """
You are NaviAI, an intelligent navigation assistant for Indian drivers.

$contextInfo

User command: "$speechText"

Parse this into a structured JSON response:
{
  "intent": "navigate|avoid_tolls|show_fuel_cost|nearby_petrol|nearby_ev|nearby_mechanic|nearby_rest|mute|unmute|language|reroute|repeat|stop|check_toll|speed_limit|help",
  "parameters": {
    "destination": "specific location or null",
    "language_code": "en|ta|hi|te|kn|ml or null",
    "avoid_type": "tolls|highways|narrow_roads or null"
  },
  "confidence": 0.0-1.0,
  "response": "Natural response in the user's language"
}

Return ONLY the JSON, no markdown or extra text.
        """.trimIndent()
    }

    private fun parseCommandResponse(responseJson: String, language: String): VoiceCommand {
        return try {
            val jsonObject = JsonParser.parseString(responseJson).asJsonObject

            VoiceCommand(
                intent = jsonObject.get("intent").asString,
                parameters = CommandParameters(
                    destination = jsonObject.getAsJsonObject("parameters").get("destination")?.asString,
                    languageCode = jsonObject.getAsJsonObject("parameters").get("language_code")?.asString,
                    avoidType = jsonObject.getAsJsonObject("parameters").get("avoid_type")?.asString
                ),
                confidence = jsonObject.get("confidence").asFloat,
                response = jsonObject.get("response").asString
            )
        } catch (e: Exception) {
            Timber.e(e, "Error parsing command response")
            VoiceCommand(
                intent = "help",
                parameters = CommandParameters(),
                confidence = 0f,
                response = "I couldn't understand that. Please try again."
            )
        }
    }
}

/**
 * Data class representing a recognized voice command
 */
data class VoiceCommand(
    val intent: String,
    val parameters: CommandParameters,
    val confidence: Float,
    val response: String
)

/**
 * Parameters for a voice command
 */
data class CommandParameters(
    val destination: String? = null,
    val languageCode: String? = null,
    val avoidType: String? = null
)

/**
 * Context for intent recognition
 */
data class NaviAIContext(
    val currentLat: Double,
    val currentLng: Double,
    val currentDestination: String?,
    val routeInProgress: Boolean,
    val vehicleType: String, // "car", "bike", "commercial"
    val fuelType: String, // "petrol", "diesel", "cng", "ev"
    val userLanguage: String, // "en", "ta", "hi", "te", "kn", "ml"
    val remainingDistance: Int?, // meters
    val remainingTime: Int?, // seconds
    val nextTurnDistance: Int?, // meters
    val currentSpeed: Int // km/h
)
