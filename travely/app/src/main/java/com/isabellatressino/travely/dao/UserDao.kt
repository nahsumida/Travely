package com.isabellatressino.travely.dao

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.isabellatressino.travely.interfaces.IUserDao
import com.isabellatressino.travely.models.User

class UserDao : IUserDao {


    // Busca um usuário pelo authID
    private val db = FirebaseFirestore.getInstance()
    private val usersCollection = db.collection("users")

    override fun getUserByAuthId(
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
                        schedule = null, // Se precisar, converta manualmente
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


    // Inserir usuário
    override fun insertUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.document(user.authID)
            .set(user)
            .addOnSuccessListener {
                // Sucesso
                onSuccess()
            }
            .addOnFailureListener { exception ->
                // Falha
                onFailure(exception)
            }
    }

    // Atualizar usuário
    override fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.document(user.authID)
            .set(user)  // Podemos usar .set() para sobrescrever o documento ou .update() para atualizar campos específicos
            .addOnSuccessListener {
                // Sucesso
                onSuccess()
            }
            .addOnFailureListener { exception ->
                // Falha
                onFailure(exception)
            }
    }

    // Deletar usuário
    override fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.document(userId)
            .delete()
            .addOnSuccessListener {
                // Sucesso
                onSuccess()
            }
            .addOnFailureListener { exception ->
                // Falha
                onFailure(exception)
            }
    }

    // Obter usuário pelo ID
    override fun getUserById(
        userId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    ) {
        usersCollection.document(userId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                // Sucesso
                val user = documentSnapshot.toObject(User::class.java)
                onSuccess(user)
            }
            .addOnFailureListener { exception ->
                // Falha
                onFailure(exception)
            }
    }

    // Obter todos os usuários
    override fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit) {
        usersCollection.get()
            .addOnSuccessListener { querySnapshot ->
                // Sucesso
                val users = querySnapshot.documents.mapNotNull { it.toObject(User::class.java) }
                onSuccess(users)
            }
            .addOnFailureListener { exception ->
                // Falha
                onFailure(exception)
            }
    }
}