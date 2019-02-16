package com.andb.apps.trails.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import org.jetbrains.annotations.Nullable

@Entity(foreignKeys = [ForeignKey(entity = BaseSkiRegion::class, parentColumns = ["regionId"], childColumns = ["regionParentId"], onDelete = ForeignKey.CASCADE)])
open class BaseSkiRegion(
    @PrimaryKey
    @ColumnInfo(name = "regionId")
    val id: Int,
    @ColumnInfo(name = "regionName")
    val name: String,
    @ColumnInfo(name = "regionMaps")
    val mapCount: Int = -1,
    @ColumnInfo(name = "regionParentId")
    @Nullable
    val parentId: Int?
)