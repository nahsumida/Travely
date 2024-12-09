package com.isabellatressino.travely.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import java.text.SimpleDateFormat
import java.util.Locale
import com.isabellatressino.travely.R
import com.isabellatressino.travely.models.Schedule

class ScheduleAdapter(private val schedules: MutableList<Schedule>) :
    RecyclerView.Adapter<ScheduleAdapter.CardItemViewHolder>() {

    private var selectedPosition = -1

    inner class CardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvType: TextView = itemView.findViewById(R.id.tv_type)
        val tvPlaceName: TextView = itemView.findViewById(R.id.tv_place_name)
        val tvPlaceAddress: TextView = itemView.findViewById(R.id.tv_place_address)
        val tvScheduleTime: TextView = itemView.findViewById(R.id.tv_schedule_time)
        val tvScheduleQuantity: TextView = itemView.findViewById(R.id.tv_schedule_quantity)
        val tvSchedulePrice: TextView = itemView.findViewById(R.id.tv_schedule_price)
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
        val (scheduleDate, scheduleTime) = schedule.datetime.split("T")
        Log.d("ScheduleAdapter", "$schedule")

        holder.tvScheduleTime.text = scheduleTime.slice(0..4)
        if (schedule.price > 0) {
            holder.tvSchedulePrice.text = "R$ ${String.format(" %.2f", schedule.price).replace(".", ",")}"
        }


        loadPlaceNameFromFirestore(schedule.placeID) { ret ->

            holder.tvPlaceName.text = ret.name
            holder.tvPlaceAddress.text = ret.address

            if (ret.type == "compra") {
                holder.tvType.text = "Ingresso"
                if (schedule.availability is String) (schedule.availability as String).toInt()
                holder.tvScheduleQuantity.text = "${schedule.availability} " + if (schedule.availability == 1) "pessoa" else "pessoas"

            } else if (ret.type == "reserva") {
                holder.tvType.text = "Agendamento"
                holder.tvScheduleQuantity.text = ""
                holder.tvSchedulePrice.text = ""
            }
        }
    }

    fun cleamList() {
        schedules.clear()
        notifyDataSetChanged()
    }

    fun updateSchedules(newSchedules: List<Schedule>) {
        schedules.clear()
        schedules.addAll(newSchedules)
        notifyDataSetChanged()
    }

    private fun loadPlaceNameFromFirestore(placeId: String, callback: (PlaceSchedule) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        firestore.collection("places").document(placeId).get()
            .addOnSuccessListener { document ->
                val name = document.getString("name") ?: ""
                val address = document.getString("address") ?: ""
                val type = document.getString("type") ?: ""
                callback(PlaceSchedule(name, address, type))
            }
            .addOnFailureListener {
                Log.d("TESTE FIREBASE SCHEDULE", "Não foi possivel obter as informações do banco")
            }
    }
}

data class PlaceSchedule(
    val name: String,
    val address: String,
    val type: String
)