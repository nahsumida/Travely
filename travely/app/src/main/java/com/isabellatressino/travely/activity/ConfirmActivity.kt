package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityConfirmBinding
import com.isabellatressino.travely.databinding.RecyclerviewScheduleBinding
import com.isabellatressino.travely.models.Schedule

class ConfirmActivity : AppCompatActivity() {
    private lateinit var binding : ActivityConfirmBinding

    lateinit var firebase: FirebaseFirestore
    private lateinit var auth: FirebaseAuth;
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val placeID = intent.getStringExtra("placeID")
        val date = intent.getStringExtra("date")
        var price = intent.getStringExtra("price")

        //botão menu
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.card.tvPlaceName.text = placeID
        binding.card.tvScheduleTime.text = date
        //binding.card.tvScheduleQuantity.text = it.availability.toString()
        binding.card.tvSchedulePrice.text = "R$${price}"
/*
        loadUserSchedules { schedules ->
            val schedule = schedules.firstOrNull()
            schedule?.let {
                binding.card.tvPlaceName.text = it.placeID
                //binding.card.tvScheduleTime.text = it.type
                binding.card.tvScheduleQuantity.text = it.availability.toString()
                binding.card.tvSchedulePrice.text = "R$${it.price}"
            }
        }
*/

    }

    private fun loadUserSchedules(callback: (List<Schedule>) -> Unit) {
        val firebaseUser = auth.currentUser
        val uid = firebaseUser?.uid
        firestore
            .collection("users")
            .whereEqualTo("authID", uid)
            .get()
            .addOnSuccessListener { documents ->
                val user = documents.firstOrNull()
                if (user != null) {
                    val schedules = extractScheduleData(user) ?: emptyList()
                    callback(schedules)
                } else {
                    Log.d(TAG, "Document does not exist")
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                Log.e(TAG, "Error getting document: ", e)
                callback(emptyList())
            }
    }

    private fun extractScheduleData(document: DocumentSnapshot): List<Schedule> {
        val schedulesList =
            document.get("schedule") as? List<Map<String, Any>> ?: return emptyList()

        return schedulesList.map { scheduleMap ->
            val placeID = (scheduleMap["placeId"]) as? String ?: ""
            val availability = (scheduleMap["amount"] as? Number)?.toInt() ?: 0
            val price = (scheduleMap["price"] as? Number)?.toDouble() ?: 0.0
            val datetime = scheduleMap["datetime"] as? String ?: ""

            Schedule(placeID, availability, price, datetime)
        }
    }



}