package com.isabellatressino.travely.adapters

import RecommendationItem
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.PlaceInfoActivity
import com.isabellatressino.travely.R
import com.isabellatressino.travely.dao.PlaceDao

class CarouselAdapter(private val items: List<RecommendationItem>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.placeName)
        val rating: TextView = itemView.findViewById(R.id.tv_rate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        Log.d("CarouselAdapter", "ViewHolder criado")
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val recommendation = items[position]

        PlaceDao().getPlaces(
            onSuccess = { allPlaces ->
                val place = allPlaces.find { it.id == recommendation.id }

                if (place != null) {
                    holder.name.text = place.name
                    holder.rating.text = place.rating.toString()
                    Log.d("CarouselAdapter", "Place ${place.name} carregado do Firestore")

                    holder.itemView.setOnClickListener {
                        val context = holder.itemView.context
                        val intent = Intent(context, PlaceInfoActivity::class.java)
                        intent.putExtra("PLACE_ID", place.id)
                        startActivity(context, intent, null)
                    }
                } else {
                    holder.name.text = recommendation.name
                    Log.w("CarouselAdapter", "Place com id ${recommendation.id} não encontrado no Firestore")
                }
            },
            onFailure = { e ->
                Log.e("CarouselAdapter", "Erro ao buscar dados do lugar: ${e.message}", e)
                holder.name.text = recommendation.name
            }
        )
    }

    override fun getItemCount(): Int {
        Log.d("CarouselAdapter", "Total de itens: ${items.size}")
        return items.size
    }
}
