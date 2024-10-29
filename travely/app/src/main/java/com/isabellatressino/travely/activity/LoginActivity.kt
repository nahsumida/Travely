package com.isabellatressino.travely.activity

import android.content.ContentValues
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.R
import com.isabellatressino.travely.databinding.ActivityLoginBinding

// Classe de Login
class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val auth by lazy { FirebaseAuth.getInstance() }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            buttonContinue.setOnClickListener {
                validUser(editUser.text.toString().trim(), editPassword.text.toString())
                buttonContinue.isEnabled = false
            }

            buttonContinue.isEnabled = true
        }

        binding.textSingUp.setOnClickListener {
            val iRegister = Intent(this, RegisterActivity::class.java)
            startActivity(iRegister)
        }

        binding.textForgotPassword.setOnClickListener {
            var email = binding.editUser.text.toString().trim();

            if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email)
                    .matches() || email.contains(" ")
            ) {
                binding.editUser.setError("Preencha com um email válido")
                showAlertMessage("Atenção","Preencha um email válido")
            } else {
                auth.sendPasswordResetEmail(email).addOnCompleteListener { task ->
                    //se o envio for um sucesso
                    if (task.isSuccessful) {
                        Log.d(ContentValues.TAG, "sendPasswordResetEmail:success")
//                        Toast.makeText(
//                            baseContext,
//                            "Email de recuperação enviado, verifique seu email",
//                            Toast.LENGTH_SHORT
//                        ).show()
                        showAlertMessage("Email de verificação enviado","Confira a caixa de entrada de seu email")
                    } else {
                        Log.w(ContentValues.TAG, "sendPasswordResetEmail:failure", task.exception)
                        Toast.makeText(
                            baseContext,
                            "Falha ao enviar email de recuperação",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        binding.buttonContinue.isEnabled = true
    }

    private fun validUser(email: String, password: String) {

        if (email.isBlank() or password.isBlank()) {
            showAlertMessage("Atenção", "Preencha todos os campos para continuar")
            return
        }

        showLoading(true)

        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                if (authResult.user?.isEmailVerified == false) {
                    auth.signOut()
                    showLoading(false)
//                    Toast.makeText(
//                        this,
//                        "Por favor, ative a conta através do link enviado no email e tente novamente!",
//                        Toast.LENGTH_LONG
//                    ).show()
                    showAlertMessage("Ativação de conta","Por favor, ative a conta através do link enviado no email e tente novamente!")
                    return@addOnSuccessListener
                }
                val uid = authResult.user?.uid
                if (uid != null) {
                    FirebaseFirestore.getInstance()
                        .collection("users")
                        .whereEqualTo("authID", uid)
                        .get()
                        .addOnSuccessListener { querySnapshot ->
                            if (!querySnapshot.isEmpty) {
                                // Aqui você pode redirecionar para a próxima tela
                                startActivity(Intent(this, MainScreenActivity::class.java))
                                finish()
                            } else {
                                Log.d(
                                    "[ERRO] LoginActivity ",
                                    "UID do usuário nao encontrado: $uid"
                                )
                                Toast.makeText(
                                    this,
                                    "Usuário não encontrado na base de dados.",
                                    Toast.LENGTH_LONG
                                ).show()
                                binding.buttonContinue.isEnabled = true
                                showLoading(false)
                            }
                        }
                        .addOnFailureListener { exception ->
                            Log.e("[ERRO] Login Auth:", exception.message.toString())
                            Toast.makeText(
                                this,
                                "Erro ao acessar os dados do usuário.",
                                Toast.LENGTH_LONG
                            ).show()
                            binding.buttonContinue.isEnabled = true
                            showLoading(false)
                        }
                }

            }.addOnFailureListener { exception ->
                if (exception.message.toString() == "The email address is badly formatted.") {
                    showAlertMessage("Erro","Endereço de email inválido, por favor digite novamente")
                } else {
                    showAlertMessage("Erro","Usuário e/ou senha inválidos")
                }
                showLoading(false)
            }
    }

    private fun showAlertMessage(type: String, message: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        alertDialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val title = view.findViewById<TextView>(R.id.tv_title)
        val text = view.findViewById<TextView>(R.id.tv_text)
        val button = view.findViewById<Button>(R.id.btn_dialog)

        title.text = type
        text.text = message

        button.setOnClickListener {
            alertDialog.dismiss()
            binding.buttonContinue.isEnabled = true
        }

        alertDialog.show()

        alertDialog.window?.setLayout(
            (resources.displayMetrics.widthPixels * 0.85).toInt(),
            WindowManager.LayoutParams.WRAP_CONTENT
        )
    }

    private fun showLoading(isLoading: Boolean) {
        binding.layoutProgressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
    }

}