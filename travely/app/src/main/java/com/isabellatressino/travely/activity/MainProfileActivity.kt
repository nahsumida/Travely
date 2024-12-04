package com.isabellatressino.travely.activity

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityMainProfileBinding
import com.isabellatressino.travely.models.User
import java.util.Locale

class MainProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainProfileBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        getUserInfo { user ->
            carregarDadosUsuario(binding.tvName, binding.tvEmail, user!!)
        }

        //logout
        binding.btnLogout.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        //reservas
        binding.btnSchedules.setOnClickListener{
            startActivity(Intent(this,ScheduleActivity::class.java))
        }

        //nav
        binding.navBar.btnHome.setOnClickListener{
            startActivity(Intent(this,MainScreenActivity::class.java))
        }
        binding.navBar.btnLocal.setOnClickListener{
            startActivity(Intent(this,MapActivity::class.java))
        }
        binding.navBar.btnProfile.setOnClickListener{
            startActivity(Intent(this,MainProfileActivity::class.java))
        }
    }
    /**
     * Função que recupera os dados do usuário e os atribui ao layout
     */
    private fun getUserInfo(callback: (User?) -> Unit) {
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        val currentUser = auth.currentUser
        if (currentUser != null) {
            val userId = currentUser.uid
            firestore.collection("users").whereEqualTo("authID", userId).get()
                .addOnSuccessListener { documents ->
                    val user = documents.firstOrNull()
                    if (user != null) {
                        val name = user.getString("name") ?: ""
                        val profile = user.getString("profile") ?: ""
                        val email = currentUser?.email
                        val firstName = formatName(name)

                        val user = User(
                            name = name,
                            cpf = firstName,
                            phone = "",
                            email = email!!,
                            password = "",
                            authID = userId,
                            schedule = null,
                            profile = profile,
                        )
                        callback(user)
                    } else {
                        Log.w("getUserInfo", "Usuário não encontrado")
                        callback(null)
                    }
                }.addOnFailureListener {
                    Log.e("getUserInfo", "Falha ao fazer requisição")
                    Toast.makeText(
                        this, "Falha ao buscar usuário",
                        Toast.LENGTH_SHORT
                    ).show()
                    callback(null)
                }
        }
    }

    private fun carregarDadosUsuario(
        tvNome: TextView,
        tvEmail: TextView,
        user: User
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
                        val nome = user.name
                        val email = currentUser.email

                        tvNome.text = nome
                        tvEmail.text = email

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

    private fun formatName(name: String): String {
        val nameParts = name.split(" ")
        val firstName = nameParts.firstOrNull()?.replaceFirstChar {
            if (it.isLowerCase()) it.titlecase(Locale.getDefault())
            else it.toString()
        }

        val formattedName = "Olá $firstName!"
        return formattedName
    }

}
