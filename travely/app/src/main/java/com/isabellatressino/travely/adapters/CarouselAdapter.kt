package com.isabellatressino.travely.adapters

import RecommendationItem
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R

class CarouselAdapter(private val items: List<RecommendationItem>) :
RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.placeName)
        val subtypes: TextView = itemView.findViewById(R.id.placeSubtypes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        Log.e("testeeeioio", "cheguei no adapter")

        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val item = items[position]
        holder.name.text = item.name
        holder.subtypes.text = item.subtypes.joinToString(", ")
        Log.e("testeeeioio", "cheguei no adapter")

    }

    override fun getItemCount(): Int = items.size
}
