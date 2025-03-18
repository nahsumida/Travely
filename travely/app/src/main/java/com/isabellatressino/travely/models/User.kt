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
) {}