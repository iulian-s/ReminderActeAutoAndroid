package com.example.reminderacteautoandroid.config

import android.content.Context
import androidx.core.content.edit

class TokenManager(context: Context) {
    private val prefs = context.getSharedPreferences("auth_prefs", Context.MODE_PRIVATE)

    fun saveToken(token: String){
        prefs.edit { putString("jwt_token", token) }
    }

    fun getToken(): String? = prefs.getString("jwt_token", null)

    fun deleteToken(){
        prefs.edit{ remove("jwt_token") }
    }
}