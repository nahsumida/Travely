package com.isabellatressino.travely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityMainProfileBinding

class MainProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val tvNome = findViewById<TextView>(R.id.tv_nome)
        val tvNomeCompleto = findViewById<TextView>(R.id.tv_nome_completo)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val tvTipoPerfil = findViewById<TextView>(R.id.tv_tipo_perfil)
        val btnSair = findViewById<Button>(R.id.btn_logout)

        carregarDadosUsuario(tvNome, tvNomeCompleto, tvEmail, tvTipoPerfil)

        btnSair.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }
    }

    private fun carregarDadosUsuario(
        tvNome: TextView,
        tvNomeCompleto: TextView,
        tvEmail: TextView,
        tvTipoPerfil: TextView
    ) {
        val db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            db.collection("users").document(userId)
                .get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val nome = document.getString("nome")
                        val nomeCompleto = document.getString("nomeCompleto") ?: nome
                        val email = currentUser.email
                        val tipoPerfil = document.getString("perfil")

                        tvNome.text = nome
                        tvNomeCompleto.text = nomeCompleto
                        tvEmail.text = email
                        tvTipoPerfil.text = tipoPerfil
                    } else {
                        Log.d("Firestore", "Nenhum documento encontrado")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.w("Firestore", "Erro ao buscar dados: ", exception)
                }
        } else {
            Log.w("Firestore", "Usuário não autenticado")
        }
    }

}
