package com.isabellatressino.travely.models

import android.util.Log
import com.google.firebase.Timestamp
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Locale

class Schedule(
    val id: String,
    val date: Timestamp,
    val placeID: String,
    val placeName: String,
    val price: Double,
    val amount: Int
) : Serializable {

    override fun toString(): String {
        return "Schedule(id='$id', placeID='$placeID', placeName='$placeName', price=$price, amount=$amount, date=$date)"
    }

    private fun parseDate(): java.util.Date? {
        return try {
            date.toDate()
        } catch (e: Exception) {
            Log.e("DateParsing", "Erro ao converter Timestamp: ${e.message}")
            null
        }
    }

    fun extractDay(): String {
        val parsed = parseDate()
        val format = SimpleDateFormat("dd", Locale.getDefault())
        return parsed?.let { format.format(it) } ?: ""
    }

    fun extractMonth(): String {
        val parsed = parseDate()
        val format = SimpleDateFormat("MM", Locale.getDefault())
        return parsed?.let { format.format(it) } ?: ""
    }

    fun extractYear(): String {
        val parsed = parseDate()
        val format = SimpleDateFormat("yyyy", Locale.getDefault())
        return parsed?.let { format.format(it) } ?: ""
    }

    fun extractHour(): String {
        val parsed = parseDate()
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())
        return parsed?.let { format.format(it) } ?: ""
    }
}
