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

    private lateinit var auth: FirebaseAuth;

    lateinit var senhaConf: String;
    lateinit var user: User;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflar layout da activity
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // inicializar as instancias do firebase auth e functions
        auth = FirebaseAuth.getInstance();

        // configurar clique do botão de cadastro
        binding.buttonCadastrar.setOnClickListener {
            // obter dados do formulário
            senhaConf = binding.TextSenhaConfirmacao.text.toString().trim();

            // criar objeto Pessoa com os dados do formulário
            user = User(
                name = binding.TextNome.text.toString().trim(),
                cpf = binding.TextCpf.text.toString().trim(),
                phone = binding.TextTelefone.text.toString().trim(),
                email = binding.TextEmail.text.toString().trim(),
                password = binding.TextSenha.text.toString().trim(),
                authID = "",
                //schedule = ,
                profile = "",
            );


            // validar campos do formulário
            if (user.name.isEmpty() || !user.isNameValid()) {
                binding.TextNome.setError("Preencha com um nome válido")
            } else if (user.cpf.isEmpty() || !user.isCpfValid()) {
                binding.TextCpf.setError("Preencha com um cpf válido")
            } else if (user.phone.isEmpty() || !user.isPhoneValid()) {
                binding.TextTelefone.setError("Preencha com um telefone válido")
            } else if (user.password.isEmpty() || !user.isPasswordValid()) {
                binding.TextSenha.setError("Preencha com uma senha de pelo menos 6 digitos")
            } else if (user.email.isEmpty() && !user.isEmailValid()) {
                binding.TextEmail.setError("Preencha com um email válido")
            } else if (senhaConf.isEmpty() || senhaConf != user.password) {
                binding.TextSenhaConfirmacao.setError("Preencha a confimação igual a senha")
            } else {
                // criar usuário com email e senha
                auth.createUserWithEmailAndPassword(user.email, user.password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            Log.d(ContentValues.TAG, "signInWithCustomToken:success")
                            user.authID = task.result.user?.uid.toString()

                            // adicionar pessoa ao firestore
                            addUser(user)

                            // enviar email de verificação para o usuário
                            auth.currentUser?.sendEmailVerification()

                            // deslogar o usuário
                            auth.signOut()

                            val iProfile = Intent(this, ProfileActivity::class.java)
                            startActivity(iProfile)
                        } else {
                            Toast.makeText(
                                this, "Cadastro falhou",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
            }
        }
    }


    // metodo para adicionar um user ao firestore
    fun addUser(user: User){
        lateinit var firebase: FirebaseFirestore;
        firebase = FirebaseFirestore.getInstance()

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
                Log.d("CadastroUser", "User adicionado com ID: ${documentReference.id}")
            }
            .addOnFailureListener { e ->
                Log.w("Erro", "Erro ao adicionar pessoa", e)
            }
    }
}