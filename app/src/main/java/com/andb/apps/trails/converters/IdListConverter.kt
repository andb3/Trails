package com.andb.apps.trails.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IDListConverter {

    val gson = Gson()
    private val listType = object : TypeToken<java.util.ArrayList<Int>>() {}.type

    @TypeConverter
    fun stringToDetails(data: String?): ArrayList<Int> {
        if (data == null) {
            return ArrayList()
        }
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun detailsToString(idList: ArrayList<Int>): String {
        return gson.toJson(idList, listType)
    }
}