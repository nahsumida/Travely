package com.isabellatressino.travely

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.isabellatressino.travely.dao.UserDao
import com.isabellatressino.travely.databinding.ActivityLoginBinding

class LoginActivity : AppCompatActivity() {

    private val binding by lazy { ActivityLoginBinding.inflate(layoutInflater) }

    private val userDao by lazy { UserDao() }

    override fun onStart() {
        super.onStart()

        val currentUser = userDao.auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this@LoginActivity, MainScreenActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        with(binding) {
            buttonLogin.setOnClickListener {
                val email = editEmail.text.toString().trim()
                val password = editPassword.text.toString()
                validUser(email, password)
            }

            buttonForgotPassword.setOnClickListener {
                val email = editEmail.text.toString().trim()

                if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    editEmail.error = "Preencha com um email válido"
                    return@setOnClickListener
                }

                buttonForgotPassword.isEnabled = false

                userDao.sendPasswordResetEmail(
                    email,
                    onSuccess = {
                        runOnUiThread {
                            showAlertMessage(
                                "Email de verificação enviado",
                                "Confira a caixa de entrada do seu email"
                            )
                            editEmail.text?.clear()
                            buttonForgotPassword.isEnabled = true
                        }
                    },
                    onFailure = { error ->
                        runOnUiThread {
                            showAlertMessage("Erro", error)
                            buttonForgotPassword.isEnabled = true
                        }
                    }
                )
            }

        }
//
//        textSingUp.setOnClickListener {
//            val iRegister = Intent(this@LoginActivity, RegisterActivity::class.java)
//            startActivity(iRegister)
//        }


    }


    private fun validUser(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            showAlertMessage("Atenção", "Preencha todos os campos para continuar")
            return
        }

        binding.buttonLogin.isEnabled = false

        userDao.signInWithEmailAndPassword(
            email, password,
            onSuccess = { uid ->
                userDao.getUserByAuthId(
                    uid,
                    onSuccess = { user ->
                        val intent = Intent(this, MainScreenActivity::class.java)
                        startActivity(intent)
                        finish()
                    },
                    onFailure = {
                        runOnUiThread {
                            showAlertMessage("Erro", "Usuário não encontrado.")
                        }
                    }
                )
            },
            onFailure = { exceptionMessage ->
                runOnUiThread {
                    val errorMessage = when {
                        "badly formatted" in exceptionMessage.lowercase() ->
                            "Endereço de email inválido, por favor digite novamente"

                        "password is invalid" in exceptionMessage.lowercase() ->
                            "Senha incorreta. Tente novamente"

                        "no user record" in exceptionMessage.lowercase() ->
                            "Usuário não encontrado. Verifique o email digitado."

                        else ->
                            "Usuário e/ou senha inválidos"
                    }

                    showAlertMessage("Erro", errorMessage)
                }
            }
        )
    }

    private fun showDefaultAlertDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Atenção")
            .setMessage("Deseja continuar com esta ação?")
            .setPositiveButton("Sim") { dialog, _ ->
                dialog.dismiss() // Fecha o diálogo ao clicar em "Sim"
            }
            .setNegativeButton("Cancelar") { dialog, _ ->
                dialog.dismiss()
            }
            .setIcon(android.R.drawable.ic_dialog_alert) // Ícone padrão de alerta
            .show()
    }

    private fun showAlertMessage(type: String, message: String) {
        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.custom_dialog, null)

        val alertDialog = AlertDialog.Builder(this)
            .setView(view)
            .create()

        alertDialog.window?.apply {
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            setLayout(
                (resources.displayMetrics.widthPixels * 0.7).toInt(),
                WindowManager.LayoutParams.WRAP_CONTENT
            )
        }

        view.findViewById<TextView>(R.id.tv_title)?.text = type
        view.findViewById<TextView>(R.id.tv_text)?.text = message
        view.findViewById<Button>(R.id.btn_dialog)?.setOnClickListener {
            alertDialog.dismiss()
            binding.buttonLogin.isEnabled = true
        }

        alertDialog.show()
    }

//    private fun showLoading(isLoading: Boolean) {
//        binding.layoutProgressbar.visibility = if (isLoading) View.VISIBLE else View.GONE
//    }
}
