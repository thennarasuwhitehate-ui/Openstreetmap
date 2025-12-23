package com.navibharat.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.navibharat.data.local.TripEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TripsAdapter(
    private val onTripClick: (TripEntity) -> Unit
) : RecyclerView.Adapter<TripsAdapter.TripViewHolder>() {

    private val trips = mutableListOf<TripEntity>()

    fun submitList(newTrips: List<TripEntity>) {
        trips.clear()
        trips.addAll(newTrips)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TripViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return TripViewHolder(view, onTripClick)
    }

    override fun onBindViewHolder(holder: TripViewHolder, position: Int) {
        holder.bind(trips[position])
    }

    override fun getItemCount() = trips.size

    class TripViewHolder(
        itemView: View,
        private val onTripClick: (TripEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val destinationTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val detailsTextView: TextView = itemView.findViewById(android.R.id.text2)
        private val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

        fun bind(trip: TripEntity) {
            destinationTextView.text = trip.destinationAddress ?: "Unknown destination"

            val distanceKm = (trip.distanceMeters / 1000).toInt()
            val durationMins = trip.durationSeconds / 60
            val dateStr = dateFormat.format(Date(trip.startTime))

            detailsTextView.text = "$distanceKm km • $durationMins mins • $dateStr"

            itemView.setOnClickListener {
                onTripClick(trip)
            }
        }
    }
}
