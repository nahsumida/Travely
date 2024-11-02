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
import androidx.recyclerview.widget.RecyclerView
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
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    private lateinit var adapterDays: DaysAdapter
    private lateinit var adapterMenu: ArrayAdapter<String>
    private lateinit var adapterTime: TimeAdapter

    private lateinit var recyclerViewDays: RecyclerView
    private lateinit var recyclerViewTime: RecyclerView

    private lateinit var daysList: MutableList<String>
    private lateinit var timeList: MutableList<String>
    private lateinit var spinnerItems: MutableList<String>

    private lateinit var place: Place

    private var selectedMonth: Int = 0
    private var selectedYear: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        initializeFirebase()
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

    }

    private fun initializeFirebase() {
        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
    }

    private fun setupRecyclerViewDays() {
        recyclerViewDays = binding.recyclerviewDays
        recyclerViewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        daysList = mutableListOf()
        adapterDays = DaysAdapter(daysList)

        adapterDays.onDaySelected = { selectedDayOfWeek ->
            // Chama setupRecyclerViewTime para atualizar os horários com o dia selecionado
            setupRecyclerViewTime(selectedDayOfWeek.replace(".", ""))
            //Log.d(TAG2,"${selectedDayOfWeek.replace(".", "")}")
        }


        recyclerViewDays.adapter = adapterDays
    }


    private fun setupRecyclerViewTime(selectedDayOfWeek: String) {
        recyclerViewTime = binding.recyclerviewTime
        recyclerViewTime.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        Log.d(TAG2, "dia selecionado $selectedDayOfWeek")

        val selectedDayOfWeekFormated = when (selectedDayOfWeek) {
            "seg" -> "Mon"
            "ter" -> "Tue"
            "qua" -> "Wed"
            "qui" -> "Thu"
            "sex" -> "Fri"
            "sáb" -> "Sat"
            "dom" -> "Sun"
            else -> ""
        }

        Log.d(TAG2, "dia formatado $selectedDayOfWeekFormated")

        val businessHours = place.businessHours[selectedDayOfWeekFormated]

        val timeList = if (businessHours != null && businessHours.size >= 2) {
            val openingTime = businessHours[0]
            val closingTime = businessHours[1]
            generateTimeSlots(openingTime, closingTime)
        } else {
            mutableListOf("fechado")
        }

        if (::adapterTime.isInitialized) {
            // Atualiza a lista de horários no adaptador existente
            adapterTime.updateTimeList(timeList)
        } else {
            // Inicializa o adaptador se ele ainda não foi criado
            adapterTime = TimeAdapter(timeList.toMutableList())
            recyclerViewTime.adapter = adapterTime
        }
    }

    private fun setupSpinner() {
        spinnerItems = getNextSixMonths()
        adapterMenu = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerItems).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        binding.spinner.adapter = adapterMenu

        binding.spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                Log.d("DaysUpdate", "$selectedItem")

                val (month, year) = parseSelectedMonth(selectedItem)
                selectedMonth = month
                selectedYear = year
                Log.d("DaysUpdate", "$selectedMonth , $selectedYear")

                // Gerar os dias do mês selecionado
                val daysOfMonth = getDaysOfMonth(selectedMonth, selectedYear)
                // Atualizar o adapter com os dias do mês selecionado
                adapterDays.updateDays(daysOfMonth)
                //Log.d("DaysUpdate", "Dias atualizados: $daysOfMonth")
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun parseSelectedMonth(selectedItem: String): Pair<Int, Int> {
        val parts = selectedItem.split(" ")
        val monthName = parts[0]
        val year = parts[1].toInt()

        val month = when (monthName) {
            "JANEIRO" -> 0
            "FEVEREIRO" -> 1
            "MARÇO" -> 2
            "ABRIL" -> 3
            "MAIO" -> 4
            "JUNHO" -> 5
            "JULHO" -> 6
            "AGOSTO" -> 7
            "SETEMBRO" -> 8
            "OUTUBRO" -> 9
            "NOVEMBRO" -> 10
            "DEZEMBRO" -> 11
            else -> 0
        }
        return Pair(month, year)
    }

    private fun getDaysOfMonth(month: Int, year: Int): List<String> {
        val days = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        calendar.set(year, month, 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))

        for (day in 1..daysInMonth) {
            calendar.set(year, month, day)
            val dayOfWeek = dayOfWeekFormat.format(calendar.time)

            // Adiciona o dia, dia da semana e o mês
            days.add(String.format("%02d-%s-%02d", day, dayOfWeek, month + 1))
        }

        return days
    }

    private fun getNextSixMonths(): MutableList<String> {
        val months = mutableListOf<String>()
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMMM yyyy", Locale("pt", "BR"))

        for (i in 0 until 6) {
            months.add(dateFormat.format(calendar.time).uppercase(Locale.getDefault()))
            calendar.add(Calendar.MONTH, 1)
        }
        return months
    }

    private fun generateTimeSlots(openingTime: String?, closingTime: String?): List<String> {
        if (openingTime == null || closingTime == null) return emptyList() // Retorna uma lista vazia se algum for nulo

        val timeSlots = mutableListOf<String>()
        val dateFormat = SimpleDateFormat("HH:mm", Locale.getDefault())

        val openingCalendar = Calendar.getInstance().apply {
            time = dateFormat.parse(openingTime)
        }

        val closingCalendar = Calendar.getInstance().apply {
            time = dateFormat.parse(closingTime)
        }

        while (openingCalendar.before(closingCalendar) || openingCalendar == closingCalendar) {
            timeSlots.add(dateFormat.format(openingCalendar.time))
            openingCalendar.add(Calendar.MINUTE, 30)
        }
        return timeSlots
    }


    private fun loadPlaceById(idPlace: String) {
        firestore.collection("places").document(idPlace).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loadedPlace = extractPlaceData(document)
                    if (loadedPlace != null) {
                        // Armazena o place carregado na variável de instância
                        place = loadedPlace
                        showPlaceInfos(place)

                        val calendar = Calendar.getInstance()
                        val dayOfWeekFormat = SimpleDateFormat("EEE", Locale("pt", "BR"))
                        val todayDayOfWeek = dayOfWeekFormat.format(calendar.time)

                        Log.d(TAG2, "Hoje é: $todayDayOfWeek")

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
        with(binding) {
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
            tvLoading.visibility = if (isLoading) View.VISIBLE else View.GONE
            btnBack.visibility = if (isLoading) View.GONE else View.VISIBLE
            imgType.visibility = if (isLoading) View.GONE else View.VISIBLE
            cvImage.visibility = if (isLoading) View.GONE else View.VISIBLE
            cvInfos.visibility = if (isLoading) View.GONE else View.VISIBLE
        }
    }

    private fun showError(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        showLoading(false)
    }
}
