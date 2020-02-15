package com.andb.apps.trails.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

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
    @ColumnInfo(name = "map_parent")
    @Json(name = "parent_id")
    val parentID: Int) {

    @ColumnInfo(name = "map_favorite")
    var favorite = false

    fun suffix(): String = url.takeLastWhile { it != '.' }
    fun isPdf(): Boolean = suffix() == "pdf"

}


fun String.isPdf(): Boolean = takeLast(3) == "pdf"

@Keep
data class Thumbnail(val width: Int, val height: Int, val url: String)

