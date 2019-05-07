package com.andb.apps.trails.objects

import androidx.room.*
import com.andb.apps.trails.converters.IdListConverter
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
    fun isParent() = listOf(1, 2, 3, 4).contains(id)
    fun isChild() = !isParent()
}

