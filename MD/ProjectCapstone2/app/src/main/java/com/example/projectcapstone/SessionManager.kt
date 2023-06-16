package com.example.projectcapstone

import android.content.Context
import android.content.SharedPreferences
import com.example.projectcapstone.ConstValue.KEY_IS_LOGIN
import com.example.projectcapstone.ConstValue.KEY_TOKEN
import com.example.projectcapstone.ConstValue.KEY_USER_NAME
import com.example.projectcapstone.ConstValue.PREFS_NAME

class SessionManager (context: Context) {

    private var prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val editor = prefs.edit()

    fun setStringPreference(prefKey: String, value: String) {
        editor.putString(prefKey, value)
        editor.apply()
    }

    fun setBooleanPreference(prefKey: String, value: Boolean) {
        editor.putBoolean(prefKey, value)
        editor.apply()
    }

    fun clearPreferences() {
        editor.clear().apply()
    }

    val getToken = prefs.getString(KEY_TOKEN, "")
    val isLogin = prefs.getBoolean(KEY_IS_LOGIN, false)
    val getUserName = prefs.getString(KEY_USER_NAME, "")
}