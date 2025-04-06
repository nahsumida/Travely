package com.isabellatressino.travely

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.isabellatressino.travely.databinding.ActivitySingUpBinding

class SingUpActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySingUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener{
            val iRegister = Intent(this, QuestionsActivity::class.java)
            startActivity(iRegister)
        }
    }
}