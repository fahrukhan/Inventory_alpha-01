package com.example.inventory_alpha_01.controller

import android.content.Context
import com.example.inventory_alpha_01.utils.SharedPreference
import com.example.inventory_alpha_01.utils.SharedPreference.*
import com.example.inventory_alpha_01.utils.SharedPreference.Key.ADDRESS

class Controller(context: Context, prefName: Name? = null) {

    private val btPref = SharedPreference(context, prefName!!)

    fun getBluetoothName(): String {
        return btPref.readString(ADDRESS)
    }
    fun setBluetoothAddress(param: String) {
        btPref.writeString(ADDRESS, param)
    }

    fun setBluetoothName(param: String) {
        btPref.writeString(ADDRESS, param)
    }

//    companion object{
//        const val BLUETOOTH = "bluetooth"
//    }
}