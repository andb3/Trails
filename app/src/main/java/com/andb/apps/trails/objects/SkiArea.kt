package com.andb.apps.trails.objects

import androidx.room.*
import com.andb.apps.trails.converters.IdListConverter
import com.andb.apps.trails.converters.SkiAreaDetailsConverter
import com.andb.apps.trails.repository.MapsRepo
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

@Entity
open class SkiArea(
    @PrimaryKey
    @ColumnInfo(name = "area_id")
    val id: Int,
    val name: String,
    @ColumnInfo(name = "areaDetails")
    val details: SkiAreaDetails,
    @ColumnInfo(name = "areaMaps")
    val maps: ArrayList<Int>,
    val parentIds: ArrayList<Int>,
    var favorite: Boolean = false
) {
    fun mapPreviewUrl(): String? {
        val mapId = maps.firstOrNull() ?: return null
        val map = MapsRepo.getMapById(mapId)
        return map?.thumbnails?.firstOrNull()?.url
    }

    fun mapPreviewId() = maps.firstOrNull()

    fun toggleFavorite() {
        favorite = !favorite
    }
}

class SkiAreaDetails(
    val liftCount: Int? = null,
    val runCount: Int? = null,
    val openingYear: Int? = null,
    val website: String? = null
)