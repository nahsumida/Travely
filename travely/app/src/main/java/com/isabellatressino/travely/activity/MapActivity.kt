package com.isabellatressino.travely.activity

import android.content.pm.PackageManager
import android.location.Location
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.location.Geocoder
import android.net.Uri
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.GeoPoint
import com.isabellatressino.travely.databinding.ActivityMapBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.BitmapHelper
import com.isabellatressino.travely.R
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule


class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var firestore: FirebaseFirestore
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private val cityMarkers = mutableListOf<Marker>()
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private var userMarker: Marker? = null
    private var isInitialLocationSet = false
    private lateinit var auth: FirebaseAuth;

    private val binding by lazy { ActivityMapBinding.inflate(layoutInflater) }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        // Configuração do mapa
        val mapFragment =
            supportFragmentManager.findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync { map ->
            googleMap = map
            loadPlacesFromFirestore()
            setupLocationUpdates()
            setupMapClickListeners()
        }

        with(binding) {
            containerInfo.visibility = View.GONE
            editSearch.setOnClickListener {
                hideInfoView()
            }
            editSearch.setOnEditorActionListener { v, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    val cityName = binding.editSearch.text.toString()
                    searchCity(cityName)
                    true
                } else {
                    false
                }
            }
            btnUserLocation.setOnClickListener { centerMapOnUserLocation() }
        }
    }

    /**
     * Centraliza o mapa na localização do usuário
     */
    private fun centerMapOnUserLocation() {
        if (userMarker != null) {
            val userLocation = userMarker?.position
            userLocation?.let {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(it, 15f), 1000, null)
            }
        } else {
            // Caso ainda não tenha a localização do usuário
            Toast.makeText(this, "Localização do usuário não encontrada", Toast.LENGTH_SHORT).show()
        }
    }

    /**
     * Busca a cidade pelo nome e adiciona um marcador no mapa se achar
     *
     * @param cityName O nome da cidade que se deseja buscar
     */
    private fun searchCity(cityName: String) {
        val geocoder = Geocoder(this)
        val addresses = geocoder.getFromLocationName(cityName, 1)

        for (marker in cityMarkers) {
            marker.remove()
        }
        cityMarkers.clear()

        if (!addresses.isNullOrEmpty()) {
            val address = addresses[0]
            val latLng = LatLng(address.latitude, address.longitude)

            val cityMarker = googleMap.addMarker(MarkerOptions().position(latLng).title(cityName))
            cityMarker?.let { cityMarkers.add(it) }
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 10f))
        } else {
            Toast.makeText(this, "Cidade não encontrada", Toast.LENGTH_SHORT).show()
        }
    }


    /**
     * Configura o mapa para ajustar à localização do usuário
     */
    private fun setupLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    updateUserLocationOnMap(location)
                }
            }
        }

        startLocationUpdates()
    }

    /**
     * Faz uma requisição da localização se a permissão for aceita.
     */
    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                mainLooper
            )
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    /**
     * Para a atualização da localização
     */
    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    /**
     * Atualiza a localização do usuário no mapa e adiciona um marker
     *
     * @param location A localização atual do usuário
     */
    private fun updateUserLocationOnMap(location: Location) {
        val userLocation = LatLng(location.latitude, location.longitude)

        if (userMarker == null) {
            userMarker = googleMap.addMarker(
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
            userMarker?.position = userLocation
        }

        // Verifica se o zoom inicial já foi feito
        if (!isInitialLocationSet) {
            // Configura o zoom inicial uma única vez ao abrir o mapa
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
            isInitialLocationSet = true // Marca que o zoom inicial foi feito
        } else {
            // Move a câmera suavemente para a nova localização do usuário
            //googleMap.animateCamera(CameraUpdateFactory.newLatLng(userLocation), 1000, null)
        }
    }

    /**
     * Função que recupera os dados do usuário e os atribui ao layout
     */
    private fun getUserInfo(callback: (String) -> Unit) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid

        firestore.collection("users").whereEqualTo("authID",uid).get()
            .addOnSuccessListener { documents ->
                val user = documents.firstOrNull()
                if (user != null) {
                    val profile = user.getString("profile") ?: ""

                    if (profile.isNotEmpty()) {
                        callback(profile)
                    }
                } else {
                    Log.w("getUserInfo", "Usuário não encontrado")
                    callback("")
                }
            } .addOnFailureListener {
                Log.e("getUserInfo", "Falha ao fazer requisição")
                Toast.makeText(
                    this, "Falha ao buscar usuário",
                    Toast.LENGTH_SHORT
                ).show()
                callback("")
            }
    }

    /**
     * Carrega os documentos da coleção "places" do Firestore
     */
    private fun loadPlacesFromFirestore() {
        val placesCollection = firestore.collection("places")

        placesCollection.get()
            .addOnSuccessListener { documents ->
                val places = mutableListOf<Place>()

                for (document in documents) {
                    val id = document.getString("id") ?: ""
                    val name = document.getString("name") ?: ""
                    val address = document.getString("address") ?: ""
                    val description = document.getString("description") ?: ""
                    val type = document.getString("type") ?: ""
                    val rate = document.getDouble("rating") ?: 0.0

                    // Verifica se businessHours é um Map e trata corretamente
                    val businessHoursMap =
                        document.get("businessHours") as? Map<String, List<String>> ?: emptyMap()

                    // Converte o Map em um formato que você deseja usar
                    val businessHoursArray = businessHoursMap.map { entry ->
                        entry.key to entry.value.toTypedArray()
                    }.toMap() // Isso cria um Map<String, Array<String>>

                    val geopoint = document.getGeoPoint("geopoint")
                    val profiles = (document.get("profiles") as? List<String>)?.toTypedArray()
                    val picture = document.getString("picture") ?: ""

                    // Extração dos dados do schedule
                    val scheduleMap = document.get("schedule") as? Map<String, Any>

                    // Verifica se o schedule existe e extrai os dados
                    val schedule = if (scheduleMap != null) {
                        val bookingData =
                            scheduleMap["bookingData"] as? Timestamp ?: Timestamp.now()
                        val placeID = scheduleMap["placeID"] as? String ?: ""
                        val compra = scheduleMap["compra"] as? String ?: ""
                        val preco = (scheduleMap["preco"] as? Double ?: 0.0).toFloat()

                        Schedule(bookingData, placeID, compra, preco)
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
                            schedule ?: Schedule(Timestamp.now(), "", "", 0.0f)
                        )
                        places.add(place)
                    }
                }
                getUserInfo { profile ->
                    addMarkers(places, profile)
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this@MapActivity,
                    "Não foi possível obter as informações do banco.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    /**
     * Adiciona os marcadores no mapa
     *
     * @param places The list of places to display.
     */
    private fun addMarkers(places: List<Place>, userProfile: String) {
        places.forEach { place ->
            val marker = place.geopoint.let { geoPoint ->
                val iconResource = if (place.profiles.isNotEmpty()) {
                    for (profile in place.profiles) {
                        if(profile == userProfile)
                            when (profile)  {
                                "compras" -> R.drawable.pin_buy
                                "gastronomico" -> R.drawable.pin_food
                                "cultura" -> R.drawable.pin_culture
                                "aventureiro" -> R.drawable.pin_adventure
                                "negocios" -> R.drawable.pin_business
                                else -> R.drawable.location_pin
                            }
                    }
                    when (place.profiles[0])  {
                        "compras" -> R.drawable.pin_buy
                        "gastronomico" -> R.drawable.pin_food
                        "cultura" -> R.drawable.pin_culture
                        "aventureiro" -> R.drawable.pin_adventure
                        "negocios" -> R.drawable.pin_business
                        else -> R.drawable.location_pin
                    }
                } else {
                    R.drawable.location_pin
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

    /**
     * Exibe as informações do local selecionado
     *
     * @param place O local a ser exibido as informações
     */
    private fun showPlaceInfo(place: Place) {
        binding.tvName.text = place.name
        binding.tvRating.text = place.rate.toString()
        setStars(place.rate)
        binding.tvDescription.text = place.description

        binding.btnSeeMore.setOnClickListener {
            val intent = Intent(this, PlaceInfoActivity::class.java)
            intent.putExtra("PLACE_ID", place.id)
            startActivity(intent)
        }

        binding.btnGo.setOnClickListener {
            openGoogleMaps(place.geopoint)
        }

        binding.containerInfo.apply {
            alpha = 0f
            visibility = View.VISIBLE
            animate()
                .alpha(1f)
                .setDuration(200)
                .start()
        }
    }

    /**
     * Chama evento de clique no mapa, para que o card de informações pare de ser exibido
     */
    private fun setupMapClickListeners() {
        googleMap.setOnMapClickListener {
            hideInfoView()
        }
    }

    /**
     * Esconde o card de informações do local
     */
    private fun hideInfoView() {
        binding.containerInfo.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.containerInfo.visibility = View.GONE
            }
            .start()
    }

    /**
     * Opens Google Maps to navigate to the given location.
     * Abre a navegação do Google Maps do local dado
     *
     * @param geopoint Coordenadas do local
     */
    private fun openGoogleMaps(geopoint: GeoPoint) {
        val latitude = geopoint.latitude
        val longitude = geopoint.longitude

        val uri = "google.navigation:q=$latitude,$longitude"
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
        intent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            // Caso o Google Maps não esteja instalado, abrir no navegador
            val webUri = "https://www.google.com/maps/dir/?api=1&destination=$latitude,$longitude"
            val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUri))
            startActivity(webIntent)
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                startLocationUpdates()
            } else {
                Toast.makeText(this, "Permissão de localização negada", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopLocationUpdates() // Para as atualizações ao sair da Activity
    }

    override fun onMapReady(gM: GoogleMap) {
        googleMap = gM
    }

}
