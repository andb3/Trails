package com.andb.apps.trails.data.model

import androidx.annotation.Keep
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Keep
@Entity
data class SkiArea(
    @PrimaryKey
    @ColumnInfo(name = "areaID")
    val id: Int,
    @ColumnInfo(name = "areaName")
    val name: String,
    @ColumnInfo(name = "areaDetails")
    @Json(name = "info")
    val details: SkiAreaDetails?,

    @ColumnInfo(name = "areaMaps")
    val maps: List<Int>,
    @Json(name = "parent_ids")
    val parentIDs: List<Int>

) {
    @Json(name = "area_favorite")
    var favorite = false

    fun mapPreviewID() = maps.firstOrNull()

}

@Keep
class SkiAreaDetails(
    val liftCount: Int? = null,
    val runCount: Int? = null,
    val openingYear: Int? = null,
    @Json(name = "officialWebsite")
    val website: String? = null,
    val operatingStatus: String? = null
){
    constructor() : this(null, null, null, null, null)
}