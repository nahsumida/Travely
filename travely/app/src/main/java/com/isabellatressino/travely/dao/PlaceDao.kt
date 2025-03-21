package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule

class PlaceDao {

    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places")

    fun getPlaceById(
        placeId: String,
        onSuccess: (Place?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        placesCollection.document(placeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val place = document.getGeoPoint("geopoint")?.let {
                        Place(
                            id = document.getString("id") ?: "",
                            name = document.getString("name") ?: "",
                            address = document.getString("address") ?: "",
                            description = document.getString("description") ?: "",
                            type = document.getString("type") ?: "",
                            rate = document.getDouble("rate") ?: 0.0,
                            businessHours = document.get("businessHours") as? Map<String, Array<String>>
                                ?: emptyMap(),
                            geopoint = it,
                            profiles = document.get("profiles") as? Array<String> ?: emptyArray(),
                            picture = document.getString("picture") ?: "",
                            schedule = document.get("schedule") as? List<Schedule>
                                ?: emptyList()
                        )
                    }
                    onSuccess(place)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Erro ao buscar place: ${exception.message}")
                onFailure(exception)
            }
    }

}
