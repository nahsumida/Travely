package com.isabellatressino.travely

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.databinding.ActivityProfileBinding
import com.isabellatressino.travely.models.User
import java.lang.Exception

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
        auth = FirebaseAuth.getInstance()

        // criar objeto Pessoa com os dados do formulário
        user = User(
            name = name!!,
            cpf = cpf!!,
            phone = phone!!,
            email = email!!,
            password = password!!,
            authID = "", //AuthID será gerado após sucesso no registro no db
            schedule = null,
            profile = "",
        )

        binding.btnFood.setOnClickListener {
            user.profile = "gastronomico"

            addUser(user)
        }
        binding.btnAdventure.setOnClickListener {
            user.profile = "aventureiro"

            addUser(user)
        }
        binding.btnRelax.setOnClickListener {
            user.profile = "descanso"

            addUser(user)
        }
        binding.btnShopp.setOnClickListener {
            user.profile = "compras"

            addUser(user)
        }
        binding.btnBusiness.setOnClickListener {
            user.profile = "negocios"

            addUser(user)
        }
        binding.btnCulture.setOnClickListener {
            user.profile = "cultural"

            addUser(user)
        }
    }

    // metodo para adicionar um user ao firestore
    private fun addUser(user: User) {
        firebase = FirebaseFirestore.getInstance("default2")

        // Criando um hash map pessoa com os dados do usuario
        val userDoc = hashMapOf(
            "cpf" to user.cpf,
            "name" to user.name,
            "phone" to user.phone,
            "profile" to user.profile,
        )

        // Adicionando  usuário no firestore
        firebase.collection("users").add(userDoc)
            .addOnSuccessListener { documentReference ->
                Toast.makeText(
                    this, "User cadastrado com sucesso",
                    Toast.LENGTH_SHORT
                ).show()
                Log.d("Cadastro User", "User adicionado com ID: ${documentReference.id}")
                createAuthUser(user, documentReference.id)
                //val iLogin = Intent(this@ProfileActivity, LoginActivity::class.java)
                //startActivity(iLogin)
            }
            .addOnFailureListener { e ->
                Toast.makeText(
                    this, "Falha ao cadastrar User",
                    Toast.LENGTH_SHORT
                ).show()
                Log.w("Erro", "Erro ao adicionar user no Firestore", e)
            }
    }

    private fun createAuthUser(user: User, docId: String) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCustomToken:success")
                    firebase = FirebaseFirestore.getInstance("default2")
                    val firebaseUser = auth.currentUser
                    val uid = firebaseUser?.uid ?: ""
                    user.authID = uid

                    // enviar email de verificação para o usuário
                    firebaseUser?.sendEmailVerification()
                        ?.addOnCompleteListener { sendEmailTask ->
                            if (sendEmailTask.isSuccessful) {
                                updateUID(user, docId)
                                Toast.makeText(
                                    this, "Verifique seu e-mail para seguir com " +
                                            "cadastro", Toast.LENGTH_LONG
                                ).show()
                            } else {
                                // Falha ao enviar email
                                Log.e("EmailVerification", "Erro ao enviar email de verificação")
                                // Remove o usuário do db se o envio do email falhar
                                firebase.collection("users").document(docId).delete()
                            }
                        }
                } else {
                    // Caso falha ao criar Auth User, remove usuário do db
                    firebase.collection("users").document(docId).delete()
                    Log.e(
                        "createAuthUser",
                        ": Falha criar auth. Firestore revertido",
                        task.exception
                    )
                }
            }
    }

    // Função para atualizar campo 'authID' do usuário no Firestore
    private fun updateUID(user: User, docId: String) {
        firebase = FirebaseFirestore.getInstance("default2")
        val userUpdate = hashMapOf(
            "authID" to user.authID
        )

        // Atualizar campo authID com o id do usuário criado no Auth
        firebase.collection("users").document(docId)
            .update(userUpdate as Map<String, Any>)
            .addOnSuccessListener {
                Log.d("updateUID","Sucesso ao atualizar campo authID.")
                // deslogar o usuário
                auth.signOut()

                // Redirecionar para a tela de Login
                val iLogin = Intent(this@ProfileActivity, LoginActivity::class.java)
                startActivity(iLogin)
            }
            .addOnFailureListener { e: Exception ->
                Log.w("updateUID", "Erro ao atualizar authID no db", e)
            }

    }
}