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

        // Buscar o Place completo pelo ID usando o DAO
        PlaceDao().getPlaceById(
            placeId = recommendation.id,
            onSuccess = { place ->
                if (place != null) {
                    holder.name.text = place.name
                    holder.subtypes.text = place.subtypes.joinToString(", ")
                    Log.d("CarouselAdapter", "Place ${place.name} carregado do Firestore")
                } else {
                    // Fallback para os dados da recomendação se o place não for encontrado
                    holder.name.text = recommendation.name
                    holder.subtypes.text = recommendation.subtypes.joinToString(", ")
                    Log.w("CarouselAdapter", "Place com id ${recommendation.id} não encontrado")
                }
            },
            onFailure = { e ->
                // Fallback em caso de erro
                holder.name.text = recommendation.name
                holder.subtypes.text = recommendation.subtypes.joinToString(", ")
                Log.e("CarouselAdapter", "Erro ao buscar place: ${e.message}", e)
            }
        )
    }


    override fun getItemCount(): Int {
        Log.d("CarouselAdapter", "Total de itens: ${items.size}")
        return items.size
    }
}
