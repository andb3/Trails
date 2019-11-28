package com.andb.apps.trails.data.local

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andb.apps.trails.data.model.SkiMap

@Dao
interface MapsDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMap(map: SkiMap)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleMaps(maps: List<SkiMap>)

    @Update
    fun updateMap(map: SkiMap)

    @Delete
    fun deleteMap(map: SkiMap)

    @Query("SELECT * FROM SkiMap WHERE map_id = :id")
    fun getMapByID(id: Int): SkiMap?

    @Query("SELECT * FROM SkiMap")
    fun getAll(): LiveData<List<SkiMap>>

    @Query("SELECT * FROM SkiMap WHERE map_parent = :parentID")
    fun getMapsFromParent(parentID: Int): LiveData<List<SkiMap>>

    @Query("SELECT * FROM SkiMap WHERE map_favorite > -1")
    fun getFavorites(): LiveData<List<SkiMap>>

    @Query("SELECT * FROM SkiMap")
    fun getAllStatic(): List<SkiMap>
}