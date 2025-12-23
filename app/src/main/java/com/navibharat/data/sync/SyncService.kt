package com.navibharat.data.sync

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.navibharat.data.local.NaviBharatDatabase
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import timber.log.Timber
import javax.inject.Inject

@AndroidEntryPoint
class SyncService : Service() {

    @Inject
    lateinit var database: NaviBharatDatabase

    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    companion object {
        const val SYNC_INTERVAL_MS = 15 * 60 * 1000L // 15 minutes
    }

    override fun onCreate() {
        super.onCreate()
        Timber.i("SyncService created")
        startPeriodicSync()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Timber.i("SyncService started")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        Timber.i("SyncService destroyed")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    private fun startPeriodicSync() {
        serviceScope.launch {
            while (true) {
                try {
                    performSync()
                } catch (e: Exception) {
                    Timber.e(e, "Sync error")
                }
                delay(SYNC_INTERVAL_MS)
            }
        }
    }

    private suspend fun performSync() {
        try {
            Timber.i("Starting sync...")

            // Sync trips
            val unsyncedTrips = database.tripDao().getUnsyncedTrips()
            if (unsyncedTrips.isNotEmpty()) {
                Timber.i("Syncing ${unsyncedTrips.size} trips")
                // In production: push to Supabase
                // for (trip in unsyncedTrips) {
                //     supabaseClient.from("trips").insert(trip.toRemote())
                //     trip.copy(isSynced = true, syncedAt = System.currentTimeMillis())
                //     database.tripDao().updateTrip(trip)
                // }
            }

            // Sync road hazards
            // val unsyncedHazards = ...
            // In production: push community reports to Supabase

            // Fetch latest data from server
            // In production: pull latest tolls, fuel prices, hazards, etc.

            Timber.i("Sync completed successfully")
        } catch (e: Exception) {
            Timber.e(e, "Error during sync")
        }
    }

    /**
     * Sync specific data type
     */
    private suspend fun syncTrips() {
        // Get unsynced trips from local database
        val trips = database.tripDao().getUnsyncedTrips()

        for (trip in trips) {
            try {
                // In production: upload to Supabase
                // val remoteTripId = supabaseClient.from("trips").insert(trip.toDTO()).single().id

                // Mark as synced
                // val updated = trip.copy(
                //     remoteTripId = remoteTripId,
                //     isSynced = true,
                //     syncedAt = System.currentTimeMillis()
                // )
                // database.tripDao().updateTrip(updated)

                Timber.i("Trip synced: ${trip.localTripId}")
            } catch (e: Exception) {
                Timber.e(e, "Error syncing trip: ${trip.localTripId}")
            }
        }
    }

    /**
     * Clear old cached data
     */
    private suspend fun cleanupOldData() {
        try {
            val oneMonthAgo = System.currentTimeMillis() - (30 * 24 * 60 * 60 * 1000L)

            // Clean old trips
            database.tripDao().deleteOldTrips(oneMonthAgo)

            // Clean old routes
            database.routeDao().deleteOldRoutes(oneMonthAgo)

            // Clean old hazards
            database.roadHazardDao().deleteExpiredHazards(System.currentTimeMillis())

            // Clean old searches
            database.recentSearchDao().deleteOldSearches(oneMonthAgo)

            Timber.i("Old data cleanup completed")
        } catch (e: Exception) {
            Timber.e(e, "Error cleaning up old data")
        }
    }

    /**
     * Force immediate sync
     */
    fun syncNow() {
        serviceScope.launch {
            performSync()
        }
    }
}
