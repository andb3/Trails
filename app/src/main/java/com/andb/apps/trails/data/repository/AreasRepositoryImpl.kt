package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.local.AreasDao
import com.andb.apps.trails.data.model.SkiArea

class AreasRepositoryImpl(private val areasDao: AreasDao) : AreasRepository {

    override fun getAll() = areasDao.getAll()
    override fun getFavorites(): LiveData<List<SkiArea>> = areasDao.getFavorites()
    override suspend fun getAreaByID(id: Int): SkiArea? = areasDao.getAreaByID(id)
    override suspend fun search(text: String) = areasDao.search(text)
    override suspend fun updateFavorite(area: SkiArea, favorite: Boolean) {
        if (!favorite) {
            val oldFavoriteIndex = area.favorite
            area.favorite = -1
            areasDao.updateArea(area)
            areasDao.getAllStatic().filter { it.favorite > oldFavoriteIndex }.forEach {
                it.favorite--
                areasDao.updateArea(it)
            }
        } else {
            area.favorite = if (!favorite) -1 else (areasDao.getAllStatic().maxBy { it.favorite }?.favorite
                ?: -1) + 1
            areasDao.updateArea(area)
        }
    }

    override suspend fun updateFavorite(area: SkiArea, favoriteIndex: Int) {
        area.favorite = favoriteIndex
        areasDao.updateArea(area)
    }
}