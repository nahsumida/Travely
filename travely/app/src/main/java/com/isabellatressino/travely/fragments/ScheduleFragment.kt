package com.isabellatressino.travely.fragments

import android.app.DatePickerDialog
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
import com.isabellatressino.travely.dao.UserDao
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

        // Inicializa a UI com o mês e dia atuais
        setupMonthSpinner() // Configura o spinner de meses
        loadDaysOfMonth() // Carrega os dias do mês atual
        setDefaultSelectedDay() // Define o dia atual como selecionado
        loadSchedules() // Carrega os agendamentos para o mês e dia atuais
    }

    private fun setDefaultSelectedDay() {
        // Se o dia atual já foi obtido de Calendar, usa ele como padrão
        val currentDay = Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
        selectedDay = currentDay
        loadSchedules() // Carrega os agendamentos para o dia atual
    }


    private fun setupMonthSpinner() {
        val months = listOf(
            "Janeiro", "Fevereiro", "Março", "Abril", "Maio", "Junho",
            "Julho", "Agosto", "Setembro", "Outubro", "Novembro", "Dezembro"
        )

        val adapter =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, months)
        binding.spinner.adapter = adapter

        // Selecione o mês atual no spinner
        binding.spinner.setSelection(selectedMonth - 1)

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                selectedMonth = position + 1

                // Limpar a seleção do dia quando o mês for alterado
                daysAdapter.clearDaySelection()

                // Limpar os agendamentos antes de carregar os novos
                scheduleAdapter.updateSchedules(emptyList())

                loadDaysOfMonth() // Recarrega os dias do mês
                loadSchedules()   // Carrega os agendamentos para o novo mês
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
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

        // Se o mês e o dia selecionado forem o mês e o dia atual
        if (selectedMonth == currentMonth) {
            // Exibe os dois dias anteriores ao dia atual e depois todos os dias restantes do mês
            val daysToDisplay = mutableListOf<Int>()

            // Adiciona os dois dias anteriores ao dia atual
            if (currentDay > 2) {
                daysToDisplay.add(currentDay - 2) // Adiciona o segundo dia anterior primeiro
                daysToDisplay.add(currentDay - 1) // Adiciona o primeiro dia anterior
            } else if (currentDay == 2) {
                daysToDisplay.add(currentDay - 1) // Adiciona o primeiro dia anterior
            }

            // Adiciona todos os dias restantes do mês
            val remainingDays = (currentDay..daysInMonth).toList()
            daysToDisplay.addAll(remainingDays)

            // Atualiza a lista de dias para exibir os dias anteriores e os dias restantes do mês
            daysAdapter.updateDays(daysToDisplay)

            // **Garantir que apenas o dia 20 do mês atual seja pintado** como "hoje"
            if (currentDay == 20) {
                // Marca o dia 20 como selecionado, para ele ser destacado
                daysAdapter.selectDay(20)
            } else {
                // Marca o dia atual como selecionado
                daysAdapter.selectDay(currentDay)
            }
        } else {
            // Se o mês não for o atual, exibe todos os dias do mês
            val days = (1..daysInMonth).toList()
            daysAdapter.updateDays(days)
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
