package com.andb.apps.trails.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.FromJson
import com.squareup.moshi.Json
import com.squareup.moshi.JsonDataException
import com.squareup.moshi.ToJson

@Entity
data class SkiMap(
    @PrimaryKey
    @ColumnInfo(name = "map_id")
    val id: Int,

    @ColumnInfo(name = "map_year")
    val year: Int,

    @ColumnInfo(name = "map_thumbs")
    val thumbnails: List<Thumbnail>,

    @ColumnInfo(name = "map_url")
    @Json(name = "image_url")
    val url: String,

    @ColumnInfo(name = "map_caption")
    val caption: String,

    @ColumnInfo(name = "map_tags")
    val tags: List<MapTag>,

    @ColumnInfo(name = "map_parent")
    @Json(name = "parent_id")
    val parentID: Int
) {

    @ColumnInfo(name = "map_favorite")
    var favorite = false

    fun suffix(): String = url.takeLastWhile { it != '.' }
}


fun String.isPdf(): Boolean = takeLast(3) == "pdf"

@Keep
data class Thumbnail(val width: Int, val height: Int, val url: String)

@Keep
enum class MapTag {
    DOWNHILL, NORDIC, HIKING, BIKING, VILLAGE, PLAN;
}

class MapTagAdapter {
    @ToJson
    fun toJson(tag: MapTag): String {
        return when (tag) {
            MapTag.DOWNHILL -> "downhill"
            MapTag.NORDIC -> "nordic"
            MapTag.HIKING -> "hiking"
            MapTag.BIKING -> "biking"
            MapTag.VILLAGE -> "village"
            MapTag.PLAN -> "master plan"
        }
    }

    @FromJson
    fun fromJson(tag: String): MapTag {
        return when (tag.toLowerCase()) {
            "downhill" -> MapTag.DOWNHILL
            "nordic" -> MapTag.NORDIC
            "hiking" -> MapTag.HIKING
            "biking" -> MapTag.BIKING
            "village" -> MapTag.VILLAGE
            "master plan" -> MapTag.PLAN
            else -> throw JsonDataException("unknown tag: $tag")
        }
    }
}
