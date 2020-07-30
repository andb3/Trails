package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.model.SkiArea

interface AreasRepository {
    fun getAll(): LiveData<List<SkiArea>>
    fun getFavorites(): LiveData<List<SkiArea>>
    suspend fun getAreaByID(id: Int): SkiArea
    suspend fun search(text: String): List<SkiArea>
    suspend fun updateFavorite(area: SkiArea, favorite: Boolean)
    suspend fun updateFavorite(area: SkiArea, favoriteIndex: Int)
}