package com.navibharat.data.network

import com.google.gson.annotations.SerializedName
import retrofit2.http.GET
import retrofit2.http.Query
import retrofit2.http.POST
import retrofit2.http.Body

// Google Maps Directions API
interface GoogleMapsAPI {
    @GET("maps/api/directions/json")
    suspend fun getDirections(
        @Query("origin") origin: String,
        @Query("destination") destination: String,
        @Query("key") apiKey: String,
        @Query("alternatives") alternatives: Boolean = true,
        @Query("avoid") avoid: String? = null
    ): DirectionsResponse
}

data class DirectionsResponse(
    val routes: List<DirectionRoute>,
    val status: String
)

data class DirectionRoute(
    val polyline: PolylineData,
    val legs: List<RouteLeg>,
    val bounds: Bounds,
    val distance: Distance,
    val duration: Duration,
    val summary: String
)

data class PolylineData(
    val points: String
)

data class RouteLeg(
    val steps: List<RouteStep>,
    val distance: Distance,
    val duration: Duration,
    val start_address: String,
    val end_address: String
)

data class RouteStep(
    val html_instructions: String,
    val distance: Distance,
    val duration: Duration,
    val start_location: LatLngData,
    val end_location: LatLngData,
    val maneuver: String? = null,
    val polyline: PolylineData,
    val steps: List<RouteStep>? = null
)

data class Distance(
    val text: String,
    val value: Int
)

data class Duration(
    val text: String,
    val value: Int
)

data class Bounds(
    val northeast: LatLngData,
    val southwest: LatLngData
)

data class LatLngData(
    val lat: Double,
    val lng: Double
)

// Google Places API
interface GooglePlacesAPI {
    @GET("v1/places:searchNearby")
    suspend fun searchNearby(
        @Query("location.latitude") latitude: Double,
        @Query("location.longitude") longitude: Double,
        @Query("radius") radiusMeters: Int,
        @Query("type") type: String,
        @Query("key") apiKey: String
    ): PlacesResponse
}

data class PlacesResponse(
    val places: List<PlaceResult>,
    val nextPageToken: String? = null
)

data class PlaceResult(
    @SerializedName("name")
    val name: String,
    @SerializedName("place_id")
    val placeId: String,
    @SerializedName("location")
    val location: PlaceLocation,
    @SerializedName("rating")
    val rating: Float? = null,
    @SerializedName("formatted_address")
    val address: String? = null,
    @SerializedName("business_status")
    val businessStatus: String? = null,
    @SerializedName("photos")
    val photos: List<PlacePhoto>? = null
)

data class PlaceLocation(
    val latitude: Double,
    val longitude: Double
)

data class PlacePhoto(
    @SerializedName("name")
    val name: String,
    @SerializedName("height")
    val height: Int,
    @SerializedName("width")
    val width: Int
)

// MapMyIndia API
interface MapMyIndiaAPI {
    @GET("advancedMaps/v1/place_nearby")
    suspend fun searchNearby(
        @Query("location") location: String,
        @Query("radius") radius: Int,
        @Query("keyword") keyword: String,
        @Query("key") apiKey: String
    ): MapMyIndiaResponse
}

data class MapMyIndiaResponse(
    val responseCode: Int,
    val responseMessage: String,
    val responseData: MapMyIndiaData? = null
)

data class MapMyIndiaData(
    val suggestedLocations: List<MapMyIndiaPlace>
)

data class MapMyIndiaPlace(
    val placeName: String,
    val placeAddress: String,
    val latitude: Double,
    val longitude: Double,
    val placeId: String? = null
)

// Gemini API
interface GeminiAPI {
    @POST("v1beta/models/gemini-pro:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequest
    ): GeminiResponse
}

data class GeminiRequest(
    val contents: List<GeminiContent>,
    val generationConfig: GenerationConfig? = null,
    val safetySettings: List<SafetySetting>? = null
)

data class GeminiContent(
    val role: String,
    val parts: List<GeminiPart>
)

data class GeminiPart(
    val text: String? = null,
    val inlineData: InlineData? = null
)

data class InlineData(
    val mimeType: String,
    val data: String
)

data class GenerationConfig(
    val temperature: Float? = null,
    val topP: Float? = null,
    val topK: Int? = null,
    val maxOutputTokens: Int? = null,
    val stopSequences: List<String>? = null
)

data class SafetySetting(
    val category: String,
    val threshold: String
)

data class GeminiResponse(
    val candidates: List<GeminiCandidate>?,
    val promptFeedback: PromptFeedback?
)

data class GeminiCandidate(
    val content: GeminiContent,
    val finishReason: String?,
    val index: Int?,
    val safetyRatings: List<SafetyRating>?
)

data class SafetyRating(
    val category: String,
    val probability: String
)

data class PromptFeedback(
    val safetyRatings: List<SafetyRating>?
)
