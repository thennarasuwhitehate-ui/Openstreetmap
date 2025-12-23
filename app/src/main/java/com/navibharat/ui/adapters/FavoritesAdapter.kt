package com.navibharat.ui.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.navibharat.data.local.FavoriteRouteEntity

class FavoritesAdapter(
    private val onRouteClick: (FavoriteRouteEntity) -> Unit,
    private val onDeleteClick: (FavoriteRouteEntity) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    private val favorites = mutableListOf<FavoriteRouteEntity>()

    fun submitList(newFavorites: List<FavoriteRouteEntity>) {
        favorites.clear()
        favorites.addAll(newFavorites)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_2, parent, false)
        return FavoriteViewHolder(view, onRouteClick, onDeleteClick)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(favorites[position])
    }

    override fun getItemCount() = favorites.size

    class FavoriteViewHolder(
        itemView: View,
        private val onRouteClick: (FavoriteRouteEntity) -> Unit,
        private val onDeleteClick: (FavoriteRouteEntity) -> Unit
    ) : RecyclerView.ViewHolder(itemView) {

        private val nameTextView: TextView = itemView.findViewById(android.R.id.text1)
        private val destinationTextView: TextView = itemView.findViewById(android.R.id.text2)

        fun bind(favorite: FavoriteRouteEntity) {
            nameTextView.text = favorite.name
            destinationTextView.text = favorite.destinationAddress ?: "Unknown destination"

            itemView.setOnClickListener {
                onRouteClick(favorite)
            }

            // Long press for delete
            itemView.setOnLongClickListener {
                onDeleteClick(favorite)
                true
            }
        }
    }
}
