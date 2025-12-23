package com.navibharat.data.repository

import com.google.gson.Gson
import com.google.gson.JsonParser
import com.navibharat.data.network.ApiClient
import com.navibharat.voice.GenerationConfig
import com.navibharat.voice.GeminiContent
import com.navibharat.voice.GeminiPart
import com.navibharat.voice.GeminiRequest
import com.navibharat.voice.IntentRecognizer
import com.navibharat.voice.NaviAIContext
import com.navibharat.voice.VoiceCommand
import dagger.hilt.android.scopes.ActivityScoped
import timber.log.Timber
import javax.inject.Inject

@ActivityScoped
class VoiceCommandRepository @Inject constructor(
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
                parameters = com.navibharat.voice.CommandParameters(
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
                parameters = com.navibharat.voice.CommandParameters(),
                confidence = 0f,
                response = "I couldn't understand that. Please try again."
            )
        }
    }

    /**
     * Execute voice command and return action
     */
    suspend fun executeCommand(command: VoiceCommand): Result<String> {
        return try {
            val action = when (command.intent) {
                "navigate" -> {
                    "Starting navigation to ${command.parameters.destination}"
                }
                "avoid_tolls" -> {
                    "Recalculating route without tolls"
                }
                "show_fuel_cost" -> {
                    "Showing fuel cost information"
                }
                "nearby_petrol" -> {
                    "Finding nearby petrol stations"
                }
                "nearby_ev" -> {
                    "Finding nearby EV chargers"
                }
                "mute" -> {
                    "Navigation voice muted"
                }
                "unmute" -> {
                    "Navigation voice enabled"
                }
                "language" -> {
                    "Changing language to ${command.parameters.languageCode}"
                }
                else -> {
                    command.response
                }
            }

            Timber.i("Command executed: ${command.intent}")
            Result.success(action)
        } catch (e: Exception) {
            Timber.e(e, "Error executing command")
            Result.failure(e)
        }
    }
}

// Helper data class for command parameters
data class CommandParameters(
    val destination: String? = null,
    val languageCode: String? = null,
    val avoidType: String? = null
)
