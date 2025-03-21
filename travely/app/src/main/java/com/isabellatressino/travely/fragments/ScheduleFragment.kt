package com.isabellatressino.travely.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.adapters.DaysAdapter
import com.isabellatressino.travely.adapters.ScheduleAdapter
import com.isabellatressino.travely.dao.PlaceDao
import com.isabellatressino.travely.dao.ScheduleDao
import com.isabellatressino.travely.databinding.FragmentScheduleBinding
import java.util.Calendar

class ScheduleFragment : Fragment() {

    private var _binding: FragmentScheduleBinding? = null
    private val binding get() = _binding!!

    private val scheduleDao by lazy { ScheduleDao() }
    private val placeDao by lazy { PlaceDao() }

    private lateinit var scheduleAdapter: ScheduleAdapter
    private lateinit var daysAdapter: DaysAdapter

    private var selectedMonth = Calendar.getInstance().get(Calendar.MONTH) + 1
    private var selectedDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)

    private val calendar by lazy { Calendar.getInstance() }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentScheduleBinding.inflate(inflater, container, false)

        setupMonthSpinner()
        setupRecyclerViewDays()
        setupRecyclerViewSchedules()

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        loadDaysOfMonth()
        setDefaultSelectedDay()
        loadSchedules()
    }

    private fun setDefaultSelectedDay() {
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        selectedDay = currentDay
        loadSchedules()
    }

    private fun setupMonthSpinner() {
        val allMonths = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        val monthsToDisplay = mutableListOf<String>()
        for (i in 0..3) {
            val monthIndex = (currentMonth + i) % 12
            monthsToDisplay.add(allMonths[monthIndex])
        }

        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            monthsToDisplay
        )
        binding.spinner.adapter = adapter

        binding.spinner.setSelection(0)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth =
                    currentMonth + position + 1
                loadDaysOfMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }


    private fun setupRecyclerViewDays() {
        daysAdapter = DaysAdapter(emptyList()) { day ->
            selectedDay = day
            loadSchedules()
        }
        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = daysAdapter
    }


    private fun loadDaysOfMonth() {
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH) + 1 // Os meses começam do zero

        val daysToDisplay = mutableListOf<Pair<Int, String>>() // Para armazenar o dia do mês e o dia da semana

        if (selectedMonth == currentMonth) {
            selectedDay = currentDay

            // Adiciona dias anteriores ao dia atual, se necessário
            when {
                currentDay > 2 -> {
                    daysToDisplay.add(Pair(currentDay - 2, getDayOfWeek(currentDay - 2)))
                    daysToDisplay.add(Pair(currentDay - 1, getDayOfWeek(currentDay - 1)))
                }

                currentDay == 2 -> {
                    daysToDisplay.add(Pair(currentDay - 1, getDayOfWeek(currentDay - 1)))
                }
            }

            // Adiciona os dias restantes a partir do dia atual até o final do mês
            val remainingDays = (currentDay..daysInMonth).toList()
            remainingDays.forEach { day ->
                daysToDisplay.add(Pair(day, getDayOfWeek(day)))
            }
        } else {
            // Se não for o mês atual, simplesmente carrega todos os dias do mês selecionado
            for (day in 1..daysInMonth) {
                daysToDisplay.add(Pair(day, getDayOfWeek(day)))
            }
            selectedDay = 1
        }

        // Atualiza os dias no adaptador, incluindo o dia da semana
        daysAdapter.updateDays(daysToDisplay)

        // Seleciona o dia correto
        daysAdapter.selectDay(selectedDay)
        loadSchedules()
    }

    private fun getDayOfWeek(day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        calendar.set(Calendar.DAY_OF_MONTH, day)
        val dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK)

        return when (dayOfWeek) {
            Calendar.SUNDAY -> "Dom"
            Calendar.MONDAY -> "Seg"
            Calendar.TUESDAY -> "Ter"
            Calendar.WEDNESDAY -> "Qua"
            Calendar.THURSDAY -> "Qui"
            Calendar.FRIDAY -> "Sex"
            Calendar.SATURDAY -> "Sab"
            else -> ""
        }
    }



    private fun setupRecyclerViewSchedules() {
        scheduleAdapter = ScheduleAdapter(mutableListOf())
        binding.recyclerviewSchedules.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recyclerviewSchedules.adapter = scheduleAdapter
    }

    private fun loadSchedules() {
        scheduleAdapter.updateSchedules(emptyList())

        FirebaseAuth.getInstance().currentUser?.uid?.let { authID ->
            scheduleDao.getSchedulesByUser(
                authID,
                onSuccess = { schedules ->

                    Log.d(
                        "ScheduleDebug",
                        "Mês selecionado: ${
                            selectedMonth.toString().padStart(2, '0')
                        } | Dia selecionado: ${selectedDay.toString().padStart(2, '0')}"
                    )

                    val schedulesWithPlaces = mutableListOf<Map<String, Any>>()

                    schedules.forEach { schedule ->
                        val scheduleMonth = schedule.extractMonth(schedule.datetime)
                        val scheduleDay = schedule.extractDay(schedule.datetime)

                        Log.d(
                            "ScheduleDebug",
                            "Mês da reserva: $scheduleMonth | Dia da reserva: $scheduleDay"
                        )

                        if (scheduleMonth == selectedMonth.toString().padStart(2, '0') &&
                            scheduleDay == selectedDay.toString().padStart(2, '0')
                        ) {
                            Log.d("ScheduleDebug", "Agendamento corresponde à data selecionada")

                            // Buscar o lugar e associar à agenda
                            placeDao.getPlaceById(
                                schedule.placeID,
                                onSuccess = { place ->
                                    val scheduleMap = mapOf(
                                        "schedule" to schedule,
                                        "placeName" to (place?.name ?: "Nome indisponível"),
                                        "placeAddress" to (place?.address
                                            ?: "Endereço indisponível")
                                    )
                                    schedulesWithPlaces.add(scheduleMap)

                                    scheduleAdapter.updateSchedules(schedulesWithPlaces)
                                },
                                onFailure = { Log.e("ScheduleDebug", "Erro ao buscar lugar") }
                            )
                        }
                    }
                },
                onFailure = { exception ->
                    Log.e("ScheduleDebug", "Erro ao buscar agendamentos", exception)
                }
            )
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}