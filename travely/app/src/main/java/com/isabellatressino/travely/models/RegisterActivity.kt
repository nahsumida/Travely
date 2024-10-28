package com.isabellatressino.travely.models

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
//import com.google.firebase.functions.FirebaseFunctions
import com.isabellatressino.travely.databinding.ActivityRegisterBinding

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
                profile = "", //Profile será gerado na ProfileActivity
            )

            // Valida se cada campo do form é válido
            if (isFormValid(senhaConf,user)) {
                //isNewUser(user)

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
    private fun isFormValid(senhaConf:String, user: User): Boolean{
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
}