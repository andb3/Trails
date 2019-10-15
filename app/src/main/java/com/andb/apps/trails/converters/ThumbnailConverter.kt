package com.andb.apps.trails.converters

import androidx.room.TypeConverter
import com.andb.apps.trails.objects.Thumbnail
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ThumbnailListConverter {
    private val gson = Gson()
    private val listType = object : TypeToken<List<Thumbnail>>() {}.type


    @TypeConverter
    fun stringToDetails(data: String?): List<Thumbnail> {
        if (data == null) {
            return listOf()
        }
        return gson.fromJson(data, listType)
    }

    @TypeConverter
    fun detailsToString(thumbnails: List<Thumbnail>): String {
        return gson.toJson(thumbnails, listType)
    }
}