package com.navibharat.ui.home

import androidx.lifecycle.ViewModel
import com.navibharat.data.local.NaviBharatDatabase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import timber.log.Timber
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val database: NaviBharatDatabase
) : ViewModel() {

    fun getRecentSearches(userId: String): Flow<List<com.navibharat.data.local.RecentSearchEntity>> {
        return database.recentSearchDao().getRecentSearches(userId)
    }

    fun getFavoriteRoutes(userId: String): Flow<List<com.navibharat.data.local.FavoriteRouteEntity>> {
        return database.favoriteRouteDao().getFavoriteRoutes(userId)
    }

    fun getTripHistory(userId: String): Flow<List<com.navibharat.data.local.TripEntity>> {
        return database.tripDao().getTripHistory(userId)
    }

    fun addFavoriteRoute(
        userId: String,
        name: String,
        originLat: Double,
        originLng: Double,
        originAddress: String,
        destLat: Double,
        destLng: Double,
        destAddress: String
    ) {
        Timber.i("Adding favorite route: $name")
        // Implementation in production
    }

    fun addRecentSearch(
        userId: String,
        address: String,
        lat: Double,
        lng: Double
    ) {
        Timber.i("Adding recent search: $address")
        // Implementation in production
    }
}
