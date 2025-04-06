package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.models.Schedule

class ScheduleDao {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    fun getSchedulesByUser(
        authId: String,
        onSuccess: (List<Schedule>) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.whereEqualTo("authID", authId).get()
            .addOnSuccessListener { documents ->
                val firstDocument = documents.firstOrNull()

                if (firstDocument != null) {
                    val schedulesList = mutableListOf<Schedule>()

                    val schedulesArray = firstDocument.get("schedule") as? List<Map<String, Any>>

                    schedulesArray?.forEach { scheduleData ->
                        val schedule = Schedule(
                            id = scheduleData["uuid"] as? String ?: "",
                            date = scheduleData["date"] as? String ?: "",
                            placeID = scheduleData["placeID"] as? String ?: "",
                            placeName = scheduleData["placeName"] as? String ?: "",
                            price = (scheduleData["price"] as? Number)?.toDouble() ?: 0.0,
                            amount = (scheduleData["amount"] as? Number)?.toInt() ?: 0
                        )
                        schedulesList.add(schedule)
                    }

                    onSuccess(schedulesList)
                } else {
                    onSuccess(emptyList())
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Erro ao buscar agendamentos", exception)
                onFailure(exception)
            }
    }


}