package com.andb.apps.trails.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class IDListConverter {

    val gson = Gson()
    private val listType = object : TypeToken<List<Int>>() {}.type

    @TypeConverter
    fun stringToDetails(data: String?): List<Int>? {
        if (data == null) {
            return listOf()
        }
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun detailsToString(idList: List<Int>?): String {
        return gson.toJson(idList, listType)
    }
}