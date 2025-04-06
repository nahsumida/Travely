package com.isabellatressino.travely.models

import com.google.type.DateTime
import java.io.Serializable

class User(
    var name: String,
    var cpf: String,
    var phone: String,
    var email: String,
    var password: String,
    // var birthDate: DateTime,
    var authID: String,
    var schedule: List<Schedule>?,
    var profile: String,
    //   var answers: List<String> = emptyList()
) : Serializable {
    // Método que retorna uma descrição com base no perfil armazenado
    fun getProfileDescription(): String {
        return when (profile) {
            "cultural" -> "Turista cultural"
            "compras" -> "Turista de compras"
            "gastronomico" -> "Turista gastronômico"
            "aventureiro" -> "Turista aventureiro"
            "negocios" -> "Turista de negócios"
            "descanso" -> "Turista de lazer"
            else -> "Perfil desconhecido"
        }
    }

    override fun toString(): String {
        return """
            User(
                name='$name',
                cpf='$cpf',
                phone='$phone',
                email='$email',
                password='$password',
                authID='$authID',
                schedule=${schedule?.joinToString { it.toString() } ?: "[]"},
                profile='$profile',
            )
        """.trimIndent()
    }

}