package com.andb.apps.trails.converters

import androidx.room.TypeConverter
import com.andb.apps.trails.objects.SkiAreaDetails
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class SkiAreaDetailsConverter {

    private val gson = Gson()
    private val detailsType = object : TypeToken<SkiAreaDetails>() {}.type

    @TypeConverter
    fun stringToDetails(data: String?): SkiAreaDetails {

        return gson.fromJson(data, detailsType)
    }

    @TypeConverter
    fun detailsToString(skiAreaDetails: SkiAreaDetails): String {
        return gson.toJson(skiAreaDetails, detailsType)
    }
}