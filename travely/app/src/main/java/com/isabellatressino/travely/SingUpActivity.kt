package com.isabellatressino.travely

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.isabellatressino.travely.models.User
import com.isabellatressino.travely.databinding.ActivitySingUpBinding

class SingUpActivity : AppCompatActivity() {

    private val binding by lazy { ActivitySingUpBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        binding.buttonRegister.setOnClickListener {
            if (!validateInputs()) return@setOnClickListener

            val user = User(
                name = binding.editName.text.toString().trim(),
                cpf = binding.editCpf.unMasked,
                phone = binding.editPhone.unMasked,
                email = binding.editEmail.text.toString().trim(),
                password = binding.editPassword.text.toString(),
                authID = "",
                schedule = emptyList(),
                profile = ""
            )

            val intent = Intent(this, QuestionsActivity::class.java)
            intent.putExtra("user", user)
            startActivity(intent)
        }
    }

    private fun validateInputs(): Boolean {
        val name = binding.editName.text.toString().trim()
        val cpf = binding.editCpf.unMasked
        val phone = binding.editPhone.unMasked
        val email = binding.editEmail.text.toString().trim()
        val password = binding.editPassword.text.toString()
        val confirmPassword = binding.editConfirmPassword.text.toString()

        if (name.isEmpty() || cpf.isEmpty() || phone.isEmpty() || email.isEmpty() ||
            password.isEmpty() || confirmPassword.isEmpty()
        ) {
            showToast("Por favor, preencha todos os campos")
            return false
        }

        if (cpf.length < 11) {
            showToast("CPF inválido")
            return false
        }

        if (phone.length < 10) {
            showToast("Telefone inválido")
            return false
        }

        if (password.length < 6) {
            showToast("A senha deve ter pelo menos 6 caracteres")
            return false
        }

        if (password != confirmPassword) {
            showToast("As senhas não coincidem")
            return false
        }

        return true
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }


}
