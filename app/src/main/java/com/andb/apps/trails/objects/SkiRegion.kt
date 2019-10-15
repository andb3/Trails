package com.andb.apps.trails.objects

import androidx.room.*
import com.squareup.moshi.Json
import org.jetbrains.annotations.Nullable
import retrofit2.http.Field

@Entity
data class SkiRegion(
    @PrimaryKey
    @ColumnInfo(name = "regionID")
    @Json(name = "id")
    val id: Int,

    @ColumnInfo(name = "regionName")
    @Json(name = "name")
    val name: String,

    @ColumnInfo(name = "regionMaps")
    @Json(name = "map_count")
    val mapCount: Int,

    @ColumnInfo(name = "regionChildIDs")
    @Json(name = "child_regions")
    val childIDs: List<Int>,

    @ColumnInfo(name = "regionAreaIDs")
    @Json(name = "child_areas")
    val areaIDs: List<Int>,

    @ColumnInfo(name = "regionParentID")
    @Json(name = "parent_id")
    val parentID: Int
) {

    /**Returns whether region is one of the 4 base regions (Americas, Europe, Asia, Oceania)**/
    fun isBase() = listOf(1, 2, 3, 4).contains(id)

    /**Returns whether region isn't a base region**/
    fun isChild() = !isBase()
}

