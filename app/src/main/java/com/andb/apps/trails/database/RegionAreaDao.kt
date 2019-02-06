package com.andb.apps.trails.database

import androidx.room.*
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.BaseSkiRegion
import com.andb.apps.trails.objects.RegionAreaJoin

@Dao
interface RegionAreaDao {

    @Insert
    fun insert(regionArea: RegionAreaJoin)

    @Update
    fun update(regionArea: RegionAreaJoin)

    @Delete
    fun delete(regionArea: RegionAreaJoin)

    @Query("SELECT * FROM RegionAreaJoin WHERE joinRegionId = :regionId AND joinAreaId = :areaId")
    fun getJoinsByBoth(regionId: Int, areaId: Int): List<RegionAreaJoin>

    @Query("SELECT * FROM RegionAreaJoin WHERE joinRegionId = :regionId")
    fun getJoinsByRegionId(regionId: Int):List<RegionAreaJoin>

    @Query("SELECT * FROM BaseSkiArea INNER JOIN RegionAreaJoin ON RegionAreaJoin.joinAreaId = BaseSkiArea.area_id WHERE RegionAreaJoin.joinRegionId=:regionId")
    fun getAreasForRegion(regionId: Int): List<BaseSkiArea>

    @Query("SELECT * FROM BaseSkiRegion INNER JOIN RegionAreaJoin ON BaseSkiRegion.regionId=RegionAreaJoin.joinRegionId WHERE RegionAreaJoin.joinAreaId=:areaId")
    fun getRegionsForArea(areaId: Int): List<BaseSkiRegion>

    @Query("SELECT Count(*) FROM RegionAreaJoin")
    fun getSize(): Int

}