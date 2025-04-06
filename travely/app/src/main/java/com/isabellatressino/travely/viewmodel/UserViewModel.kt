package com.isabellatressino.travely.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.isabellatressino.travely.dao.UserDao
import com.isabellatressino.travely.models.User
import androidx.core.content.edit

class UserViewModel : ViewModel() {

    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    private val userDao: UserDao = UserDao()

    fun fetchUser(uid: String, context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)

        val savedName = sharedPreferences.getString("user_name", null)
        val savedProfile = sharedPreferences.getString("user_profile", null)
        val savedEmail = sharedPreferences.getString("user_email", null)

        if (savedName != null && savedProfile != null && savedEmail != null) {
            // Recupera do cache local
            _user.value = User(savedName, "", "", savedEmail, "", uid, null, savedProfile)
        } else {
            // Busca no Firestore
            userDao.getUserByAuthId(
                uid,
                onSuccess = { user ->
                    if (user != null) {
                        _user.value = user
                        saveUserToPreferences(user, context)
                    } else {
                        _user.value = null
                    }
                },
                onFailure = {
                    _user.value = null
                }
            )
        }
    }

    private fun saveUserToPreferences(user: User, context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            putString("user_name", user.name)
            putString("user_profile", user.profile)
            putString("user_email", user.email)
        }
    }

    fun clearUserPreferences(context: Context) {
        val sharedPreferences = context.getSharedPreferences("user_prefs", Context.MODE_PRIVATE)
        sharedPreferences.edit {
            clear()
        }
        _user.value = null
    }
}
