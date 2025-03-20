package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.models.User

class UserDao {

    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")
    val auth = FirebaseAuth.getInstance()

    // Função para pegar um usuário pelo authID
    fun getUserByAuthId(
        authId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.whereEqualTo("authID", authId).get()
            .addOnSuccessListener { documents ->
                val firstDocument = documents.firstOrNull()

                if (firstDocument != null) {
                    val user = User(
                        name = firstDocument.getString("name") ?: "",
                        cpf = firstDocument.getString("cpf") ?: "",
                        phone = firstDocument.getString("phone") ?: "",
                        email = firstDocument.getString("email") ?: "",
                        password = firstDocument.getString("password") ?: "",
                        authID = firstDocument.getString("authID") ?: "",
                        schedule = null,
                        profile = firstDocument.getString("profile") ?: ""
                    )

                    Log.d("TESTEEEE", "Usuário encontrado: $user")
                    onSuccess(user)
                } else {
                    Log.w("TESTEEEE", "Nenhum usuário encontrado")
                    onSuccess(null)
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firestore", "Erro ao buscar usuário", exception)
                onFailure(exception)
            }
    }

    // Função de login
    fun signInWithEmailAndPassword(
        email: String,
        password: String,
        onSuccess: (String) -> Unit,
        onFailure: (String) -> Unit
    ) {
        auth.signInWithEmailAndPassword(email, password)
            .addOnSuccessListener { authResult ->
                val user = authResult.user
                if (user != null) {
                    if (user.isEmailVerified) {
                        onSuccess(user.uid)
                    } else {
                        onFailure("Email não verificado. Verifique seu email para ativar a conta.")
                    }
                }
            }
            .addOnFailureListener { exception ->
                onFailure(exception.message ?: "Erro desconhecido")
            }
    }

    // Função para enviar email de recuperação de senha
    fun sendPasswordResetEmail(email: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        auth.sendPasswordResetEmail(email)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    onSuccess()
                } else {
                    onFailure("Falha ao enviar email de recuperação.")
                }
            }
    }

}