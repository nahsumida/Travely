package com.isabellatressino.travely.adapters

import RecommendationItem
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R
import com.isabellatressino.travely.dao.PlaceDao

class CarouselAdapter(private val items: List<RecommendationItem>) :
    RecyclerView.Adapter<CarouselAdapter.CarouselViewHolder>() {

    class CarouselViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val name: TextView = itemView.findViewById(R.id.placeName)
        val subtypes: TextView = itemView.findViewById(R.id.placeSubtypes)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_place_card, parent, false)
        Log.d("CarouselAdapter", "ViewHolder criado")
        return CarouselViewHolder(view)
    }

    override fun onBindViewHolder(holder: CarouselViewHolder, position: Int) {
        val recommendation = items[position]

        // Buscar informações completas do lugar pelo id
        PlaceDao().getPlaces(
            onSuccess = { allPlaces ->
                val place = allPlaces.find { it.id == recommendation.id }

                if (place != null) {
                    holder.name.text = place.name
                    holder.subtypes.text = place.subtypes.joinToString(", ")
                    Log.d("CarouselAdapter", "Place ${place.name} carregado do Firestore")
                } else {
                    holder.name.text = recommendation.name
                    holder.subtypes.text = recommendation.subtypes.joinToString(", ")
                    Log.w("CarouselAdapter", "Place com id ${recommendation.id} não encontrado no Firestore")
                }
            },
            onFailure = { e ->
                Log.e("CarouselAdapter", "Erro ao buscar dados do lugar: ${e.message}", e)
                // Fallback para os dados da API caso falhe
                holder.name.text = recommendation.name
                holder.subtypes.text = recommendation.subtypes.joinToString(", ")
            }
        )
    }


    override fun getItemCount(): Int {
        Log.d("CarouselAdapter", "Total de itens: ${items.size}")
        return items.size
    }
}
