package com.andb.apps.trails.data.local

import androidx.room.*
import com.andb.apps.trails.data.model.SkiMap
import kotlinx.coroutines.flow.Flow

@Dao
interface MapsDao{

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertMap(map: SkiMap)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertMultipleMaps(maps: List<SkiMap>)

    @Update
    fun updateMap(map: SkiMap)

    @Update
    fun updateMaps(maps: List<SkiMap>)

    @Delete
    fun deleteMap(map: SkiMap)

    @Query("SELECT * FROM SkiMap WHERE map_id = :id")
    fun getMapByID(id: Int): SkiMap?

    @Query("SELECT * FROM SkiMap")
    fun getAll(): Flow<List<SkiMap>>

    @Query("SELECT * FROM SkiMap WHERE map_parent = :parentID")
    fun getMapsFromParent(parentID: Int): Flow<List<SkiMap>>

    @Query("SELECT * FROM SkiMap WHERE map_favorite = 1")
    fun getFavorites(): Flow<List<SkiMap>>

    @Query("SELECT * FROM SkiMap")
    fun getAllStatic(): List<SkiMap>
}