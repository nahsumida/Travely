package com.isabellatressino.travely

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat.startActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.models.Place

class SuggestionsAdapter(private val places: MutableList<Place>) :
    RecyclerView.Adapter<SuggestionsAdapter.CardItemViewHolder>() {

    inner class CardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.tv_name)
        val placeRate: TextView = itemView.findViewById(R.id.tv_rate)
        val placeImage: ImageView = itemView.findViewById(R.id.img_place)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recycler_view_suggestions, parent, false)
        return CardItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return places.size
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        val place = places[position]
        holder.placeName.text = place.name
        holder.placeRate.text = place.rate.toString()

        if (place.picture.isNotEmpty()) {
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(place.picture)
            storageReference.downloadUrl.addOnSuccessListener { uri ->
                Glide.with(holder.itemView.context)
                    .load(uri)
                    .into(holder.placeImage)
            }.addOnFailureListener {
                holder.placeImage.setImageResource(R.drawable.image_unavailable)
            }
        } else {
            holder.placeImage.setImageResource(R.drawable.image_unavailable)
        }

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, PlaceInfoActivity::class.java)
            intent.putExtra("PLACE_ID", place.id)
            startActivity(context, intent, null)
        }
    }
}

