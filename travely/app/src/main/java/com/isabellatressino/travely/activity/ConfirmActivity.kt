package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityConfirmBinding
import com.isabellatressino.travely.databinding.RecyclerviewScheduleBinding
import com.isabellatressino.travely.models.Schedule
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

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

        //botão menu
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

    }


}