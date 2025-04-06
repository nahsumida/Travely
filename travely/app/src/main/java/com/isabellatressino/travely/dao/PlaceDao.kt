package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.storage.FirebaseStorage
import com.isabellatressino.travely.models.Place
import com.isabellatressino.travely.models.Schedule

class PlaceDao {

    private val db = FirebaseFirestore.getInstance()
    private val placesCollection = db.collection("places-v2")

    fun getPlaceById(
        placeId: String,
        onSuccess: (Place?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        placesCollection.document(placeId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val place = document.toPlace()
                    onSuccess(place)
                } else {
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("PlaceDao", "Erro ao buscar place: ${exception.message}")
                onFailure(exception)
            }
    }

    fun getPlaces(onSuccess: (List<Place>) -> Unit, onFailure: (Exception) -> Unit) {
        placesCollection.get()
            .addOnSuccessListener { documents ->
                val places = documents.mapNotNull { it.toPlace() }
                onSuccess(places)
            }
            .addOnFailureListener { exception ->
                Log.w("PlaceDao", "Não foi possível obter as informações do banco.")
                onFailure(exception)
            }
    }

    private fun DocumentSnapshot.toPlace(): Place? {
        val geoPoint = this.getGeoPoint("geopoint") ?: return null

        val businessHoursRaw = this.get("businessHours") as? Map<*, *>
        val businessHoursParsed = businessHoursRaw?.mapNotNull { entry ->
            val day = entry.key as? String ?: return@mapNotNull null
            val hours = entry.value as? Map<*, *>
            val open = hours?.get("open") as? String
            val close = hours?.get("close") as? String

            if (open != null && close != null) {
                day to mapOf("open" to open, "close" to close)
            } else {
                null
            }
        }?.toMap() ?: emptyMap()

        return Place(
            id = this.getString("id") ?: "",
            name = this.getString("name") ?: "",
            address = this.getString("address") ?: "",
            description = this.getString("description") ?: "",
            rating = this.getDouble("rating") ?: 0.0,
            businessHours = businessHoursParsed,
            geopoint = geoPoint,
            keyWords = this.get("keyWords") as? List<String> ?: emptyList(),
            subtypes = (this.get("subtypes") as? List<String>) ?: emptyList(),
            picture = this.getString("picture") ?: ""
        )
    }


    private fun extractScheduleData(document: DocumentSnapshot): List<Schedule> {
        val schedulesList = document.get("schedule") as? List<Map<String, Any>> ?: emptyList()

        return schedulesList.mapNotNull { scheduleMap ->
            try {
                Schedule(
                    placeID = scheduleMap["placeID"] as? String ?: "",
                    availability = (scheduleMap["availability"] as? Long)?.toInt() ?: 0,
                    price = (scheduleMap["price"] as? Double) ?: 0.0,
                    datetime = scheduleMap["datetime"] as? String ?: ""
                )
            } catch (e: Exception) {
                Log.e("PlaceDao", "Erro ao converter schedule: ${e.message}")
                null
            }
        }
    }

    // Função para carregar a URL da imagem
    fun getImageUrl(imageUrl: String, onSuccess: (String) -> Unit, onFailure: (Exception) -> Unit) {
        val storageReference = FirebaseStorage.getInstance().getReferenceFromUrl(imageUrl)
        storageReference.downloadUrl.addOnSuccessListener { uri ->
            onSuccess(uri.toString())
        }.addOnFailureListener { exception ->
            onFailure(exception)
        }
    }

}
