package com.isabellatressino.travely.activity

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityScheduleBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.isabellatressino.travely.R
import com.isabellatressino.travely.adapters.DaysAdapter
import com.isabellatressino.travely.adapters.ScheduleAdapter
import com.isabellatressino.travely.adapters.TimeAdapter
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

val TAG = "TESTE FIREBASE SCHEDULE"

class ScheduleActivity : AppCompatActivity() {

    private val binding by lazy { ActivityScheduleBinding.inflate(layoutInflater) }
    private val calendar by lazy { Calendar.getInstance() }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private val firestore by lazy { FirebaseFirestore.getInstance() }

    private lateinit var adapterDays: DaysAdapter
    private lateinit var adapterSchedules: ScheduleAdapter
    private lateinit var schedulesList: List<Schedule>
    private val schedulesByDay: MutableMap<String, List<Schedule>> = mutableMapOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupSpinner()
        setupRecyclerViewDays()
        setupRecyclerViewSchedules()
        loadUserSchedules()
    }

    private fun setupSpinner() {
        val spinnerItems = getNextSixMonths()
        binding.spinner.adapter =
            ArrayAdapter(this, R.layout.spinner_item, spinnerItems).apply {
                setDropDownViewResource(R.layout.spinner_dropdown_item)
            }

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val (month, year) = parseSelectedMonth(
                    parent.getItemAtPosition(position).toString()
                )
                adapterSchedules.cleamList()
                adapterDays.resetSelection()
                adapterDays.updateDays(getDaysOfMonth(month, year))
                Log.d(TAG, "mes selecioando $month $year")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun parseSelectedMonth(selectedItem: String): Pair<Int, Int> {
        val (monthName, yearStr) = selectedItem.split(" ")
        val month = listOf(
            "JANEIRO", "FEVEREIRO", "MARÇO", "ABRIL", "MAIO", "JUNHO",
            "JULHO", "AGOSTO", "SETEMBRO", "OUTUBRO", "NOVEMBRO", "DEZEMBRO"
        )
            .indexOf(monthName.uppercase())
        return month to yearStr.toInt()
    }

    private fun getNextSixMonths(): List<String> {
        return (0..5).map {
            SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(calendar.time).uppercase()
                .also {
                    calendar.add(Calendar.MONTH, 1)
                }
        }
    }

    private fun getDaysOfMonth(month: Int, year: Int): List<String> {
        val days = mutableListOf<String>()
        calendar.set(year, month, 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))

        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        for (day in 1..daysInMonth) {
            calendar.set(year, month, day)
            val dayOfWeek = dayOfWeekFormat.format(calendar.time)
            if (month == currentMonth) {
                if (day >= currentDay) {
                    days.add(
                        String.format(
                            "%02d-%s-%02d-%04d",
                            day,
                            dayOfWeek,
                            month + 1,
                            year
                        )
                    )
                }
            } else {
                days.add(String.format("%02d-%s-%02d-%04d", day, dayOfWeek, month + 1, year))
            }
        }
        return days
    }

    private fun setupRecyclerViewDays() {
        adapterDays = DaysAdapter(mutableListOf())
            .apply {
                onDaySelected = { date ->
                    CoroutineScope(Dispatchers.IO).launch {
                        val schedules = loadSchedulesByDate(date)
                        withContext(Dispatchers.Main) {
                            adapterSchedules.updateSchedules(schedules)
                            Log.d(TAG, "dia selecionado $date")
                        }
                    }
                }
            }

        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = adapterDays
    }

    // Função que carrega os agendamentos do usuário do dia em uma lista, ordenando-a por horário
    private suspend fun loadSchedulesByDate(date: String): List<Schedule> {
        val ret = mutableListOf<Schedule>()
        if (!::schedulesList.isInitialized) {
            Log.w(TAG, "schedulesList não foi inicializada")
            withContext(Dispatchers.Main) {
                binding.tvNoSchedules.visibility = View.VISIBLE
                binding.recyclerviewSchedules.visibility = View.GONE
            }
            return ret
        }
        val cleanedDate = date.replace(".", "")
        val (year, month, day, _) = cleanedDate.split("-")
        val formattedDate = "$year-$month-$day"

        //Log.d("FORMATACAO", "Data formatada para busca: $formattedDate")
        // Carregando lista de reservas do dia a partir do cache (map)
        val sortedSchedule = schedulesByDay[formattedDate] ?: emptyList()

        withContext(Dispatchers.Main) {
            if (sortedSchedule.isEmpty()) {
                binding.tvNoSchedules.visibility = View.VISIBLE
                binding.recyclerviewSchedules.visibility = View.GONE
            } else {
                binding.tvNoSchedules.visibility = View.GONE
                binding.recyclerviewSchedules.visibility = View.VISIBLE
            }
        }
        Log.d(TAG, "$sortedSchedule")
        return sortedSchedule
    }

    private fun setupRecyclerViewSchedules() {
        adapterSchedules = ScheduleAdapter(mutableListOf())
        binding.recyclerviewSchedules.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewSchedules.adapter = adapterSchedules
    }

    private fun getCurrentDateInCustomFormat(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-eee", Locale("pt", "BR"))
        return today.format(formatter)
    }

    private fun loadUserSchedules() {
        schedulesList = emptyList()
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val documents = firestore.collection("users")
                    .whereEqualTo("authID",uid)
                    .get().await()
                val user = documents.firstOrNull()
                if (user != null) {
                    schedulesList = extractScheduleData(user)
                    // Preenchendo o Map de reservas por dia
                    schedulesList.forEach { schedule ->
                        val (scheduleDate, _) = schedule.datetime.split("T")
                        if (!schedulesByDay.containsKey(scheduleDate)) {
                            schedulesByDay[scheduleDate] = mutableListOf()
                        }
                        (schedulesByDay[scheduleDate] as MutableList).add(schedule)
                    }
                    // Ordenando as listas por dia
                    schedulesByDay.forEach { (date, schedules) ->
                        schedulesByDay[date] = schedules.sortedBy { schedule -> schedule.datetime }
                    }
                    // Formatando data atual
                    val todayDisplay = getCurrentDateInCustomFormat()
                    val todayKeyFormat = todayDisplay.split("-").take(3).joinToString("-")
                    val todaySchedule = schedulesByDay[todayKeyFormat] ?: emptyList()
                    // Buscando reservas na data atual
                    withContext(Dispatchers.Main) {
                        adapterSchedules.updateSchedules(todaySchedule)
                        adapterDays.selectDay(todayDisplay)
                    }
                    Log.d(TAG, "$schedulesList")
                } else {
                    Log.d(TAG, "Document does not exist")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting document: ", e)
            }
        }
        /*
        firestore
            .collection("users")
            .whereEqualTo("authID",uid)
            .get()
            .addOnSuccessListener { documents ->
                val user = documents.firstOrNull()
                if (user != null) {
                    schedulesList = extractScheduleData(user)
                    schedulesList = schedulesList.sortedBy { schedule ->
                        schedule.datetime
                    }
                    val today = getCurrentDateInCustomFormat()
                    val todaySchedule = loadSchedulesByDate(today)
                    adapterSchedules.updateSchedules(todaySchedule)
                    adapterDays.selectDay(today)
                    Log.d(TAG, "$schedulesList")
                } else {
                    Log.d(TAG, "Document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting document: ", e)
            }*/
    }

    private fun extractScheduleData(document: DocumentSnapshot): List<Schedule> {
        val schedulesList =
            document.get("schedule") as? List<Map<String, Any>> ?: return emptyList()

        // Formato de saída (ISO 8601)
        val outputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        outputFormat.timeZone = TimeZone.getTimeZone("UTC") // Garante UTC no formato de saída

        val ret = schedulesList.map { scheduleMap ->
            val placeID = (scheduleMap["placeID"]) as? String ?: ""
            val amount = scheduleMap["amount"]?.toString()?.toIntOrNull() ?: 0
            val price = (scheduleMap["price"] as? Number)?.toDouble() ?: 0.0

            // Conversão do Timestamp para o formato ISO 8601
            val datetimeTimestamp = scheduleMap["datetime"] as? Timestamp
            val datetime = datetimeTimestamp?.toDate()?.let { outputFormat.format(it) } ?: ""

            Schedule(placeID, amount, price, datetime)
        }

        // Log dos dados extraídos
        ret.forEach { schedule ->
            Log.d(TAG, "Schedule datetime: ${schedule.datetime}")
        }

        return ret
    }
}
