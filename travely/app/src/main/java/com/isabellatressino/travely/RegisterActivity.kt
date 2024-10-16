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

    lateinit var senhaConf: String;
    lateinit var user: User;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // inflar layout da activity
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // configurar clique do botão de cadastro
        binding.buttonCadastrar.setOnClickListener {
            // obter dados do formulário
            senhaConf = binding.TextSenhaConfirmacao.text.toString().trim();
            user = User(
                name = binding.TextNome.text.toString().trim(),
                cpf = binding.TextCpf.text.toString().trim(),
                phone = binding.TextTelefone.text.toString().trim(),
                email = binding.TextEmail.text.toString().trim(),
                password = binding.TextSenha.text.toString().trim(),
                authID = "",
                schedule = null ,
                profile = "",
            );

            // validar campos do formulário
            if (user.name.isEmpty() || !user.isNameValid()) {
                binding.TextNome.setError("Preencha com um nome válido")
            } else if (user.cpf.isEmpty() || !user.isCpfValid()) {
                binding.TextCpf.setError("Preencha com um cpf válido")
            } else if (user.phone.isEmpty() || !user.isPhoneValid()) {
                binding.TextTelefone.setError("Preencha com um telefone válido")
            } else if (user.email.isBlank() || !user.isEmailValid()) {
                binding.TextEmail.setError("Preencha com um email válido")
            } else if (user.password.isEmpty() || !user.isPasswordValid()) {
                binding.TextSenha.setError("Preencha com uma senha de pelo menos 6 dígitos e sem espaços")
            } else if (senhaConf.isEmpty() || senhaConf != user.password) {
                binding.TextSenhaConfirmacao.setError("Preencha a confimação igual a senha")
            } else {
                val iProfile = Intent(this, ProfileActivity::class.java)
                iProfile.putExtra("userName", user.name)
                iProfile.putExtra("userCPF", user.cpf)
                iProfile.putExtra("userPhone", user.phone)
                iProfile.putExtra("userEmail", user.email)
                iProfile.putExtra("userPassword", user.password)
                startActivity(iProfile)
            }
        }
    }
}