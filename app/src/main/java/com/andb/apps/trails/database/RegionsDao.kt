package com.andb.apps.trails.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.andb.apps.trails.objects.BaseSkiRegion

@Dao
interface RegionsDao{

    @Insert
    fun insertRegion(region: BaseSkiRegion)

    @Delete
    fun deleteRegion(region: BaseSkiRegion)

    @Query("SELECT * FROM BaseSkiRegion WHERE `regionParentId` = :parentId")
    fun getAllFromParent(parentId: Int): List<BaseSkiRegion>

    @Query("SELECT * FROM BaseSkiRegion")
    fun getAllChildren(): List<BaseSkiRegion>

    @Query("SELECT * FROM BaseSkiRegion WHERE regionId in (1, 2, 3, 4)")
    fun getAllParents(): List<BaseSkiRegion>
}