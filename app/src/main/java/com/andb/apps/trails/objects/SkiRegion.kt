package com.andb.apps.trails.objects

import androidx.room.*
import com.squareup.moshi.Json
import org.jetbrains.annotations.Nullable
import retrofit2.http.Field

@Entity
open class SkiRegion(
    @PrimaryKey
    @ColumnInfo(name = "regionID")
    val id: Int,

    @ColumnInfo(name = "regionName")
    val name: String,

    @ColumnInfo(name = "regionMaps")
    @field:Json(name = "map_count")
    val mapCount: Int,

    @ColumnInfo(name = "regionChildIDs")
    @field:Json(name = "child_regions")
    val childIDs: ArrayList<Int>,

    @ColumnInfo(name = "regionAreaIDs")
    @field:Json(name = "child_areas")
    val areaIDs: ArrayList<Int>,

    @ColumnInfo(name = "regionParentID")
    @field:Json(name = "parent_id")
    val parentID: Int
) {

    /**Returns whether region is one of the 4 base regions (Americas, Europe, Asia, Oceania)**/
    fun isBase() = listOf(1, 2, 3, 4).contains(id)

    /**Returns whether region isn't a base region**/
    fun isChild() = !isBase()
}

