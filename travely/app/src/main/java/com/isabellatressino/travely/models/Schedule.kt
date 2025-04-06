package com.isabellatressino.travely.models

import android.util.Log
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Schedule(
    val id: String,
    val date: String, // formato: "2025-04-05T14:30:00Z" - TODO: transformar em timestamp ou datetime
    val placeID: String,
    val placeName: String,
    val price: Double,
    val amount: Int
) : Serializable {

    override fun toString(): String {
        return "Schedule(uuid='$id', placeID='$placeID', placeName='$placeName', price=$price, amount=$amount, date='$date')"
    }

    private fun parseDate(): Date? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.timeZone = java.util.TimeZone.getTimeZone("UTC")
            inputFormat.parse(date)
        } catch (e: Exception) {
            Log.e("DateParsing", "Erro ao converter data: ${e.message}")
            null
        }
    }

    fun extractDay(): String {
        val date = parseDate()
        val outputFormat = SimpleDateFormat("dd", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractMonth(): String {
        val date = parseDate()
        val outputFormat = SimpleDateFormat("MM", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractYear(): String {
        val date = parseDate()
        val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractHour(): String {
        val date = parseDate()
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

}
