package com.isabellatressino.travely

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.models.Schedule

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
        holder.placeName.text = "teste"
        holder.date.text = "Data: ${schedule.bookingData}"
        holder.time.text = "11"
    }
}