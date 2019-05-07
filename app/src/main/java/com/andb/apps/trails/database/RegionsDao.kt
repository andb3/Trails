package com.andb.apps.trails.database

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.andb.apps.trails.objects.SkiRegion

@Dao
interface RegionsDao{

    @Insert
    fun insertRegion(region: SkiRegion)

    @Delete
    fun deleteRegion(region: SkiRegion)

    @Query("SELECT * FROM SkiRegion WHERE `regionParentId` = :parentId")
    fun getAllFromParent(parentId: Int): List<SkiRegion>

    @Query("SELECT * FROM SkiRegion WHERE regionId not in (1, 2, 3, 4)")
    fun getAllChildren(): List<SkiRegion>

    @Query("SELECT * FROM SkiRegion WHERE regionId in (1, 2, 3, 4)")
    fun getAllParents(): List<SkiRegion>

    @Query("SELECT * FROM SkiRegion")
    fun getAll(): LiveData<List<SkiRegion>>

    @Query("SELECT * FROM SkiRegion")
    fun getAllStatic(): List<SkiRegion>
}