package com.isabellatressino.travely.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R

class TimeAdapter(private var times: MutableList<String>) :
    RecyclerView.Adapter<TimeAdapter.TimeItemViewHolder>() {

    private var selectedPosition = -1
    var onTimeSelected: ((String) -> Unit)? = null


    inner class TimeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_time)
        val cardView: CardView = itemView.findViewById(R.id.cv_time)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TimeItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recyclerview_times, parent, false)
        return TimeItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return times.size
    }

    override fun onBindViewHolder(holder: TimeItemViewHolder, position: Int) {
        val time = times[position]
        holder.textView.text = time

        if (position == selectedPosition && time != "Nenhum horário disponível" && time != "Fechado" && time != "Informação indisponível") {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.purple_haze)
            )
            holder.textView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.textView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.black
                )
            )
        }


        holder.itemView.setOnClickListener {
            if (time != "Nenhum horário disponível" && time != "Fechado" && time != "Informação indisponível"){
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onTimeSelected?.invoke(time)
            }
        }


    }

    fun resetSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    fun updateTimeList(newTimeList: List<String>) {
        times.clear()
        times.addAll(newTimeList)
        notifyDataSetChanged()
    }

}
