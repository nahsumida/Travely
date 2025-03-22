package com.isabellatressino.travely.models

class User(
    var name: String,
    var cpf: String,
    var phone: String,
    var email: String,
    var password: String,
    var authID: String,
    var schedule: List<Schedule>?,
    var profile: String,
) {
    // Método que retorna uma descrição com base no perfil armazenado
    fun getProfileDescription(): String {
        return when (profile) {
            "cultural" -> "Turista cultural"
            "compras" -> "Turista de compras"
            "gastronomico" -> "Turista gastronômico"
            "aventureiro" -> "Turista aventureiro"
            "negocios" -> "Turista de negócios"
            "descanso" -> "Turista de descanso"
            else -> "Perfil desconhecido"
        }
    }
}