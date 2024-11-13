package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityQrCodeBinding

class QrCodeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQrCodeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater) // Inicializa o binding
        setContentView(binding.root)

        binding.btncopy.setOnClickListener{
            binding.pixLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            binding.loadingMessage.visibility = View.VISIBLE

            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this,ConfirmActivity::class.java)
                startActivity(intent)
                finish()
            }, 3000)

        }

        binding.btnClose.setOnClickListener{
            val intent = Intent(this,MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}