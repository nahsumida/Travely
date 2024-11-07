package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityConfirmBinding
import com.isabellatressino.travely.databinding.ActivityMainScreenBinding

class ConfirmActivity : AppCompatActivity() {
    private lateinit var binding : ActivityConfirmBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityConfirmBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //botão menu
        binding.btnMenu.setOnClickListener {
            val intent = Intent(this, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}