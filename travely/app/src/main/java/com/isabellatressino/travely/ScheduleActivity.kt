package com.isabellatressino.travely

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityScheduleBinding
import com.isabellatressino.travely.models.Schedule

class ScheduleActivity : AppCompatActivity() {

    private val binding by lazy { ActivityScheduleBinding.inflate(layoutInflater) }
    private lateinit var adapter: ScheduleAdapter
    private lateinit var recyclerView: RecyclerView
    private lateinit var schedules: MutableList<Schedule>
    private lateinit var firestore: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        firestore = FirebaseFirestore.getInstance("default2")

        recyclerView = binding.recyclerviewSchedules
        recyclerView.layoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        schedules = mutableListOf(
           // Schedule("10/10/2025","sddsd","compra",125.00f)
        )
        adapter = ScheduleAdapter(schedules)

        recyclerView.adapter = adapter

    }
}