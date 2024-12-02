package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityQrCodeBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import BookingManagerClientKT
import com.google.firebase.auth.FirebaseAuth

class QrCodeActivity : AppCompatActivity() {
    private val client = BookingManagerClientKT()

    private lateinit var binding: ActivityQrCodeBinding

    private lateinit var auth: FirebaseAuth;

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater) // Inicializa o binding
        setContentView(binding.root)

        binding.btncopy.setOnClickListener{
            binding.pixLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            binding.loadingMessage.visibility = View.VISIBLE

            auth = FirebaseAuth.getInstance()

            val placeID = intent.getStringExtra("placeID")
            val date = intent.getStringExtra("date")
            var amount = intent.getStringExtra("amount")

            val authUser = auth.currentUser
            if (authUser != null) {
                val authID = authUser.uid
                if (placeID != null) {
                    if (date != null) {
                        if (amount == null){
                            amount = "1"
                        }
                        sendBookingRequest(authID, placeID , date, amount.toInt())
                    }
                }
            }


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

    private fun sendBookingRequest(authID: String, placeID: String, datetime: String, amount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = client.sendBookingRequest(authID, placeID, datetime, amount)
            val list: List<String> = listOf(*result.split(",").toTypedArray())

            withContext(Dispatchers.Main) {
                // Exibe o Toast e navega para a próxima Activity se a resposta for sucesso
                if (list.contains("SUCESSO")) {
                    Toast.makeText(this@QrCodeActivity, "Reserva realizada com sucesso!", Toast.LENGTH_LONG).show()

                    // Navega para a próxima Activity
                    val intent = Intent(this@QrCodeActivity, ConfirmActivity::class.java)
                    startActivity(intent)
                    intent.putExtra("placeID", list[1])
                    intent.putExtra("date", list[2])
                    intent.putExtra("price", list[3])

                    finish() // Opcional: fecha a Activity atual

                } else {
                    // Exibe um Toast com a mensagem de erro
                    Toast.makeText(this@QrCodeActivity, result, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}