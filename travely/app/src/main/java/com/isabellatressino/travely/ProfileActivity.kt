package com.isabellatressino.travely

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
    private lateinit var auth: FirebaseAuth;

    private lateinit var user: User

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflar layout da activity
        binding = ActivityProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val name = intent.getStringExtra("userName")
        val cpf = intent.getStringExtra("userCPF")
        val phone = intent.getStringExtra("userPhone")
        val email = intent.getStringExtra("userEmail")
        val password = intent.getStringExtra("userPassword")

        // inicializar as instancias do firebase auth e functions
        auth = FirebaseAuth.getInstance();

        // criar objeto Pessoa com os dados do formulário
        user = User(
            name = name!!,
            cpf = cpf!!,
            phone = phone!!,
            email = email!!,
            password = password!!,
            authID = "",
            schedule = null,
            profile = "",
        );

        binding.btnFood.setOnClickListener {
            user.profile = "gastronomico"

            isNewUser(user)
        }
        binding.btnAdventure.setOnClickListener {
            user.profile = "aventureiro"

            isNewUser(user)
        }
        binding.btnRelax.setOnClickListener {
            user.profile = "descanso"

            isNewUser(user)
        }
        binding.btnShopp.setOnClickListener {
            user.profile = "compras"

            createAuthUser(user)
        }
        binding.btnBusiness.setOnClickListener {
            user.profile = "negocios"

            isNewUser(user)
        }
        binding.btnCulture.setOnClickListener {
            user.profile = "cultural"

            isNewUser(user)
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

                val iLogin = Intent(this, LoginActivity::class.java)
                startActivity(iLogin)
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Falha ao cadastrar User",
                    Toast.LENGTH_SHORT).show()
                Log.w("Erro", "Erro ao adicionar user", e)
            }
    }

    fun createAuthUser (user: User){
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCustomToken:success")
                    user.authID = task.result.user?.uid.toString()

                    addUser(user)

                    // enviar email de verificação para o usuário
                    auth.currentUser?.sendEmailVerification()

                    // deslogar o usuário
                    auth.signOut()
                } else {
                    Toast.makeText(
                        this, "Falha ao criar autenticação do usuário",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }

    fun isNewUser(user: User){
        // criar usuário com email e senha
        auth.fetchSignInMethodsForEmail(user.email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // O usuário existe
                    Toast.makeText(
                        this, "O seu email já está vinculado a uma conta",
                        Toast.LENGTH_SHORT
                    ).show()
                     Handler(Looper.getMainLooper()).postDelayed({
                         //mockado pra quando tiver mergeado
                         val iLogin = Intent(this, LoginActivity::class.java)
                         startActivity(iLogin)
                         finish()
                     }, 2000L)

                    // deslogar o usuário
                    auth.signOut()
                } else {
                    createAuthUser(user)
                }
            }
    }
}
