package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.models.Schedule
import com.isabellatressino.travely.models.User
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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
                            placeID = scheduleData["placeID"] as? String ?: "",
                            availability = scheduleData["amount"] as? Int ?: 0,
                            price = (scheduleData["price"] as? Number)?.toDouble() ?: 0.0,
                            datetime = scheduleData["datetime"] as? String ?: ""
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