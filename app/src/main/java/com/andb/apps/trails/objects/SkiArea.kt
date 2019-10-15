package com.andb.apps.trails.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.andb.apps.trails.repository.MapsRepo
import com.squareup.moshi.Json

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
    val parentIDs: List<Int>,

    @Json(name = "favorite")
    var favorite: Boolean = false
) {
    suspend fun getMapPreviewUrl(): String? {
        val mapID = maps.firstOrNull() ?: return null
        val map = MapsRepo.getMapByID(mapID)
        return map?.thumbnails?.firstOrNull()?.url
    }

    fun mapPreviewID() = maps.firstOrNull()

    fun toggleFavorite() {
        favorite = !favorite
    }
}

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