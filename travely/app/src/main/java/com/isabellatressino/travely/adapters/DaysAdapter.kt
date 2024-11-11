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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DaysAdapter(
    private var days: MutableList<String>,
) : RecyclerView.Adapter<DaysAdapter.DaysItemViewHolder>() {

    private var selectedPosition = -1
    var onDaySelected: ((String) -> Unit)? = null

    inner class DaysItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dayTextView: TextView = itemView.findViewById(R.id.tv_day)
        val dayOfWeekTextView: TextView = itemView.findViewById(R.id.tv_day_of_week)
        val cardView: CardView = itemView.findViewById(R.id.cardViewDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DaysItemViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.recycler_view_dates, parent, false)
        return DaysItemViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DaysItemViewHolder, position: Int) {
        val dayWithWeekday = days[position]
        val (day, weekDay, month,year) = dayWithWeekday.split("-")
        val dayWithMonth = getCurrentDayAndMonth()
        val (currentDay, currentMonth) = dayWithMonth.split("-")

        holder.dayTextView.text = day
        holder.dayOfWeekTextView.text = weekDay.replace(".", "")

        // Define a cor do item baseado na seleção ou se é o dia atual
        if (position == selectedPosition) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.purple_haze)
            )
            holder.dayTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
            holder.dayOfWeekTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        } else if (day == currentDay && month == currentMonth) {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.lime_green)
            )
            holder.dayTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
            holder.dayOfWeekTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        } else {
            holder.cardView.setCardBackgroundColor(
                ContextCompat.getColor(holder.itemView.context, R.color.white)
            )
            holder.dayTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.black
                )
            )
            holder.dayOfWeekTextView.setTextColor(
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

            // Chama o callback onDaySelected para enviar o dia da semana selecionado
            //onDaySelected?.invoke("$day-$weekDay-$month-$year")
            onDaySelected?.invoke("$year-$month-$day-$weekDay")
        }

    }

    override fun getItemCount(): Int = days.size

    // Método para atualizar os dias
    fun updateDays(newDays: List<String>) {
        days.clear()
        days.addAll(newDays)
        notifyDataSetChanged()
    }

    fun resetSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }


    // Pega o dia e o mes atual
    private fun getCurrentDayAndMonth(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("dd-MM", Locale.getDefault()).format(calendar.time)
    }
}