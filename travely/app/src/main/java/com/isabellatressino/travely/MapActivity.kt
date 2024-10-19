package com.isabellatressino.travely

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.isabellatressino.travely.databinding.ActivityMapBinding
import com.isabellatressino.travely.models.Place

class MapActivity : AppCompatActivity() {

    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val binding by lazy { ActivityMapBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance("default2")
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            loadPlacesFromFirestore()
            getUserLocation()
            setupMapClickListeners()
        }

        binding.containerInfo.visibility = View.GONE
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        // Obtém a última localização conhecida
        fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
            if (location != null) {
                // Obtém as coordenadas da localização
                val userLocation = LatLng(location.latitude, location.longitude)
                // Move a câmera para a localização do usuário
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                googleMap.addMarker(
                    MarkerOptions()
                        .position(userLocation)
                        .title("Você está aqui")
                        .icon(
                            BitmapHelper.vectorToBitmap(
                                this, R.drawable.person_pin,
                                ContextCompat.getColor(this, R.color.purple_haze)
                            )
                        )
                )
            } else {
                Toast.makeText(this, "Localização não disponível", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getUserLocation()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadPlacesFromFirestore() {
        val placesCollection = firestore.collection("places")

        placesCollection.get()
            .addOnSuccessListener { documents ->
                val places = mutableListOf<Place>()

                for (document in documents) {
                    val name = document.getString("name") ?: ""
                    val address = document.getString("address") ?: ""
                    val description = document.getString("description") ?: ""
                    val type = document.getString("type") ?: ""
                    val rate = document.getDouble("rate") ?: 0.0
                    val businessHours =
                        (document.get("businessHours") as? List<String>)?.toTypedArray()
                    val geopoint = document.getGeoPoint("geopoint")
                    val profiles = (document.get("profiles") as? List<String>)?.toTypedArray()

                    if (geopoint != null) {
                        val place = Place(
                            name,
                            address,
                            description,
                            type,
                            rate,
                            businessHours ?: emptyArray(),
                            geopoint,
                            profiles ?: emptyArray()
                        )
                        places.add(place)
                    }
                }
                addMarkers(places)
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MapActivity,
                    "Não foi possível obter as informações do banco.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun addMarkers(places: List<Place>) {
        places.forEach { place ->
            val marker = place.geopoint.let { geoPoint ->
                val iconResource = when (place.type) {
                    "compra" -> R.drawable.pin_buy
                    "comida" -> R.drawable.pin_food
                    "cultura" -> R.drawable.pin_culture
                    "aventura" -> R.drawable.pin_adventure
                    "negocios" -> R.drawable.pin_business
                    else -> R.drawable.location_pin
                }
                val markerOptions = MarkerOptions()
                    .title(place.name)
                    .snippet(place.address)
                    .position(LatLng(geoPoint.latitude, geoPoint.longitude))
                    .icon(
                        BitmapHelper.vectorToBitmap(
                            this, iconResource,
                            ContextCompat.getColor(this, R.color.purple_haze)
                        )
                    )
                val addedMarker = googleMap.addMarker(markerOptions)
                addedMarker?.tag = place
            }
        }

        googleMap.setOnMarkerClickListener { marker ->
            googleMap.animateCamera(
                CameraUpdateFactory.newLatLng(marker.position),
                500,
                null
            )

            val place = marker.tag as? Place
            place?.let {
                showPlaceInfo(it)
            }

            true
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

    private fun showPlaceInfo(place: Place) {
        binding.tvName.text = place.name
        binding.tvRating.text = place.rate.toString()
        setStars(place.rate)
        binding.tvDescription.text = place.decription

        binding.containerInfo.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    private fun setupMapClickListeners() {
        googleMap.setOnMapClickListener {
            hideInfoView()
        }
    }

    private fun hideInfoView() {
        binding.containerInfo.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.containerInfo.visibility = View.GONE
            }
            .start()
    }

}
