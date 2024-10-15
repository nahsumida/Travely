package com.isabellatressino.travely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityProfileBinding
import com.isabellatressino.travely.databinding.ActivityRegisterBinding
import com.isabellatressino.travely.models.User

class ProfileActivity : AppCompatActivity() {
    private lateinit var binding: ActivityProfileBinding

    lateinit var firebase: FirebaseFirestore

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflar layout da activity
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("userName")
        val cpf = intent.getStringExtra("userCPF")
        val phone = intent.getStringExtra("userPhone")
        val authID = intent.getStringExtra("userAuthID")

        // criar objeto Pessoa com os dados do formulário
        user = User(
            name = name!!,
            cpf = cpf!!,
            phone = phone!!,
            email = null,
            password = null,
            authID = authID!!,
            schedule = null,
            profile = "",
        );

        binding.btnFood.setOnClickListener {
            user.profile = "gastronomico"
            addUser(user)

            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
        binding.btnAdventure.setOnClickListener {
            user.profile = "aventureiro"
            addUser(user)

            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
        binding.btnRelax.setOnClickListener {
            user.profile = "descanso"
            addUser(user)

            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
        binding.btnShopp.setOnClickListener {
            user.profile = "compras"
            addUser(user)

            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
        binding.btnBusiness.setOnClickListener {
            user.profile = "negocios"
            addUser(user)


            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
        binding.btnCulture.setOnClickListener {
            user.profile = "cultural"
            addUser(user)

            // mockado pra quando tiver mergeado
            /*val iLogin = Intent(this, LoginActivity::class.java)
            startActivity(iLogin)*/
        }
    }

    // metodo para adicionar um user ao firestore
    fun addUser(user: User){
        firebase = FirebaseFirestore.getInstance("default2")

        // Criando um hash map pessoa com os dados do usuario
        val userDoc = hashMapOf(
            "authID" to user.authID,
            "cpf" to user.cpf,
            "name" to user.name,
            "phone" to user.phone,
            "profile" to user.profile,
        )

        // Adicionando a pessoa no firestore
        firebase.collection("users")
            .add(userDoc)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(this, "User cadastrado com sucesso",
                    Toast.LENGTH_SHORT).show()
                Log.d("Cadastro User", "User adicionado com ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao cadastrar User",
                    Toast.LENGTH_SHORT).show()
                Log.w("Erro", "Erro ao adicionar user", e)
            }
    }
}
