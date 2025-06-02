package com.isabellatressino.travely.models

import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import android.util.Log

class Place(
    val address: String,
    val businessHours: Map<String, Map<String, String>>,
    val description: String,
    val geopoint: GeoPoint,
    val id: String,
    val keyWords: List<String>,
    val name: String,
    val rating: Double,
    val subtypes: List<String>,
    val picture: String
) {

    fun isOpen(): Boolean {
        val currentDay = getCurrentDayOfWeek()
        val currentTime = getCurrentTime()

        val todayHours = businessHours[currentDay]
        Log.d("PlaceModelTest", "currentDay: $currentDay")
        Log.d("PlaceModelTest", "businessHours keys: ${businessHours.keys}")

        Log.d("PlaceModelTest", "${todayHours}")

        return if (!todayHours.isNullOrEmpty()) {
            val openTime = todayHours["open"]
            val closeTime = todayHours["close"]

            if (!openTime.isNullOrEmpty() && !closeTime.isNullOrEmpty()) {
                currentTime >= openTime && currentTime <= closeTime
            } else {
                false
            }
        } else {
            false
        }
    }

    fun getCloseTime(): String? {
        val currentDay = getCurrentDayOfWeek()
        return businessHours[currentDay]?.get("close")
    }

    fun getNextOpenTime(): String {
        val currentDay = getCurrentDayOfWeek()
        val currentTime = getCurrentTime()

        val daysOfWeek = listOf(
            "sunday", "monday", "tuesday", "wednesday",
            "thursday", "friday", "saturday"
        )

        val daysInPortuguese = mapOf(
            "sunday" to "Domingo",
            "monday" to "Segunda-feira",
            "tuesday" to "Terça-feira",
            "wednesday" to "Quarta-feira",
            "thursday" to "Quinta-feira",
            "friday" to "Sexta-feira",
            "saturday" to "Sábado"
        )

        val currentIndex = daysOfWeek.indexOf(currentDay)

        // Verifica se ainda abre hoje
        val todayHours = businessHours[currentDay]
        val openTimeToday = todayHours?.get("open")
        if (!openTimeToday.isNullOrEmpty() && currentTime < openTimeToday) {
            return "Hoje às $openTimeToday"
        }

        // Verifica próximos dias
        for (i in 1..7) {
            val nextIndex = (currentIndex + i) % 7
            val nextDay = daysOfWeek[nextIndex]
            val nextHours = businessHours[nextDay]
            val nextOpenTime = nextHours?.get("open")

            if (!nextOpenTime.isNullOrEmpty()) {
                val nextDayInPortuguese = daysInPortuguese[nextDay] ?: nextDay
                return "$nextDayInPortuguese às $nextOpenTime"
            }
        }

        return "Horário de funcionamento indisponível"
    }

    private fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        val format = SimpleDateFormat("EEEE", Locale.US)
        Log.d("PlaceModelTest", "${format.format(calendar.time).lowercase()}")
        return format.format(calendar.time).lowercase()
    }

    private fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        Log.d("PlaceModelTest", "${SimpleDateFormat("HH:mm", Locale.US).format(calendar.time)}")
        return SimpleDateFormat("HH:mm", Locale.US).format(calendar.time)
    }

    override fun toString(): String {
        return "Place(name='$name')"
    }
}
