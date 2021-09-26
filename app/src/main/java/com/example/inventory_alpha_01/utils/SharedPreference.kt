package com.example.inventory_alpha_01.utils

import android.content.Context

class SharedPreference(context: Context, prefName: Name) {
    private val sharedPref = context.getSharedPreferences(prefName.toString(), Context.MODE_PRIVATE)

    fun writeString(key: Key, value: String){
        with(sharedPref.edit()){
            putString(key.toString(), value)
            commit()
        }
    }

    fun readString(key: Key): String {
        return sharedPref.getString(key.toString(), "") as String
    }

    enum class Name{
        BLUETOOTH
    }

    enum class Key{
        ADDRESS
    }
}