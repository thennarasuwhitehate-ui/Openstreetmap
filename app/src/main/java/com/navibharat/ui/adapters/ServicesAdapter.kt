package com.navibharat.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.navibharat.data.network.PlaceResult

class ServicesAdapter(
    private val onServiceClick: (PlaceResult) -> Unit,
    private val onDirectionsClick: (PlaceResult) -> Unit
) : RecyclerView.Adapter<ServicesAdapter.ServiceViewHolder>() {

    private val services = mutableListOf<PlaceResult>()

    fun submitList(newServices: List<PlaceResult>) {
        services.clear()
        services.addAll(newServices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return ServiceViewHolder(view, onServiceClick, onDirectionsClick)
    }

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size

    class ServiceViewHolder(
        itemView: View,
        private val onServiceClick: (PlaceResult) -> Unit,
        private val onDirectionsClick: (PlaceResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val addressTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(service: PlaceResult) {
            nameTextView.text = service.name
            addressTextView.text = service.address ?: "Unknown location"

            itemView.setOnClickListener {
                onServiceClick(service)
            }
        }
    }
}

/**
 * Enhanced Services Adapter with more details
 */
class ServicesDetailAdapter(
    private val onDirectionsClick: (PlaceResult) -> Unit,
    private val onCallClick: (PlaceResult) -> Unit
) : RecyclerView.Adapter<ServicesDetailAdapter.ServiceDetailViewHolder>() {

    private val services = mutableListOf<PlaceResult>()

    fun submitList(newServices: List<PlaceResult>) {
        services.clear()
        services.addAll(newServices)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ServiceDetailViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            android.R.layout.activity_list_item,
            parent,
            false
        )
        return ServiceDetailViewHolder(view, onDirectionsClick, onCallClick)
    }

    override fun onBindViewHolder(holder: ServiceDetailViewHolder, position: Int) {
        holder.bind(services[position])
    }

    override fun getItemCount() = services.size

    class ServiceDetailViewHolder(
        itemView: View,
        private val onDirectionsClick: (PlaceResult) -> Unit,
        private val onCallClick: (PlaceResult) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val addressTextView: TextView? = itemView.findViewById(android.R.id.text2)

        fun bind(service: PlaceResult) {
            nameTextView.text = service.name
            addressTextView?.text = service.address ?: "Unknown location"

            itemView.setOnClickListener {
                onDirectionsClick(service)
            }
        }
    }
}
