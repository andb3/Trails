package com.andb.apps.trails.objects

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
open class BaseSkiArea(
    @PrimaryKey
    @ColumnInfo(name = "area_id")
    val id: Int,
    val name: String,
    val liftCount: Int,
    val runCount: Int,
    val openingYear: Int,
    val website: String,
    var favorite: Int = 0
) {
    constructor(id: Int, name: String) : this(id, name, -1, -1, -1, "")
}