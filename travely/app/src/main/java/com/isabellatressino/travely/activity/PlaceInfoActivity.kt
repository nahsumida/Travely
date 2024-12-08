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
import BookingManagerClientKT
import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.utils.BookingHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

val tag = "TESTEADAPTER"

class PlaceInfoActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth;

    //private val client = BookingManagerClientKT()
    private lateinit var bookingHelper: BookingHelper;
    private val binding by lazy { ActivityPlaceInfoBinding.inflate(layoutInflater) }
    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val calendar by lazy { Calendar.getInstance() }

    private lateinit var adapterDays: DaysAdapter
    private lateinit var adapterTime: TimeAdapter
    private lateinit var place: Place
    private lateinit var placeID: String

    private var scheduleTime = ""
    private var scheduleDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupSpinner()
        setupRecyclerViewDays()

        val placeIdIntent = intent.getStringExtra("PLACE_ID")
        if (placeIdIntent != null) {
            showLoading(true)
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

    private fun addSchedule() {
        auth = FirebaseAuth.getInstance()

        val authID = auth.currentUser
        val schedule = "${scheduleDate.slice(0..9)}T$scheduleTime:00Z"

        if (place.type == "reserva"){
            if (authID != null) {
                bookingHelper = BookingHelper()
                bookingHelper.requestBooking(this, authID.uid, placeID , schedule, 1, "reserva") {
                    list ->
                    if (list.size > 2) {
                        val placeID = list[1]
                        val date = list[2]
                        val intent = Intent(this@PlaceInfoActivity, MainScreenActivity::class.java)
                        Log.d("BookingSuccess", "Reserva realizada com sucesso. PlaceID: $placeID, Date: $date")
                        Toast.makeText(this, "Agendamento registrado na sua sessão de 'Reservas'", Toast.LENGTH_LONG).show()
                        Handler(Looper.getMainLooper()).postDelayed({
                            startActivity(intent)
                            finish()
                        }, 3000)
                    } else {
                        Log.e("BookingError", "Erro ao obter informações da reserva")
                    }
                } // pre
            }
        } else {
            val intent = Intent(this@PlaceInfoActivity, ConfirmActivity::class.java)
            startActivity(intent)
        }


        Log.d("TESTEADAPTER", schedule)
    }

    private fun updateButtonState() {
        binding.btnSchedule.isEnabled =
            scheduleDate.isNotEmpty() && scheduleTime.isNotEmpty()
    }

    private fun showCardSchedule(): String {
        val scheduling = "${scheduleDate.slice(0..9)}T$scheduleTime:00Z"
        val schedulesList = place.schedule
        var quantity = 1
        var totalPrice = 0.0

        for (schedule in schedulesList) {
            if (schedule.datetime == scheduling) {
                val basePrice = schedule.price
                val fee = basePrice * 0.1

                binding.card.tvPrice.text =
                    "R$ ${String.format("%.2f", basePrice).replace(".", ",")} +" +
                            " ( R$ ${String.format("%.2f", fee).replace(".", ",")} taxa)"
                binding.card.tvQuantity.text = quantity.toString()
                binding.card.tvTotalPrice.text =
                    "R$ ${String.format(" % .2f", (basePrice + fee) * quantity).replace(".", ", ")}"

                binding.card.btnMore.setOnClickListener {
                    if (quantity < schedule.availability) {
                        quantity++
                        binding.card.tvQuantity.text = quantity.toString()
                        binding.card.tvTotalPrice.text =
                            "R$ ${
                                String.format(" % .2f", (basePrice + fee) * quantity)
                                    .replace(".", ", ")
                            }"

                        binding.card.btnLess.isEnabled = true
                    }

                    binding.card.btnMore.isEnabled = quantity < schedule.availability
                }

                binding.card.btnLess.setOnClickListener {
                    if (quantity > 1) {
                        quantity--
                        binding.card.tvQuantity.text = quantity.toString()
                        binding.card.tvTotalPrice.text =
                            "R$ ${
                                String.format(" % .2f", (basePrice + fee) * quantity)
                                    .replace(".", ",")
                            }"
                        binding.card.btnMore.isEnabled = true
                    }

                    binding.card.btnLess.isEnabled = quantity > 1
                }

                binding.card.btnBuy.setOnClickListener {
                   // addSchedule()
                    val intent = Intent(this, QrCodeActivity::class.java)
                    intent.putExtra("placeID", placeID)
                    intent.putExtra("date", scheduling)
                    intent.putExtra("amount", quantity)
                    startActivity(intent)
                }

                totalPrice = (basePrice + fee) * quantity
            }
        }

        /*val intent = Intent(this, QrCodeActivity::class.java)
         intent.putExtra("placeID", placeID)
         intent.putExtra("date", scheduling)
         intent.putExtra("amount", quantity)
        startActivity(intent)*/

        // Aqui retorno os dados em formato de string, tanto para salvar a schedule para o usuário,
        // quanto para passar no intent para a proxima tela
        return "placeId:$placeID,price:$totalPrice,quantity(avaiability?):$quantity,datetime:$scheduling"
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
                adapterDays.resetSelection()
                scheduleDate = ""
                updateButtonState()
                adapterDays.updateDays(getDaysOfMonth(month, year))
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupRecyclerViewDays() {
        adapterDays = DaysAdapter(mutableListOf())
            .apply {
                onDaySelected = { date ->
                    adapterTime.resetSelection()
                    scheduleTime = ""
                    val ret = loadAvailableTimes(date)
                    setupRecyclerViewTime(ret)
                    scheduleDate = date
                    updateButtonState()
                }
            }

        binding.recyclerviewDays.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerviewDays.adapter = adapterDays
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
            scheduleTime = time
            updateButtonState()
            if (place.type == "compra") {
                binding.card.cardMain.visibility = View.VISIBLE
                showCardSchedule()
            }
        }
    }

    private fun getCurrentDateInCustomFormat(): String {
        val today = LocalDate.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd-eee", Locale("pt", "BR"))
        return today.format(formatter)
    }

    private fun loadAvailableTimes(date: String): List<String> {
        val (year, month, day, weekDay) = date.split("-")
        val dayOfWeekMap = mapOf(
            "seg." to "Mon", "ter." to "Tue", "qua." to "Wed",
            "qui." to "Thu", "sex." to "Fri", "sáb." to "Sat", "dom." to "Sun"
        )

        val availableTimes = mutableListOf<String>()

        if (place.type == "reserva") {
            val dayOfWeekFormated = dayOfWeekMap[weekDay]
            val businessHours = place.businessHours[dayOfWeekFormated]
            if (businessHours != null) {
                if (businessHours.size >= 2) {
                    val times = generateHalfHourIntervals(businessHours[0], businessHours[1])
                    for (time in times) availableTimes.add(time)
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

    private fun loadPlaceById(idPlace: String) {
        firestore.collection("places").document(idPlace).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val loadedPlace = extractPlaceData(document)
                    if (loadedPlace != null) {
                        // Armazena o place carregado na variável de instância
                        place = loadedPlace
                        showPlaceInfos(place)

                        val ret = loadAvailableTimes(getCurrentDateInCustomFormat())
                        setupRecyclerViewTime(ret)

                        if (place.type == "reserva") {
                            binding.btnSchedule.visibility = View.VISIBLE
                        }

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

        val businessHoursArray = businessHoursMap.map { entry ->
            entry.key to entry.value.toTypedArray()
        }.toMap()

        val geopoint = document.getGeoPoint("geopoint")
        val profiles = (document.get("profiles") as? List<String>)?.toTypedArray()
        val picture = document.getString("picture") ?: ""

        // Extração dos dados do schedule
        val schedule = extractScheduleData(document) ?: emptyList()

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
                schedule
            )
        } else {
            null
        }
    }

    private fun extractScheduleData(document: DocumentSnapshot): List<Schedule> {
        // Acessando o campo 'schedule' e garantindo que é uma lista de maps
        val schedulesList =
            document.get("schedule") as? List<Map<String, Any>> ?: return emptyList()

        return schedulesList.map { scheduleMap ->
            val placeID = placeID
            val availability = (scheduleMap["availability"] as? Number)?.toInt() ?: 0
            val price = (scheduleMap["price"] as? Number)?.toDouble() ?: 0.0
            val datetime = scheduleMap["datetime"] as? String ?: ""

            Schedule(placeID, availability, price, datetime)
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
