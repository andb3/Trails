package com.andb.apps.trails.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.andb.apps.trails.converters.ThumbnailListConverter

@Entity
class SkiMap(
    @PrimaryKey
    @ColumnInfo(name = "map_id")
    val id: Int,
    @ColumnInfo(name = "map_year")
    val year: Int,
    @ColumnInfo(name = "map_thumbs")
    val thumbnails: ArrayList<Thumbnail>,
    @ColumnInfo(name = "map_url")
    val url: String,
    @ColumnInfo(name = "map_parent")
    val parentID: Int) {

    @ColumnInfo(name = "map_favorite")
    var favorite = false

    fun isPdf(): Boolean = url.takeLast(4) == ".pdf"

}

class Thumbnail(val width: Int, val height: Int, val url: String)

