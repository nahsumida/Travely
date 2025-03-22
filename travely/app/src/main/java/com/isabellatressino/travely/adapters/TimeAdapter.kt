package com.isabellatressino.travely.adapters

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

    // Constantes para horários inválidos
    companion object {
        private val INVALID_TIMES =
            setOf("Nenhum horário disponível", "Fechado", "Informação indisponível")
    }

    inner class TimeItemViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textView: TextView = itemView.findViewById(R.id.tv_time)
        val cardView: CardView = itemView.findViewById(R.id.cv_time)

        // Cores de seleção armazenadas
        val selectedColor = ContextCompat.getColor(itemView.context, R.color.primaryColor)
        val unselectedColor = ContextCompat.getColor(itemView.context, R.color.onPrimary)
        val selectedTextColor = ContextCompat.getColor(itemView.context, R.color.onPrimary)
        val unselectedTextColor = ContextCompat.getColor(itemView.context, R.color.onSecondary)
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

        // Verificação para destacar o item selecionado
        if (position == selectedPosition && time !in INVALID_TIMES) {
            holder.cardView.setCardBackgroundColor(holder.selectedColor)
            holder.textView.setTextColor(holder.selectedTextColor)
        } else {
            holder.cardView.setCardBackgroundColor(holder.unselectedColor)
            holder.textView.setTextColor(holder.unselectedTextColor)
        }

        holder.itemView.setOnClickListener {
            if (time !in INVALID_TIMES) {
                val previousPosition = selectedPosition
                selectedPosition = holder.adapterPosition

                notifyItemChanged(previousPosition)
                notifyItemChanged(selectedPosition)

                onTimeSelected?.invoke(time)
            }
        }
    }

    // Reseta a seleção
    fun resetSelection() {
        selectedPosition = -1
        notifyDataSetChanged()
    }

    // Atualiza a lista de horários
    fun updateTimeList(newTimeList: List<String>) {
        times.clear()
        times.addAll(newTimeList)
        resetSelection()
        notifyDataSetChanged()
    }

}

