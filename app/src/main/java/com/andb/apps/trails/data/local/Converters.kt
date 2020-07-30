package com.andb.apps.trails.data.local

import androidx.annotation.Keep
import androidx.room.TypeConverter
import com.andb.apps.trails.data.model.MapTag
import com.andb.apps.trails.data.model.SkiAreaDetails
import com.andb.apps.trails.data.model.Thumbnail
import com.squareup.moshi.JsonAdapter
import com.squareup.moshi.Moshi
import org.koin.core.KoinComponent
import org.koin.core.inject

@Keep
class Converters : KoinComponent{
    val moshi: Moshi by inject()
    private val intListAdapter = moshi.adapter<List<Int>>(List::class.java)
    private val detailsAdapter = moshi.adapter<SkiAreaDetails>(SkiAreaDetails::class.java)
    private val stringAdapter = moshi.adapter<List<String>>(List::class.java)
    private val thumbnailAdapter = moshi.adapter<Thumbnail>(Thumbnail::class.java)
    private val mapTagAdapter = moshi.adapter<MapTag>(MapTag::class.java)

    @TypeConverter
    fun stringToIntList(data: String?): List<Int> {
        if (data == null) {
            return ArrayList()
        }
        val doubleList = (intListAdapter.fromJson(data) as List<Double>?) ?: listOf()
        return doubleList.map { it.toInt() }
    }

    @TypeConverter
    fun intListToString(idList: List<Int>): String {
        return intListAdapter.toJson(idList)
    }

    @TypeConverter
    fun stringToDetails(data: String?): SkiAreaDetails {
        if (data == null) {
            return SkiAreaDetails()
        }
        return detailsAdapter.fromJson(data) ?: SkiAreaDetails()
    }

    @TypeConverter
    fun detailsToString(details: SkiAreaDetails): String {
        return detailsAdapter.toJson(details)
    }

    @TypeConverter
    fun stringToThumbnails(data: String?) = toList(data, thumbnailAdapter)

    @TypeConverter
    fun thumbnailsToString(thumbnails: List<Thumbnail>) = toString(thumbnails, thumbnailAdapter)

    @TypeConverter
    fun stringToMapTags(data: String?) = toList(data, mapTagAdapter)

    @TypeConverter
    fun mapTagsToString(mapTags: List<MapTag>) = toString(mapTags, mapTagAdapter)

    fun <T : Any> toList(string: String?, adapter: JsonAdapter<T>): List<T> {
        if (string == null) return listOf()
        val strings = stringAdapter.fromJson(string) ?: listOf()
        return strings.mapNotNull { adapter.fromJson(it) }
    }

    fun <T> toString(list: List<T>, adapter: JsonAdapter<T>): String {
        return stringAdapter.toJson(list.map { adapter.toJson(it) })
    }

}