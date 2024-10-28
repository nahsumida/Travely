package com.isabellatressino.travely.activities

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.models.Schedule
import java.text.SimpleDateFormat
import java.util.Locale
import com.isabellatressino.travely.R

class ScheduleAdapter(private val schedules: MutableList<Schedule>) :
    RecyclerView.Adapter<ScheduleAdapter.CardItemViewHolder>() {

    inner class CardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeName: TextView = itemView.findViewById(R.id.tvPlaceName)
        val date: TextView = itemView.findViewById(R.id.tvDate)
        val time: TextView = itemView.findViewById(R.id.tvTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recyclerview_schedule, parent, false)
        return CardItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return schedules.size
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        val schedule = schedules[position]

        Log.d("TESTE FIREBASE SCHEDULE", "Binding schedule at position $position: $schedule")

        val timeStamp = schedule.bookingData.toDate()

        // Formatação da data
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val formattedDate = dateFormat.format(timeStamp)

        // Formatação do horário
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val formattedTime = timeFormat.format(timeStamp)

        // Atribuindo valores às views
        holder.date.text = "Data: $formattedDate"
        holder.time.text = "Horário: $formattedTime"

        // Carrega o nome do local de forma assíncrona
        loadPlaceNameFromFirestore(schedule.placeID) { name ->
            holder.placeName.text = name
        }
    }

    private fun loadPlaceNameFromFirestore(placeId: String, callback: (String) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("places").document(placeId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: ""
                callback(name) // Passa o nome recuperado para a callback
            }
            .addOnFailureListener {
                Log.d("TESTE FIREBASE SCHEDULE", "Não foi possivel obter as informações do banco")
                callback("Nome não encontrado")
            }
    }

}