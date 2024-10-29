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


        val tvNome = findViewById<TextView>(R.id.tv_nome)
        val tvNomeCompleto = findViewById<TextView>(R.id.tv_nome_completo)
        val tvEmail = findViewById<TextView>(R.id.tv_email)
        val tvTipoPerfil = findViewById<TextView>(R.id.tv_tipo_perfil)
        val btnSair = findViewById<Button>(R.id.btn_logout)
        val btnHome = findViewById<ImageButton>(R.id.btn_home)
        val btnLocal = findViewById<ImageButton>(R.id.btn_local)
        val btnPerfil = findViewById<ImageButton>(R.id.btn_profile)
        val btnReservas = findViewById<Button>(R.id.btn_reservas)

        getUserInfo { user ->
            carregarDadosUsuario(tvNome, tvNomeCompleto, tvEmail, tvTipoPerfil, user!!)
        }

        //logout
        btnSair.setOnClickListener{
            FirebaseAuth.getInstance().signOut()

            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            startActivity(intent)
            finish()
        }

        //reservas
        btnReservas.setOnClickListener{
            startActivity(Intent(this,ScheduleActivity::class.java))
        }

        //nav
        btnHome.setOnClickListener{
            startActivity(Intent(this,MainScreenActivity::class.java))
        }
        btnLocal.setOnClickListener{
            startActivity(Intent(this,MapActivity::class.java))
        }
        btnPerfil.setOnClickListener{
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
                            authID = userId, //AuthID será gerado após sucesso no registro no db
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
        tvNomeCompleto: TextView,
        tvEmail: TextView,
        tvTipoPerfil: TextView,
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
                        val nome = user.cpf
                        val nomeCompleto = user.name
                        val email = currentUser.email
                        val tipoPerfil = user.profile

                        tvNome.text = nome
                        tvNomeCompleto.text = "Nome completo: $nomeCompleto"
                        tvEmail.text = "Email: $email"
                        tvTipoPerfil.text = "Perfil: $tipoPerfil"
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
