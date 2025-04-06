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
import java.time.format.TextStyle
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

    private var year = calendar.get(Calendar.YEAR)
    private var selectedMonth = String.format("%02d", calendar.get(Calendar.MONTH) + 1)
    private var selectedDay = String.format("%02d", calendar.get(Calendar.DAY_OF_MONTH))
    private var selectedTime = ""
    private var selectedDayOfWeek =
        LocalDate.now().dayOfWeek.getDisplayName(TextStyle.SHORT, Locale("pt", "BR"))


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

        loadPlaceById(placeID)

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSchedule.setOnClickListener {
            addSchedule()
        }
    }

    private fun addSchedule() {
        if (selectedTime != "") {
            FirebaseAuth.getInstance().currentUser?.uid?.let { authID ->

                val scheduleDateTime = "$year-$selectedMonth-${selectedDay}T$selectedTime:00Z"

                userDao.addSchedule(
                    authID = authID,
                    placeID = placeID,
                    placeName = place.name,
                    scheduleDateTime = scheduleDateTime,
                    onSuccess = {
                        Toast.makeText(
                            this,
                            "Agendamento realizado com sucesso!",
                            Toast.LENGTH_SHORT
                        ).show()
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

                    val ret = loadAvailableTimes()
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
            tvRating.text = place.rating.toString()
            setStars(place.rating)
            tvDescription.text = place.description
        }

        setPlaceImage(place)
    }

    private fun setPlaceImage(place: Place) {
        val iconResource = getProfileIconResource(place.subtypes)
        binding.imgType.setImageResource(iconResource)

        if (place.picture.isNotEmpty()) {
            loadImageFromStorage(place.picture)
        } else {
            binding.imgPlace.setImageResource(R.drawable.image_unavailable)
        }
    }

    private fun getProfileIconResource(profiles: List<String>): Int {
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
                selectedMonth = String.format("%02d", (currentMonth + position + 1))
                loadDaysOfMonth()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }

    private fun loadDaysOfMonth() {
        calendar.set(Calendar.MONTH, selectedMonth.toInt() - 1)
        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)

        val today = Calendar.getInstance()
        val currentDay = today.get(Calendar.DAY_OF_MONTH)
        val currentMonth = today.get(Calendar.MONTH) + 1

        val daysToDisplay = mutableListOf<Pair<Int, String>>()

        if (selectedMonth.toInt() == currentMonth) {
            selectedDay = String.format("%02d", currentDay)

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
            selectedDay = 1.toString()
        }

        daysAdapter.updateDays(daysToDisplay)
        daysAdapter.selectDay(selectedDay.toInt())
    }

    private fun setupRecyclerViewDays() {
        daysAdapter = DaysAdapter(emptyList()) { day ->
            selectedDay = String.format("%02d", day)
            selectedDayOfWeek = getDayOfWeek(selectedDay.toInt())

            val ret = loadAvailableTimes()
            setupRecyclerViewTime(ret)
        }
        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = daysAdapter
    }

    private fun getDayOfWeek(day: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, selectedMonth.toInt() - 1)
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


    private fun loadAvailableTimes(): List<String> {
        val dayOfWeekMap = mapOf(
            "Seg" to "monday", "Ter" to "tuesday", "Qua" to "wednesday",
            "Qui" to "thursday", "Sex" to "friday", "Sab" to "saturday", "Dom" to "sunday",
            "seg." to "monday", "ter." to "tuesday", "qua." to "wednesday",
            "qui." to "thursday", "sex." to "friday", "sáb." to "saturday", "dom." to "sunday"
        )

        val businessDay = dayOfWeekMap[selectedDayOfWeek]

        val hours = place.businessHours[businessDay]
        val openingTime = hours?.get("open")
        val closingTime = hours?.get("close")

        if (openingTime != null && closingTime != null) {
            val availableTimes = mutableListOf<String>()

            val selectedDate = LocalDate.of(year, selectedMonth.toInt(), selectedDay.toInt())
            val currentDate = LocalDate.now()
            val currentTime = LocalTime.now()

            val times = generateHalfHourIntervals(openingTime, closingTime)
            for (time in times) {
                val parsedTime = LocalTime.parse(time)

                if (selectedDate.isAfter(currentDate) ||
                    (selectedDate.isEqual(currentDate) && parsedTime.isAfter(currentTime))
                ) {
                    availableTimes.add(time)
                }
            }
            return availableTimes
        } else {
            return listOf("Fechado")
        }
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