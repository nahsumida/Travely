package com.isabellatressino.travely.models


import com.google.firebase.firestore.GeoPoint
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale


class Place(
    val id: String,
    val name: String,
    val address: String,
    val description: String,
    val type: String,
    val rate: Double,
    val businessHours: Map<String, Array<String>>,
    val geopoint: GeoPoint,
    val profiles: Array<String>,
    val picture: String
) {

    // Função para verificar se o local está aberto no momento
    fun isOpen(): Boolean {
        val currentDay = getCurrentDayOfWeek() // Sun
        val currentTime = getCurrentTime() // 11:24

        val todayHours = businessHours[currentDay]

        return if (!todayHours.isNullOrEmpty()) {
            if (todayHours[0] == "closed") {
                false
            } else {
                val openTime = todayHours[0]
                val closeTime = todayHours[1]
                currentTime in openTime..closeTime
            }
        } else {
            false
        }
    }

    // Função para obter o horário de fechamento do local no dia atual
    fun getCloseTime(): String? {
        val currentDay = getCurrentDayOfWeek()
        val todayHours = businessHours[currentDay]

        return if (!todayHours.isNullOrEmpty() && todayHours[0] != "closed") {
            todayHours[1]
        } else {
            null
        }
    }

    // Função para obter o próximo horário de abertura
    fun getNextOpenTime(): String {
        val currentDay = getCurrentDayOfWeek() // Ex: Sun
        val currentTime = getCurrentTime() // Ex: 13:00
        val daysOfWeek = listOf("Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat")
        val daysInPortuguese = mapOf(
            "Sun" to "Domingo",
            "Mon" to "Segunda-feira",
            "Tue" to "Terça-feira",
            "Wed" to "Quarta-feira",
            "Thu" to "Quinta-feira",
            "Fri" to "Sexta-feira",
            "Sat" to "Sábado"
        )

        val currentIndex = daysOfWeek.indexOf(currentDay)

        // Verifica os horários de hoje primeiro
        val todayHours = businessHours[currentDay]
        if (!todayHours.isNullOrEmpty() && todayHours[0] != "closed") {
            val openTime = todayHours[0]

            if (currentTime < openTime) return "Hoje às $openTime"
        }

        // Se o local não abre mais hoje, percorre os próximos dias
        for (i in 1..7) {
            val nextIndex = (currentIndex + i) % 7
            val nextDay = daysOfWeek[nextIndex]
            val nextHours = businessHours[nextDay]

            if (!nextHours.isNullOrEmpty() && nextHours[0] != "closed") {
                val nextDayInPortuguese = daysInPortuguese[nextDay] ?: nextDay
                return "$nextDayInPortuguese às ${nextHours[0]}"
            }
        }
        return ""
    }


    // Função para obter o dia da semana atual em formato abreviado
    public fun getCurrentDayOfWeek(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("EEE").format(calendar.time).lowercase()
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
    }

    // Função para obter o horário atual no formato HH:mm
    public fun getCurrentTime(): String {
        val calendar = Calendar.getInstance()
        return SimpleDateFormat("HH:mm").format(calendar.time)
    }
}