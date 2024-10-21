package com.isabellatressino.travely

import android.content.ContentValues
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.functions.FirebaseFunctions
import com.isabellatressino.travely.databinding.ActivityRegisterBinding
import com.isabellatressino.travely.models.User

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding

    private lateinit var senhaConf: String
    lateinit var user: User
    private lateinit var auth: FirebaseAuth

    lateinit var firebase: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        auth = FirebaseAuth.getInstance()

        // inflar layout da activity
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configurar clique do botão de cadastro
        binding.buttonCadastrar.setOnClickListener {
            // obter dados do formulário
            senhaConf = binding.TextSenhaConfirmacao.text.toString().trim()
            user = User(
                name = binding.TextNome.text.toString().trim(),
                cpf = binding.TextCpf.text.toString().trim(),
                phone = binding.TextTelefone.text.toString().trim(),
                email = binding.TextEmail.text.toString().trim(),
                password = binding.TextSenha.text.toString().trim(),
                authID = "", //AuthID será gerado na ProfileActivity
                schedule = null ,
                profile = "",
            )

            // Valida se cada campo do form é válido
            if (isFormValido(senhaConf,user)) {
                // Passar dados do usuário para a ProfileActivity
                val intent = Intent(this, ProfileActivity::class.java).apply {
                    putExtra("userName", user.name)
                    putExtra("userCPF", user.cpf)
                    putExtra("userPhone", user.phone)
                    putExtra("userEmail", user.email)
                    putExtra("userPassword", user.password)
                }
                startActivity(intent)
            }
        }
    }
    // Função que valida todos os campos do formulário
    private fun isFormValido(senhaConf:String, user: User): Boolean{
        if (user.name.isEmpty() || !user.isNameValid()) {
            binding.TextNome.error = "Preencha com um nome válido"
            return false
        } else if (user.cpf.isEmpty() || !user.isCpfValid()) {
            binding.TextCpf.error = "Preencha com um cpf válido"
            return false
        } else if (user.phone.isEmpty() || !user.isPhoneValid()) {
            binding.TextTelefone.error = "Preencha com um telefone válido"
            return false
        } else if (user.email.isBlank() || !user.isEmailValid()) {
            binding.TextEmail.error = "Preencha com um email válido"
            return false
        } else if (user.password.isEmpty() || !user.isPasswordValid()) {
            binding.TextSenha.error = "Preencha com uma senha de pelo menos 6 dígitos e sem espaços"
            return false
        } else if (senhaConf.isEmpty() || senhaConf != user.password) {
            binding.TextSenhaConfirmacao.setError("As senhas devem ser idênticas.")
            return false
        } else {
            return true
        }
    }

    /*
    fun createAuthUser (user: User) {
        auth.createUserWithEmailAndPassword(user.email, user.password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "signInWithCustomToken:success")
                    user.authID = task.result.user?.uid.toString()

                    //addUser(user)

                    // enviar email de verificação para o usuário
                    auth.currentUser?.sendEmailVerification()

                    // deslogar o usuário
                    auth.signOut()

                    val iProfile = Intent(this, ProfileActivity::class.java)
                    iProfile.putExtra("userName", user.name)
                    iProfile.putExtra("userCPF", user.cpf)
                    iProfile.putExtra("userPhone", user.phone)
                    iProfile.putExtra("userAuth", user.authID)
                    //  iProfile.putExtra("userPassword", user.password)
                    startActivity(iProfile)

                } else {
                    Toast.makeText(
                        this, "Falha ao criar autenticação do usuário",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
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
     */
}