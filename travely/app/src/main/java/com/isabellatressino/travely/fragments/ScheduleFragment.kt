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

    private var lastSelectedDay = -1

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
        // Cria uma lista de meses do ano
        val allMonths = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        // Obtemos o mês atual
        val currentMonth = Calendar.getInstance().get(Calendar.MONTH)

        // Criamos uma lista apenas com o mês atual e os próximos 3 meses
        val monthsToDisplay = mutableListOf<String>()
        for (i in 0..3) {
            val monthIndex = (currentMonth + i) % 12
            monthsToDisplay.add(allMonths[monthIndex])
        }

        // Agora, adaptamos o spinner para mostrar apenas esses 4 meses
        val adapter = ArrayAdapter(
            requireContext(),
            android.R.layout.simple_spinner_dropdown_item,
            monthsToDisplay
        )
        binding.spinner.adapter = adapter

        // Definimos o mês selecionado inicialmente como o mês atual
        binding.spinner.setSelection(0)  // O mês atual será o primeiro item

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                // Quando um mês for selecionado, definimos o mês atual e carregamos os dias
                selectedMonth =
                    currentMonth + position + 1 // Ajusta o mês selecionado de acordo com a posição
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
        // Configura o mês no calendário
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH) + 1 // Os meses começam do zero

        // Verifica se o mês selecionado é o mês atual
        if (selectedMonth == currentMonth) {
            // Se for o mês atual, usa o dia atual como o dia selecionado
            selectedDay = currentDay

            val daysToDisplay = mutableListOf<Int>()

            // Adiciona os dias anteriores ao dia atual, se necessário
            when {
                currentDay > 2 -> {
                    daysToDisplay.add(currentDay - 2)
                    daysToDisplay.add(currentDay - 1)
                }

                currentDay == 2 -> {
                    daysToDisplay.add(currentDay - 1)
                }
            }

            // Adiciona os dias a partir do dia atual até o final do mês
            val remainingDays = (currentDay..daysInMonth).toList()
            daysToDisplay.addAll(remainingDays)

            // Atualiza os dias no adaptador
            daysAdapter.updateDays(daysToDisplay)

            // Marca o dia selecionado corretamente (seleciona o dia atual)
            daysAdapter.selectDay(selectedDay)
        } else {
            // Se não for o mês atual, simplesmente carrega todos os dias do mês selecionado
            val days = (1..daysInMonth).toList()
            daysAdapter.updateDays(days)

            // Seleciona o primeiro dia por padrão (ou qualquer outro valor desejado)
            selectedDay = 1
            daysAdapter.selectDay(selectedDay)
        }

        // Carrega as agendas para o dia selecionado
        loadSchedules()
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