package com.isabellatressino.travely.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.isabellatressino.travely.R
import com.isabellatressino.travely.models.Option

class OptionAdapter(
    private val options: List<Option>
) : RecyclerView.Adapter<OptionAdapter.OptionViewHolder>() {

    private var selectedPosition = -1

    inner class OptionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.optionIcon)
        val label: TextView = view.findViewById(R.id.optionLabel)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OptionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_option, parent, false)
        return OptionViewHolder(view)
    }

    override fun onBindViewHolder(holder: OptionViewHolder, position: Int) {
        val option = options[position]
        holder.icon.setImageResource(option.iconResId)
        holder.label.text = option.label

        holder.icon.setBackgroundResource(
            if (position == selectedPosition)
                R.drawable.option_selected_background
            else
                R.drawable.option_default_background
        )

        holder.itemView.setOnClickListener {
            val clickedPosition = holder.adapterPosition
            if (clickedPosition == RecyclerView.NO_POSITION) return@setOnClickListener

            val previousSelected = selectedPosition
            selectedPosition = clickedPosition

            notifyItemChanged(previousSelected)
            notifyItemChanged(selectedPosition)
        }
    }

    override fun getItemCount(): Int = options.size

    fun getSelectedOption(): Option? {
        return if (selectedPosition != -1) options[selectedPosition] else null
    }
}
