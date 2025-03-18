package com.isabellatressino.travely.interfaces

import com.isabellatressino.travely.models.User

interface IUserDao {

    // Busca o usuário com base no authID
    fun getUserByAuthId(
        authId: String,
        onSuccess: (User?) -> Unit,
        onFailure: (Exception) -> Unit
    )

    // Inserir um usuário no banco de dados
    fun insertUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // Atualizar os dados de um usuário no banco de dados
    fun updateUser(user: User, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // Deletar um usuário do banco de dados
    fun deleteUser(userId: String, onSuccess: () -> Unit, onFailure: (Exception) -> Unit)

    // Obter um usuário pelo ID
    fun getUserById(userId: String, onSuccess: (User?) -> Unit, onFailure: (Exception) -> Unit)

    // Obter todos os usuários
    fun getAllUsers(onSuccess: (List<User>) -> Unit, onFailure: (Exception) -> Unit)
}