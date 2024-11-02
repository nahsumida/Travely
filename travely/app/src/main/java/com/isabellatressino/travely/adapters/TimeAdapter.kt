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
    var onTimeSelect: ((String) -> Unit)? = null

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
        holder.textView.text = times[position]

        if (position == selectedPosition) {
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
            // Atualiza a posição do item selecionado
            val previousPosition = selectedPosition
            selectedPosition = holder.adapterPosition

            // Notifica apenas os itens afetados
            notifyItemChanged(previousPosition)
            notifyItemChanged(selectedPosition)

            onTimeSelect?.invoke(times[selectedPosition])
        }

    }

    fun updateTimeList(newTimeList: List<String>) {
        times.clear()
        times.addAll(newTimeList)
        notifyDataSetChanged()
    }
}
