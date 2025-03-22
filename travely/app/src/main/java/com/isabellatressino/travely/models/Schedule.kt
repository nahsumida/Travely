package com.isabellatressino.travely.models

import android.util.Log
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class Schedule(
    var placeID: String,
    var availability: Int,
    var price: Double,
    var datetime: String
) {
    override fun toString(): String {
        return "Schedule(placeID='$placeID', availability='$availability', price=$price, datetime='$datetime')"
    }

    private fun parseDate(datetime: String): Date? {
        return try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
            inputFormat.parse(datetime)
        } catch (e: Exception) {
            Log.e("DateParsing", "Erro ao converter data: ${e.message}")
            null
        }
    }

    fun extractDay(datetime: String): String {
        val date = parseDate(datetime)
        val outputFormat = SimpleDateFormat("dd", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractMonth(datetime: String): String {
        val date = parseDate(datetime)
        val outputFormat = SimpleDateFormat("MM", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractYear(datetime: String): String {
        val date = parseDate(datetime)
        val outputFormat = SimpleDateFormat("yyyy", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }

    fun extractHour(datetime: String): String {
        val date = parseDate(datetime)
        val outputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        return date?.let { outputFormat.format(it) } ?: ""
    }


}