package com.andb.apps.trails.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.andb.apps.trails.objects.SkiMap

@Dao
interface MapsDao{

    @Insert
    fun insertMap(map: SkiMap)

    @Delete
    fun deleteMap(map: SkiMap)

/*    @Query("SELECT * FROM SkiMap WHERE map_id = :id")
    fun getMapById(id: Int): SkiMap*/

    @Query("SELECT * FROM SkiMap")
    fun getAll(): List<SkiMap>

/*    @Query("SELECT * FROM SkiMap WHERE map_id = :id")
    fun deleteMapById(id: Int)*/
}