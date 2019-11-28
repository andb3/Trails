package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap

interface MapsRepository {
    fun getMapsFromArea(area: SkiArea): LiveData<List<SkiMap>>
    fun getFavorites(): LiveData<List<SkiMap>>
    suspend fun getMapByID(id: Int): SkiMap?
    suspend fun updateFavorite(map: SkiMap, favorite: Boolean)
    suspend fun updateFavorite(map: SkiMap, favoriteIndex: Int)
}