package com.isabellatressino.travely.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R
import com.isabellatressino.travely.models.Schedule

class ScheduleAdapter(private var schedules: List<Map<String, Any>>) :
    RecyclerView.Adapter<ScheduleAdapter.CardItemViewHolder>() {


    inner class CardItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val placeNameTextView: TextView = itemView.findViewById(R.id.tv_name)
        val placeAddressTextView: TextView = itemView.findViewById(R.id.tv_place_address)
        val scheduleTime: TextView = itemView.findViewById(R.id.tv_schedule_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CardItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recyclerview_schedules, parent, false)
        return CardItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return schedules.size
    }

    override fun onBindViewHolder(holder: CardItemViewHolder, position: Int) {
        val scheduleMap = schedules[position]
        val schedule = scheduleMap["schedule"] as Schedule
        val placeName = scheduleMap["placeName"] as String
        val placeAddress = scheduleMap["placeAddress"] as String

        holder.placeNameTextView.text = placeName
        holder.placeAddressTextView.text = placeAddress
        holder.scheduleTime.text = schedule.extractHour()
    }

    fun updateSchedules(newSchedules: List<Map<String, Any>>) {
        schedules = newSchedules
        notifyDataSetChanged()
    }

}

