package com.andb.apps.trails.database

import androidx.room.*
import com.andb.apps.trails.objects.BaseSkiArea


@Dao
interface AreasDao {

    @Insert
    fun insertArea(area: BaseSkiArea)

    @Update
    fun updateArea(area: BaseSkiArea)

    @Delete
    fun deleteArea(area: BaseSkiArea)

    @Query("SELECT * FROM BaseSkiArea WHERE area_id = :id")
    fun getAreasById(id: Int): List<BaseSkiArea>

    @Query("SELECT * FROM BaseSkiArea")
    fun getAll(): List<BaseSkiArea>

    @Query("SELECT * FROM BaseSkiArea WHERE favorite = 1")
    fun getFavorites(): List<BaseSkiArea>

    @Query("SELECT Count(*) FROM BaseSkiArea")
    fun getSize(): Int

    @Query("SELECT * FROM BaseSkiArea WHERE name LIKE :text")
    fun search(text: String): List<BaseSkiArea>
}