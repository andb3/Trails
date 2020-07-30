package com.andb.apps.trails.data.repository

import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import kotlinx.coroutines.flow.Flow

interface MapsRepository {
    fun getMapsFromArea(area: SkiArea): Flow<List<SkiMap>>
    fun getFavorites(): Flow<List<SkiMap>>
    suspend fun getMapByID(id: Int): SkiMap?
    suspend fun updateFavorite(map: SkiMap, favorite: Boolean)
    suspend fun updateFavorite(map: SkiMap, favoriteIndex: Int)
}