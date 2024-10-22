package com.isabellatressino.travely

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.libraries.places.api.Places
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.databinding.ActivityMainScreenBinding
import com.isabellatressino.travely.models.Place

class MainScreenActivity : AppCompatActivity() {

    private val binding by lazy { ActivityMainScreenBinding.inflate(layoutInflater) }

    private lateinit var recyclerView: RecyclerView
    private lateinit var places: MutableList<Place>
    private lateinit var adapter: SuggestionsAdapter
    private lateinit var firestore: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance("default2")
        storage = FirebaseStorage.getInstance()

        recyclerView = binding.recyclerViewSuggestions
        recyclerView.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        places = mutableListOf()
        adapter = SuggestionsAdapter(places)

        recyclerView.adapter = adapter

        loadPlacesFromFirestore()

    }

    private fun loadPlacesFromFirestore() {
        val placesCollection = firestore.collection("places")

        placesCollection.get()
            .addOnSuccessListener { documents ->

                for (document in documents) {
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
                            picture
                        )
                        places.add(place)
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
}