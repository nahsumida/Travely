package com.isabellatressino.travely.models

import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Locale

class Schedule(
    var placeID: String,
    var availability: Any,
    var price: Double,
    var datetime: String
) {
    override fun toString(): String {
        return "Schedule(datetime=$datetime, placeID='$placeID', availability='$availability', preco=$price)"
    }

    // Método para obter o dia
    fun getDay(): String {
        return formatDate("dd")
    }

    // Método para obter o mês
    fun getMonth(): String {
        return formatDate("MM")
    }

    // Método para obter o ano
    fun getYear(): String {
        return formatDate("yyyy")
    }

    // Método para obter hora e minuto
    fun getHourMinute(): String {
        return formatDate("HH:mm")
    }

    // Função auxiliar para formatação
    private fun formatDate(pattern: String): String {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            val date = dateFormat.parse(datetime)
            val outputFormat = SimpleDateFormat(pattern, Locale.getDefault())
            outputFormat.format(date)
        } catch (e: Exception) {
            ""
        }
    }

}

