package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.R
import com.isabellatressino.travely.adapters.SuggestionsAdapter
import com.isabellatressino.travely.databinding.ActivityMainScreenBinding
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule
import java.util.Locale

class MainScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainScreenBinding.inflate(layoutInflater) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var places: MutableList<Place>
    private lateinit var adapter: SuggestionsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage
    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()
        auth = FirebaseAuth.getInstance()

        recyclerView = binding.recyclerViewSuggestions
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        places = mutableListOf()
        adapter = SuggestionsAdapter(places)

        recyclerView.adapter = adapter

        getUserInfo { profile ->
            loadPlacesFromFirestore(profile)
        }

        binding.cardViewMap.setOnClickListener {
            startActivity(Intent(this, MapActivity::class.java))
        }

        binding.imgCalendar.setOnClickListener {
            startActivity(Intent(this, ScheduleActivity::class.java))
        }

        binding.imgUser.setOnClickListener {
            startActivity(Intent(this, MainProfileActivity::class.java))
        }
    }

    /**
     * Função que recupera os dados do usuário e os atribui ao layout
     */
    private fun getUserInfo(callback: (String) -> Unit) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid
        val tvName = binding.tvName
        val tvProfile = binding.tvProfile

        firestore.collection("users").whereEqualTo("authID",uid).get()
            .addOnSuccessListener { documents ->
                val user = documents.firstOrNull()
                if (user != null) {
                    val name = user.getString("name") ?: ""
                    val profile = user.getString("profile") ?: ""

                    if (name.isNotEmpty()) {
                        tvName.text = formatName(name)
                        when (profile) {
                            "compras" -> tvProfile.text = "Turista de Compras"
                            "gastronomico" -> tvProfile.text = "Turista Gastronômico"
                            "cultura" -> tvProfile.text = "Turista Cultural"
                            "aventureiro" -> tvProfile.text ="Turista Aventureiro"
                            "negocios" -> tvProfile.text ="Turista de Negócios"
                            else ->  tvProfile.text = "Turista $profile"
                        }
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
    private fun loadPlacesFromFirestore(userProfile: String) {
        val placesCollection = firestore.collection("places")
        placesCollection.whereArrayContains("profiles", userProfile).get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
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
                        // Limitar quantidade de locais sugeridos ao usuário
                        if (places.size < 6 ){
                            places.add(place)
                        }
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Não foi possível obter as informações do banco.",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }

    private fun formatName(name: String): String {
        val nameParts = name.split(" ")
        val firstName = nameParts.firstOrNull()?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }
        val lastName = nameParts.lastOrNull()?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }

        // Concatena o primeiro e o último nome
        val formattedName = listOfNotNull(firstName, lastName).joinToString(" ")
        return formattedName
    }
}

/**
 * .icon(
 *                             BitmapHelper.vectorToBitmap(
 *                                 this, R.drawable.person_pin,
 *                                 ContextCompat.getColor(this, R.color.purple_haze)
 *                             )
 */