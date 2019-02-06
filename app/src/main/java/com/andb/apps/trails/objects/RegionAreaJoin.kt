package com.andb.apps.trails.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["joinRegionId", "joinAreaId"],
    foreignKeys = [
        ForeignKey(entity = BaseSkiRegion::class, parentColumns = ["regionId"], childColumns = ["joinRegionId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = BaseSkiArea::class, parentColumns = ["area_id"], childColumns = ["joinAreaId"], onDelete = ForeignKey.CASCADE)
    ])
class RegionAreaJoin(
    @ColumnInfo(name = "joinRegionId")
    val regionId: Int,
    @ColumnInfo(name = "joinAreaId")
    val areaId: Int
)