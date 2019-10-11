package com.andb.apps.trails.objects

import androidx.room.*
import com.andb.apps.trails.converters.IDListConverter
import com.andb.apps.trails.converters.SkiAreaDetailsConverter
import com.andb.apps.trails.repository.MapsRepo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.squareup.moshi.Json

@Entity
open class SkiArea(
    @PrimaryKey
    @ColumnInfo(name = "areaID")
    val id: Int,

    @ColumnInfo(name = "areaName")
    val name: String,
    @ColumnInfo(name = "areaDetails")
    @field:Json(name = "info")
    val details: SkiAreaDetails,
    @ColumnInfo(name = "areaMaps")
    val maps: ArrayList<Int>,
    @field:Json(name = "parent_ids")
    val parentIDs: ArrayList<Int>,

    @field:Json(name = "favorite")
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
    @field:Json(name = "officialWebsite")
    val website: String? = null,
    val operatingStatus: String? = null
)