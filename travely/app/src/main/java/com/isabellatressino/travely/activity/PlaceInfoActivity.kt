package com.isabellatressino.travely.activity

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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.R
import com.isabellatressino.travely.adapters.DaysAdapter
import com.isabellatressino.travely.adapters.TimeAdapter
import com.isabellatressino.travely.databinding.ActivityPlaceInfoBinding
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

val TAG2 = "ADAPTER TIME"

class PlaceInfoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPlaceInfoBinding.inflate(layoutInflater) }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val calendar by lazy { Calendar.getInstance() }

    private lateinit var adapterDays: DaysAdapter
    private lateinit var adapterTime: TimeAdapter
    private lateinit var place: Place
    private lateinit var placeType: String

    private var scheduleDate: String? = null
    private var scheduleTime: String? = null

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupRecyclerViewDays()
        setupSpinner()

        val placeId = intent.getStringExtra("PLACE_ID")
        if (placeId != null) {
            showLoading(true)
            loadPlaceById(placeId)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

        binding.btnSchedule.setOnClickListener {
            addSchedule()
        }

        binding.btnSchedule.isEnabled = false
    }

    private fun addSchedule() {
        Toast.makeText(this, "confirmado, prox activity", Toast.LENGTH_SHORT).show()
    }


    private fun updateDateSchedule(day: String, month: String, year: String) {
        scheduleDate = "$year-$month-$day"
        updateButtonState()
    }

    private fun updateTimeSchedule(time: String) {
        scheduleTime = time
        updateButtonState()
    }

    private fun updateButtonState() {
        binding.btnSchedule.isEnabled = !scheduleDate.isNullOrEmpty() && !scheduleTime.isNullOrEmpty() && scheduleTime != "Fechado"
    }

    private fun setupRecyclerViewDays() {
        adapterDays = DaysAdapter(mutableListOf()).apply {
            onDaySelected = { data ->
                val (day, weekDay, month, year) = data.split("-")
                updateDateSchedule(day, month, year)
                setupRecyclerViewTime(weekDay.replace(".", ""))
            }
        }

        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = adapterDays
    }

    private fun setupRecyclerViewTime(selectedDayOfWeek: String) {
        binding.recyclerviewTime.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        val dayOfWeekMap = mapOf(
            "seg" to "Mon", "ter" to "Tue", "qua" to "Wed",
            "qui" to "Thu", "sex" to "Fri", "sáb" to "Sat", "dom" to "Sun"
        )

        val formattedDay = dayOfWeekMap[selectedDayOfWeek] ?: ""
        val businessHours = place.businessHours[formattedDay]

        val timeList = businessHours?.takeIf { it.size >= 2 }?.let {
            generateTimeSlots(it[0], it[1])
        } ?: listOf("Fechado")

        if (::adapterTime.isInitialized) {
            adapterTime.updateTimeList(timeList)
        } else {
            adapterTime = TimeAdapter(timeList.toMutableList())
            binding.recyclerviewTime.adapter = adapterTime
        }

        adapterTime.onTimeSelect = { time ->
            updateTimeSchedule(time)
        }
    }

    private fun setupSpinner() {
        val spinnerItems = getNextSixMonths()
        binding.spinner.adapter =
            ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems).apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
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
                selectedMonth = month
                selectedYear = year
                adapterDays.resetSelection()
                scheduleDate = null
                updateButtonState()
                adapterDays.updateDays(getDaysOfMonth(month, year))
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
                    // Adiciona o dia, dia da semana e o mês
                    days.add(String.format("%02d-%s-%02d-%04d", day, dayOfWeek, month + 1, year))
                }
            } else {
                days.add(String.format("%02d-%s-%02d-%04d", day, dayOfWeek, month + 1, year))
            }
        }
        return days
    }

    private fun getNextSixMonths(): List<String> {
        return (0..5).map {
            SimpleDateFormat("MMMM yyyy", Locale("pt", "BR")).format(calendar.time).uppercase()
                .also {
                    calendar.add(Calendar.MONTH, 1)
                }
        }
    }

    private fun generateTimeSlots(openingTime: String, closingTime: String): List<String> {
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

    private fun loadPlaceById(idPlace: String) {
        firestore.collection("places").document(idPlace).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loadedPlace = extractPlaceData(document)
                    if (loadedPlace != null) {
                        // Armazena o place carregado na variável de instância
                        placeType = loadedPlace.type
                        place = loadedPlace
                        showPlaceInfos(place)

                        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
                        val todayDayOfWeek = dayOfWeekFormat.format(calendar.time)

                        setupRecyclerViewTime(todayDayOfWeek.replace(".", ""))
                    }
                } else {
                    showError("Documento não encontrado")
                }
            }
            .addOnFailureListener { exception ->
                showError("Erro ao carregar documento: ${exception.message}")
            }
    }


    private fun extractPlaceData(document: DocumentSnapshot): Place? {
        val id = document.getString("id") ?: ""
        val name = document.getString("name") ?: ""
        val address = document.getString("address") ?: ""
        val description = document.getString("description") ?: ""
        val type = document.getString("type") ?: ""
        val rate = document.getDouble("rating") ?: 0.0

        val businessHoursMap =
            document.get("businessHours") as? Map<String, List<String>> ?: emptyMap()
        val businessHoursArray =
            businessHoursMap.map { entry -> entry.key to entry.value.toTypedArray() }.toMap()

        val geopoint = document.getGeoPoint("geopoint")
        val profiles = (document.get("profiles") as? List<String>)?.toTypedArray()
        val picture = document.getString("picture") ?: ""

        val schedule = extractScheduleData(document)

        return if (geopoint != null) {
            Place(
                id,
                name,
                address,
                description,
                type,
                rate,
                businessHoursArray,
                geopoint,
                profiles ?: emptyArray(),
                picture,
                schedule ?: Schedule(Timestamp.now(), "", "", 0.0f)
            )
        } else {
            null
        }
    }

    private fun extractScheduleData(document: DocumentSnapshot): Schedule? {
        val scheduleMap = document.get("schedule") as? Map<String, Any>
        return scheduleMap?.let {
            val bookingData = it["bookingData"] as? Timestamp ?: Timestamp.now()
            val placeID = it["placeID"] as? String ?: ""
            val compra = it["compra"] as? String ?: ""
            val preco = (it["preco"] as? Double ?: 0.0).toFloat()

            Schedule(bookingData, placeID, compra, preco)
        }
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
            showLoading(false)
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
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            Glide.with(this)
                .load(uri)
                .into(binding.imgPlace)
            showLoading(false)
        }.addOnFailureListener { exception ->
            Log.e("ImageLoadError", "Error loading image: ${exception.message}")
            binding.imgPlace.setImageResource(R.drawable.image_unavailable)
            showLoading(false)
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
