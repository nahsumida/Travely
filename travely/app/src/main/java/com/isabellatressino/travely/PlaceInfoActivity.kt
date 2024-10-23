package com.isabellatressino.travely

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.databinding.ActivityPlaceInfoBinding
import com.isabellatressino.travely.models.Place
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.isabellatressino.travely.models.Schedule


class PlaceInfoActivity : AppCompatActivity() {

    private val binding by lazy { ActivityPlaceInfoBinding.inflate(layoutInflater) }
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance("default2")
        storage = FirebaseStorage.getInstance()

        val placeId = intent.getStringExtra("PLACE_ID")
        if (placeId != null) {
            Log.d("IsabellaMAria", placeId)
            showLoading(true)
            loadPlaceById(placeId)
        }

        binding.btnBack.setOnClickListener {
            finish()
        }

    }

    private fun loadPlaceById(idPlace: String) {
        val placesCollection = firestore.collection("places")

        placesCollection.document(idPlace).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val address = document.getString("address") ?: ""
                    val description = document.getString("description") ?: ""
                    val type = document.getString("type") ?: ""
                    val rate = document.getDouble("rate") ?: 0.0

                    val businessHoursMap =
                        document.get("businessHours") as? Map<String, List<String>> ?: emptyMap()

                    val businessHoursArray = businessHoursMap.map { entry ->
                        entry.key to entry.value.toTypedArray()
                    }.toMap()

                    val geopoint = document.getGeoPoint("geopoint")
                    val profiles = (document.get("profiles") as? List<String>)?.toTypedArray()
                    val picture = document.getString("picture") ?: ""

                    // Extração dos dados do schedule
                    val scheduleMap = document.get("schedule") as? Map<String, Any>

                    // Verifica se o schedule existe e extrai os dados
                    val schedule = if (scheduleMap != null) {
                        val bookingData =
                            scheduleMap["bookingData"] as? Timestamp ?: Timestamp.now()
                        //val placeID = scheduleMap["placeID"] as? String ?: ""
                        val compra = scheduleMap["compra"] as? String ?: ""
                        val preco = (scheduleMap["preco"] as? Double ?: 0.0).toFloat()

                        Schedule(bookingData, compra, preco)
                    } else {
                        null
                    }

                    if (geopoint != null) {
                        val place = Place(
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
                            schedule ?: Schedule(Timestamp.now(), "", 0.0f)
                        )

                        showPlaceInfos(place)
                    }
                } else {
                    Toast.makeText(this, "Documento não encontrado", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { exception ->
                Toast.makeText(
                    this,
                    "Erro ao carregar documento: ${exception.message}",
                    Toast.LENGTH_SHORT
                ).show()
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

        val iconResource = if (place.profiles.isNotEmpty()) {
            when (place.profiles[0]) {
                "compras" -> R.drawable.profileshopp
                "gastronomico" -> R.drawable.profilefood
                "cultura" -> R.drawable.profileculture
                "aventureiro" -> R.drawable.profileadventure
                "negocios" -> R.drawable.profilebusiness
                else -> R.drawable.profile_unavailable
            }
        } else {
            R.drawable.profile_unavailable
        }

        binding.imgType.setImageResource(iconResource)

        if (place.picture.isNotEmpty()) {
            val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(place.picture)
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
        } else {
            binding.imgPlace.setImageResource(R.drawable.image_unavailable)
            showLoading(false)
        }

        if (place.isOpen()) {
            binding.tvStatus.text = "Aberto"
            binding.tvOpenClosed.text = "Fecha às ${place.getCloseTime()}"
            binding.tvStatus.setTextColor(Color.parseColor("#9BB550"))
        } else {
            binding.tvStatus.text = "Fechado"
            binding.tvOpenClosed.text = "Abre ${place.getNextOpenTime()}"
            binding.tvStatus.setTextColor(Color.parseColor("#F44336"))
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

        for (i in 1..fullStars) {
            val star = ImageView(this)
            star.setImageResource(filledStar)
            val params = LinearLayout.LayoutParams(36, 36)
            star.layoutParams = params
            starLayout.addView(star)
        }

        if (decimalPart >= 0.5) {
            val star = ImageView(this)
            star.setImageResource(halfStar)
            val params = LinearLayout.LayoutParams(36, 36)
            star.layoutParams = params
            starLayout.addView(star)
        }

        val remainingStars = maxStars - fullStars - if (decimalPart >= 0.5) 1 else 0
        for (i in 1..remainingStars) {
            val star = ImageView(this)
            star.setImageResource(emptyStar)
            val params = LinearLayout.LayoutParams(36, 36)
            star.layoutParams = params
            starLayout.addView(star)
        }
    }

    private fun showLoading(isLoading: Boolean) {
        if (isLoading) {
            with(binding) {
                progressBar.visibility = View.VISIBLE
                tvLoading.visibility = View.VISIBLE
                btnBack.visibility = View.GONE
                imgType.visibility = View.GONE
                cvImage.visibility = View.GONE
                cvInfos.visibility = View.GONE
                btnSchedule.visibility = View.GONE
            }
        } else {
            with(binding) {
                progressBar.visibility = View.GONE
                tvLoading.visibility = View.GONE
                btnBack.visibility = View.VISIBLE
                imgType.visibility = View.VISIBLE
                cvImage.visibility = View.VISIBLE
                cvInfos.visibility = View.VISIBLE
                btnSchedule.visibility = View.VISIBLE
            }
        }
    }

}