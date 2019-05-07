package com.andb.apps.trails.database

import androidx.lifecycle.LiveData
import androidx.room.*
import com.andb.apps.trails.objects.SkiMap

@Dao
interface MapsDao{

    @Insert
    fun insertMap(map: SkiMap)

    @Update
    fun updateMap(map: SkiMap)

    @Delete
    fun deleteMap(map: SkiMap)

    @Query("SELECT * FROM SkiMap WHERE map_id = :id")
    fun getMapById(id: Int): SkiMap

    @Query("SELECT * FROM SkiMap")
    fun getAll(): LiveData<List<SkiMap>>

    @Query("SELECT * FROM SkiMap WHERE map_parent = :parentId")
    fun getMapsFromParent(parentId: Int): List<SkiMap>

    @Query("SELECT * FROM SkiMap WHERE map_favorite = 1")
    fun getFavorites(): List<SkiMap>
}