package com.andb.apps.trails.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andb.apps.trails.objects.SkiRegion

@Dao
interface RegionsDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertRegion(region: SkiRegion)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleRegions(regions: List<SkiRegion>)

    @Delete
    fun deleteRegion(region: SkiRegion)

    @Query("SELECT * FROM SkiRegion WHERE `regionID` = :regionID")
    fun getRegionByID(regionID: Int): SkiRegion?

    @Query("SELECT * FROM SkiRegion WHERE `regionParentID` = :parentID")
    fun getAllFromParent(parentID: Int): LiveData<List<SkiRegion>>

    @Query("SELECT * FROM SkiRegion")
    fun getAll(): LiveData<List<SkiRegion>>

    @Query("SELECT * FROM SkiRegion")
    fun getAllStatic(): List<SkiRegion>
}