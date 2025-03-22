package com.isabellatressino.travely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.adapters.DaysAdapter
import com.isabellatressino.travely.adapters.TimeAdapter
import com.isabellatressino.travely.dao.PlaceDao
import com.isabellatressino.travely.dao.UserDao
import com.isabellatressino.travely.databinding.ActivityPlaceInfoBinding
import com.isabellatressino.travely.models.Place
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class PlaceInfoActivity : AppCompatActivity() {
    private val binding by lazy { ActivityPlaceInfoBinding.inflate(layoutInflater) }
    private val placeDao by lazy { PlaceDao() }
    private val userDao by lazy { UserDao() }
    private val calendar by lazy { Calendar.getInstance() }

    private lateinit var daysAdapter: DaysAdapter
    private lateinit var adapterTime: TimeAdapter
    private lateinit var place: Place
    private lateinit var placeID: String

    private var selectedMonth = calendar.get(Calendar.MONTH) + 1
    private var selectedDay = calendar.get(Calendar.DAY_OF_MONTH)
    private var selectedTime = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupMonthSpinner()
        setupRecyclerViewDays()

        val placeIdIntent = intent.getStringExtra("PLACE_ID")
        if (placeIdIntent != null) {
            loadPlaceById(placeIdIntent)
            placeID = placeIdIntent
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSchedule.setOnClickListener {
            addSchedule()
        }


    }

    private fun formatDateTime(scheduleDate: String, scheduleTime: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy")
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(scheduleDate, inputFormatter)

        val formattedTime = scheduleTime.padStart(5, '0')

        return "${date.format(outputFormatter)}T$formattedTime:00Z"
    }

    private fun addSchedule() {
        if (selectedTime != "") {
            val formattedDate = String.format(
                "%02d/%02d/%d",
                selectedDay,
                selectedMonth,
                Calendar.getInstance().get(Calendar.YEAR)
            )

            Log.d("TestePlaceInfo", "agendamento: ${formatDateTime(formattedDate, selectedTime)}")

            FirebaseAuth.getInstance().currentUser?.uid?.let {
                userDao.addSchedule(
                    authID = it,
                    placeID = placeID,
                    scheduleDateTime = formatDateTime(formattedDate, selectedTime),
                    onSuccess = {
                        Toast.makeText(
                            this,
                            "Agendamento realizado com sucesso!",
                            Toast.LENGTH_SHORT
                        )
                            .show()
                        finish()
                    },
                    onFailure = { exception ->
                        Toast.makeText(
                            this,
                            "Erro ao agendar: ${exception.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("ScheduleError", "Erro ao adicionar agendamento", exception)
                    }
                )
            }

        }
    }

    private fun loadPlaceById(idPlace: String) {
        placeDao.getPlaceById(
            idPlace,
            onSuccess = { itPlace ->
                if (itPlace != null) {
                    place = itPlace
                    showPlaceInfos(place)

                    val ret = loadAvailableTimes(getCurrentDateInCustomFormat())
                    setupRecyclerViewTime(ret)
                } else {
                    showError("Documento não encontrado")
                }
            },
            onFailure = { exception ->
                showError("Erro ao carregar documento: ${exception.message}")
            }
        )
    }

    private fun showPlaceInfos(place: Place) {
        with(binding) {
            tvName.text = place.name
            tvAddress.text = place.address
            tvRating.text = place.rate.toString()
            setStars(place.rate)
            tvDescription.text = place.description
        }

        setPlaceImage(place)
    }

    private fun setPlaceImage(place: Place) {
        val iconResource = getProfileIconResource(place.profiles)
        binding.imgType.setImageResource(iconResource)

        if (place.picture.isNotEmpty()) {
            loadImageFromStorage(place.picture)
        } else {
            binding.imgPlace.setImageResource(R.drawable.image_unavailable)
        }
    }

    private fun getProfileIconResource(profiles: Array<String>): Int {
        return if (profiles.isNotEmpty()) {
            when (profiles[0]) {
                "compras" -> R.drawable.profileshopp
                "gastronomico" -> R.drawable.profilefood
                "cultural" -> R.drawable.profileculture
                "aventureiro" -> R.drawable.profileadventure
                "negocios" -> R.drawable.profilebusiness
                "descanso" -> R.drawable.profilerelax
                else -> R.drawable.profile_unavailable
            }
        } else {
            R.drawable.profile_unavailable
        }
    }

    private fun loadImageFromStorage(imageUrl: String) {
        placeDao.getImageUrl(
            imageUrl,
            onSuccess = { url ->
                Glide.with(this)
                    .load(url)
                    .into(binding.imgPlace)
                showLoading(false)
            },
            onFailure = { exception ->
                Log.e("ImageLoadError", "Error loading image: ${exception.message}")
                binding.imgPlace.setImageResource(R.drawable.image_unavailable)
                showLoading(false)
            }
        )
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
            this,
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
            //loadAvailableTimes(day.toString())
        }
        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = daysAdapter
    }

    private fun loadDaysOfMonth() {
        calendar.set(Calendar.MONTH, selectedMonth - 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH) + 1

        val daysToDisplay = mutableListOf<Pair<Int, String>>()

        if (selectedMonth == currentMonth) {
            selectedDay = currentDay

            when {
                currentDay > 2 -> {
                    daysToDisplay.add(Pair(currentDay - 2, getDayOfWeek(currentDay - 2)))
                    daysToDisplay.add(Pair(currentDay - 1, getDayOfWeek(currentDay - 1)))
                }

                currentDay == 2 -> {
                    daysToDisplay.add(Pair(currentDay - 1, getDayOfWeek(currentDay - 1)))
                }
            }

            val remainingDays = (currentDay..daysInMonth).toList()
            remainingDays.forEach { day ->
                daysToDisplay.add(Pair(day, getDayOfWeek(day)))
            }
        } else {
            for (day in 1..daysInMonth) {
                daysToDisplay.add(Pair(day, getDayOfWeek(day)))
            }
            selectedDay = 1
        }

        daysAdapter.updateDays(daysToDisplay)
        daysAdapter.selectDay(selectedDay)
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

    private fun setupRecyclerViewTime(timeList: List<String>) {
        binding.recyclerviewTime.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        if (::adapterTime.isInitialized) {
            adapterTime.updateTimeList(timeList)
        } else {
            adapterTime = TimeAdapter(timeList.toMutableList())
            binding.recyclerviewTime.adapter = adapterTime
        }

        adapterTime.onTimeSelected = { time ->
            selectedTime = time
            Log.d("PlaceInfoActivity", "$selectedDay/$selectedMonth $selectedTime")
        }
    }

    private fun getCurrentDateInCustomFormat(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-eee", Locale("pt", "BR"))
        return today.format(formatter)
    }

    /*
  *
private fun loadAvailableTimes(date: String): List<String> {




      if (place.type == "reserva") {
          val dayOfWeekFormated = dayOfWeekMap[weekDay]
          val businessHours = place.businessHours[dayOfWeekFormated]
          if (businessHours != null) {
              if (businessHours.size >= 2) {
                  val times = generateHalfHourIntervals(businessHours[0], businessHours[1])

                  for (time in times) {
                      if (selectedDate.isAfter(currentDate) || (selectedDate.isEqual(currentDate) && LocalTime.parse(time) > currentTime)) {
                          availableTimes.add(time)
                      }
                  }
              } else {
                  availableTimes.add("Fechado")
              }
          }
      } else if (place.type == "compra") {
          val thisDate = "$year-$month-$day"
          val schedulesList = place.schedule
          for (schedule in schedulesList) {
              val (scheduleDate, scheduleTime) = schedule.datetime.split("T")
              if (thisDate == scheduleDate) {
                  availableTimes.add(scheduleTime.slice(0..4))
              }
          }
          if (availableTimes.isEmpty()) {
              availableTimes.add("Nenhum horário disponível")
              binding.card.cardMain.visibility = View.GONE
          }
      } else {
          availableTimes.add("Informação indisponível")
      }
      return availableTimes
  }

  private fun generateHalfHourIntervals(openingTime: String, closingTime: String): List<String> {
      val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
      val openCal = Calendar.getInstance().apply { time = dateFormat.parse(openingTime) }
      val closeCal = Calendar.getInstance().apply { time = dateFormat.parse(closingTime) }

      return buildList {
          while (openCal.before(closeCal) || openCal == closeCal) {
              add(dateFormat.format(openCal.time))
              openCal.add(Calendar.MINUTE, 30)
          }
      }
  }
  * */

    private fun loadAvailableTimes(date: String): List<String> {
        val (year, month, day, weekDay) = date.split("-")

        val selectedDate = LocalDate.of(year.toInt(), month.toInt(), day.toInt())

        // Mapeamento correto de PT-BR para EN
        val dayOfWeekMap = mapOf(
            "seg." to "Mon", "ter." to "Tue", "qua." to "Wed",
            "qui." to "Thu", "sex." to "Fri", "sáb." to "Sat", "dom." to "Sun"
        )

        // Pegamos o nome do dia em português no formato "seg.", "ter.", etc.
        val dayOfWeekFormatted =
            selectedDate.format(DateTimeFormatter.ofPattern("E", Locale("pt", "BR"))).lowercase()

        // Convertendo para o formato correto do JSON
        val businessDay = dayOfWeekMap[dayOfWeekFormatted] ?: return listOf("Fechado")

        val availableTimes = mutableListOf<String>()
        val currentDate = LocalDate.now()
        val currentTime = LocalTime.now()

        // Obtendo os horários de funcionamento do JSON
        val businessHours = place.businessHours[businessDay]

        if (businessHours != null && businessHours.size >= 2) {
            val openingTime = businessHours[0]
            val closingTime = businessHours[1]

            val times = generateHalfHourIntervals(openingTime, closingTime)

            for (time in times) {
                val parsedTime = LocalTime.parse(time)

                if (selectedDate.isAfter(currentDate) ||
                    (selectedDate.isEqual(currentDate) && parsedTime.isAfter(currentTime))
                ) {
                    availableTimes.add(time)
                }
            }
        } else {
            availableTimes.add("Fechado")
        }

        return availableTimes
    }

    private fun generateHalfHourIntervals(openingTime: String, closingTime: String): List<String> {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        val openCal = Calendar.getInstance().apply { time = dateFormat.parse(openingTime) }
        val closeCal = Calendar.getInstance().apply { time = dateFormat.parse(closingTime) }

        return buildList {
            while (openCal.before(closeCal) || openCal == closeCal) {
                add(dateFormat.format(openCal.time))
                openCal.add(Calendar.MINUTE, 30)
            }
        }
    }

    private fun setStars(rating: Double, maxStars: Int = 5) {
        val starLayout = findViewById<LinearLayout>(R.id.starLayout)
        starLayout.removeAllViews()

        val filledStar = R.drawable.star_filled
        val halfStar = R.drawable.star_half
        val emptyStar = R.drawable.star_empty

        val fullStars = rating.toInt()
        val decimalPart = rating - fullStars

        repeat(fullStars) {
            starLayout.addView(createStarImageView(filledStar))
        }

        if (decimalPart >= 0.5) {
            starLayout.addView(createStarImageView(halfStar))
        }

        val remainingStars = maxStars - fullStars - if (decimalPart >= 0.5) 1 else 0
        repeat(remainingStars) {
            starLayout.addView(createStarImageView(emptyStar))
        }
    }

    private fun createStarImageView(resource: Int): ImageView {
        return ImageView(this).apply {
            setImageResource(resource)
            layoutParams = LinearLayout.LayoutParams(36, 36)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        binding.layoutProgressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showLoading(false)
    }
}