package com.isabellatressino.travely.models

import android.util.Patterns

class User (var name: String,
              var cpf: String,
              var phone:String,
              var email: String,
              var password: String,
              var authID: String,
              var schedule: List<Schedule>?,
              var profile: String,){

    public fun isNameValid(): Boolean {
        val regex = Regex("^[^0-9@#$%^&+=]*\$")
        return regex.matches(name)
    }

    public fun isCpfValid(): Boolean {
        val regex = Regex("^[0-9]{11}\$")
        return regex.matches(cpf)
    }

    public fun isPhoneValid(): Boolean {
        val regex = Regex("^[0-9]{11}\$")
        return regex.matches(phone)
    }

    public fun isPasswordValid(): Boolean {
        if (!password.contains(" ")){
            if (password.length >= 6){
                return true
            }
        }
        return false
    }

    public fun isEmailValid(): Boolean {
        return !email.contains(" ") || Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}