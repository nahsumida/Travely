package com.isabellatressino.travely.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R

class DaysAdapter(
    private var days: List<Pair<Int, String>>,
    private val onDaySelected: (Int) -> Unit
) : RecyclerView.Adapter<DaysAdapter.DayViewHolder>() {

    private var selectedDay: Int? = null

    inner class DayViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtDay: TextView = itemView.findViewById(R.id.tv_day)
        val cardView: CardView = itemView.findViewById(R.id.cardViewDate)
        val txtDayOfWeek: TextView = itemView.findViewById(R.id.tv_day_of_week)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DayViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.recyclerview_days, parent, false)
        return DayViewHolder(view)
    }

    override fun onBindViewHolder(holder: DayViewHolder, position: Int) {
        val (day, dayOfWeek) = days[position]  // Desestruturando o par (dia, dia da semana)

        holder.txtDay.text = day.toString()
        holder.txtDayOfWeek.text = dayOfWeek

        if (day == selectedDay) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.primaryColor)
            )
            holder.txtDay.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.onPrimary)
            )
            holder.txtDayOfWeek.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.onPrimary)
            )
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.surfaceColor)
            )
            holder.txtDay.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.onSurface)
            )
            holder.txtDayOfWeek.setTextColor(
                ContextCompat.getColor(holder.itemView.context, R.color.onSurface)
            )
        }

        holder.itemView.setOnClickListener {
            selectedDay = day
            notifyDataSetChanged()
            onDaySelected(day)
        }
    }

    override fun getItemCount(): Int = days.size

    // Atualiza os dias exibidos na RecyclerView
    fun updateDays(newDays: List<Pair<Int, String>>) {
        days = newDays
        notifyDataSetChanged()
    }

    // Define o dia selecionado
    fun selectDay(day: Int) {
        selectedDay = day
        notifyDataSetChanged()  // Atualiza a RecyclerView
    }
}
