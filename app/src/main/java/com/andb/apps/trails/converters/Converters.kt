package com.andb.apps.trails.database

import androidx.room.TypeConverter
import com.andb.apps.trails.objects.SkiAreaDetails
import com.andb.apps.trails.objects.Thumbnail
import com.squareup.moshi.Moshi
import org.koin.core.KoinComponent
import org.koin.core.inject

class Converters : KoinComponent{
    private val moshi: Moshi by inject()
    private val intListAdapter = moshi.adapter<List<Int>>(List::class.java)
    private val detailsAdapter = moshi.adapter<SkiAreaDetails>(SkiAreaDetails::class.java)
    private val stringAdapter = moshi.adapter<List<String>>(List::class.java)
    private val thumbnailAdapter = moshi.adapter<Thumbnail>(Thumbnail::class.java)

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
    fun stringToThumbnailList(data: String?): List<Thumbnail> {
        if (data == null) {
            return listOf()
        }
        //For some reason, moshi can't decode lists of thumbnails, and instead decodes them to List<LinkedHashTreeMap>
        //By converting to string and then list, and vice versa, it works
        val strings = stringAdapter.fromJson(data) ?: listOf()
        return strings.mapNotNull { thumbnailAdapter.fromJson(it) }
    }

    @TypeConverter
    fun thumbnailListToString(thumbnails: List<Thumbnail>): String {
        return stringAdapter.toJson(thumbnails.map { thumbnailAdapter.toJson(it) })
    }
}