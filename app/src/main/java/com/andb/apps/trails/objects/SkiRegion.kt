package com.andb.apps.trails.objects

import androidx.room.*
import org.jetbrains.annotations.Nullable

@Entity
open class SkiRegion(
    @PrimaryKey
    @ColumnInfo(name = "regionId")
    val id: Int,
    @ColumnInfo(name = "regionName")
    val name: String,
    @ColumnInfo(name = "regionMaps")
    val mapCount: Int,
    @ColumnInfo(name = "regionChildIds")
    val childIds: ArrayList<Int>,
    @ColumnInfo(name = "regionAreaIds")
    val areaIds: ArrayList<Int>,
    @ColumnInfo(name = "regionParentId")
    @Nullable
    val parentId: Int?
) {

    /**Returns whether region is one of the 4 base regions (Americas, Europe, Asia, Oceania)**/
    fun isBase() = listOf(1, 2, 3, 4).contains(id)

    /**Returns whether region isn't a base region**/
    fun isChild() = !isBase()
}

