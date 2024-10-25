package com.isabellatressino.travely

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityScheduleBinding
import com.isabellatressino.travely.models.Schedule
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

val TAG = "TESTE FIREBASE SCHEDULE"

class ScheduleActivity : AppCompatActivity() {

    private val binding by lazy { ActivityScheduleBinding.inflate(layoutInflater) }
    private val auth by lazy { FirebaseAuth.getInstance() }
    private lateinit var adapter: ScheduleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var schedulesList: MutableList<Schedule>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()

        recyclerView = binding.recyclerviewSchedules
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        schedulesList = mutableListOf() // Inicialize a lista vazia
        adapter = ScheduleAdapter(schedulesList)

        recyclerView.adapter = adapter

        loadUserSchedules()
    }

    private fun loadUserSchedules() {
        firestore
            .collection("users")
            .document(/*auth.currentUser?.uid.toString()*/"FKmB1tPOczNURj0WGrHk")
            .get()
            .addOnSuccessListener { document ->
                if (document != null && document.exists()) {
                    val schedulesData = document.get("schedule") as? List<Map<String, Any>>

                    if (schedulesData != null) {
                        schedulesList.clear()

                        for (scheduleData in schedulesData) {
                            // Verifique se os campos existem e são do tipo correto
                            val bookingData = scheduleData["bookingDate"] as? Timestamp
                            val placeID = scheduleData["placeID"] as? String ?: ""
                            val type = scheduleData["type"] as? String ?: ""
                            val preco = if (scheduleData["preco"] is Number) {
                                (scheduleData["preco"] as Number).toFloat()
                            } else {
                                0.0f
                            }

                            val precoteste = scheduleData["preco"]
                            Log.d(TAG, "Preco original recuperado do Firestore: $precoteste")
                            Log.d(TAG, "Preco convertido para Float: $preco")


                            // Cria um objeto Schedule e o adiciona à lista
                            if (bookingData != null) {
                                val schedule = Schedule(bookingData, placeID, type, preco)
                                schedulesList.add(schedule)
                            } else {
                                Log.d(TAG, "Missing bookingData in schedule: $scheduleData")
                            }
                        }

                        // Notifica o adapter sobre mudanças na lista
                        adapter.notifyDataSetChanged()
                    } else {
                        Log.d(TAG, "No schedules data found")
                    }
                } else {
                    Log.d(TAG, "Document does not exist")
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting document: ", e)
            }
    }

}
