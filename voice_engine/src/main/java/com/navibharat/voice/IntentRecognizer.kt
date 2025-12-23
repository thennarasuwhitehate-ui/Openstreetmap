package com.navibharat.voice

import com.google.gson.Gson
import com.google.gson.JsonParser
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import timber.log.Timber

data class VoiceCommand(
    val intent: String,
    val parameters: CommandParameters,
    val confidence: Float,
    val response: String
)

data class CommandParameters(
    val destination: String? = null,
    val languageCode: String? = null,
    val avoidType: String? = null
)

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig
)

data class GeminiContent(
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String
)

data class GenerationConfig(
    val temperature: Float = 0.7f,
    val topP: Float = 1.0f,
    val topK: Int = 40,
    val maxOutputTokens: Int = 200,
    val responseMimeType: String = "application/json"
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>
)

data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String
)

interface GeminiAPI {
    @Headers("Content-Type: application/json")
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Body request: GeminiRequest
    ): GeminiResponse
}

class IntentRecognizer(
    private val geminiApiKey: String,
    private val currentContext: NaviAIContext? = null
) {

    private val gson = Gson()
    private val geminiApi: GeminiAPI by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()
        retrofit.create(GeminiAPI::class.java)
    }

    suspend fun recognizeIntent(speechText: String): VoiceCommand {
        return withContext(Dispatchers.IO) {
            try {
                val prompt = buildPrompt(speechText)
                val response = geminiApi.generateContent(
                    GeminiRequest(
                        contents = listOf(
                            GeminiContent(
                                parts = listOf(GeminiPart(prompt))
                            )
                        ),
                        generationConfig = GenerationConfig()
                    )
                )

                parseGeminiResponse(response)
            } catch (e: Exception) {
                Timber.e(e, "Error recognizing intent from: $speechText")
                createErrorCommand()
            }
        }
    }

    private fun buildPrompt(speechText: String): String {
        val contextStr = if (currentContext != null) {
            """
            Current context:
            - Location: ${currentContext.currentLat}, ${currentContext.currentLng}
            - Destination: ${currentContext.currentDestination ?: "None"}
            - Route in progress: ${currentContext.routeInProgress}
            - Vehicle: ${currentContext.vehicleType}
            - Fuel type: ${currentContext.fuelType}
            - Language: ${currentContext.userLanguage}
            - Remaining distance: ${currentContext.remainingDistance}m
            - Current speed: ${currentContext.currentSpeed} km/h
            """.trimIndent()
        } else ""

        return """
        You are a navigation assistant for an Indian navigation app.
        $contextStr

        User voice command: "$speechText"

        Parse this into a structured intent. Return ONLY valid JSON (no markdown):
        {
          "intent": "navigate|avoid_tolls|show_fuel_cost|nearby_petrol|nearby_ev|nearby_mechanic|nearby_rest|mute|unmute|language_switch|reroute|repeat|stop|check_toll|speed_limit|help",
          "parameters": {
            "destination": "string or null",
            "language_code": "en|ta|hi|te|kn|ml or null",
            "avoid_type": "tolls|highways|narrow_roads or null"
          },
          "confidence": 0.0-1.0,
          "response": "User-friendly response text"
        }
        """.trimIndent()
    }

    private fun parseGeminiResponse(response: GeminiResponse): VoiceCommand {
        return try {
            val candidate = response.candidates.firstOrNull()
                ?: return createErrorCommand()
            val text = candidate.content.parts.firstOrNull()?.text
                ?: return createErrorCommand()

            val jsonObject = JsonParser.parseString(text).asJsonObject
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
            Timber.e(e, "Error parsing Gemini response")
            createErrorCommand()
        }
    }

    private fun createErrorCommand(): VoiceCommand {
        return VoiceCommand(
            intent = "help",
            parameters = CommandParameters(),
            confidence = 0f,
            response = "I couldn't understand that command. Please try again."
        )
    }
}

data class NaviAIContext(
    val currentLat: Double,
    val currentLng: Double,
    val currentDestination: String? = null,
    val routeInProgress: Boolean = false,
    val vehicleType: String = "car",
    val fuelType: String = "petrol",
    val userLanguage: String = "en",
    val remainingDistance: Int? = null,
    val remainingTime: Int? = null,
    val nextTurnDistance: Int? = null,
    val currentSpeed: Int = 0
)
