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
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.isabellatressino.travely.utils.BookingHelper

class QrCodeActivity : AppCompatActivity() {
    // private val client = BookingManagerClientKT()
    private lateinit var binding: ActivityQrCodeBinding
    private lateinit var auth: FirebaseAuth;
    private lateinit var bookingHelper: BookingHelper

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        binding = ActivityQrCodeBinding.inflate(layoutInflater) // Inicializa o binding
        setContentView(binding.root)

        bookingHelper = BookingHelper()
        binding.btncopy.setOnClickListener {
            binding.pixLayout.visibility = View.GONE
            binding.progressBar.visibility = View.VISIBLE
            binding.loadingMessage.visibility = View.VISIBLE

            auth = FirebaseAuth.getInstance()

            val placeID = intent.getStringExtra("placeID")
            val date = intent.getStringExtra("date")
            val amount = intent.getIntExtra("amount", 1)

            val authUser = auth.currentUser
            if (authUser != null) {
                val authID = authUser.uid
                if (placeID != null) {
                    if (date != null) {
                        bookingHelper.requestBooking(
                            this, authID, placeID, date, amount, "compra",
                            onSuccess = { list ->
                                val intent =
                                    Intent(this@QrCodeActivity, ConfirmActivity::class.java)
/*
// unsused
                                if (list.size > 3) {
                                    intent.putExtra("placeID", list[1])
                                    intent.putExtra("date", list[2])
                                    intent.putExtra("price", list[3])
                                }
*/
                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(intent)
                                    finish()
                                }, 3000)
                            },
                            onError = { list ->
                                Toast.makeText(
                                    this, "Falha ao finalizar compra, horario indisponivel",
                                    Toast.LENGTH_SHORT
                                ).show()
                                val intent =
                                    Intent(this@QrCodeActivity, PlaceInfoActivity::class.java)
                                intent.putExtra("PLACE_ID", placeID)
                                startActivity(intent)

                                Handler(Looper.getMainLooper()).postDelayed({
                                    startActivity(intent)
                                    finish()
                                }, 2000)
                            },
                        )
                    }
                }
            }

            /*
            Handler(Looper.getMainLooper()).postDelayed({
                val intent = Intent(this,ConfirmActivity::class.java)
                startActivity(intent)
                finish()
            }, 3000)*/
        }

        binding.btnClose.setOnClickListener{
            val intent = Intent(this,MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    /*
    private fun requestBooking(authID: String, placeID: String, datetime: String, amount: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            val result = client.sendBookingRequest(authID, placeID, datetime, amount)
            val list: List<String> = listOf(*result.split(",").toTypedArray())

            withContext(Dispatchers.Main) {
                // Exibe o Toast e navega para a próxima Activity se a resposta for sucesso
                if (list.contains("SUCESSO")) {
                    //Toast.makeText(this@QrCodeActivity, "Reserva realizada com sucesso!", Toast.LENGTH_LONG).show()
                    Log.d("Resposta do Servidor:","SUCESSO")
                    // Criar intent para ir para tela de confirmação da compra
                    val intent = Intent(this@QrCodeActivity, ConfirmActivity::class.java)

                    if (list.size > 3) {
                        intent.putExtra("placeID", list[1])
                        intent.putExtra("date", list[2])
                        intent.putExtra("price", list[3])
                    }

                    startActivity(intent)
                    finish() // fecha a Activity atual

                } else {
                    // Exibe um Toast com a mensagem de erro
                    //Toast.makeText(this@QrCodeActivity, result, Toast.LENGTH_LONG).show()
                    Log.e("Resposta do Servidor:",result)
                }
            }
        }
    }*/
}