package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.map
import com.andb.apps.trails.data.local.AreasDao
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.model.SkiArea

class AreasRepositoryImpl(private val areasDao: AreasDao) : AreasRepository {

    override fun getAll() = areasDao.getAll()
    override fun getFavorites(): LiveData<List<SkiArea>> = areasDao.getFavorites().map { list ->
        list.sortedBy {
            Prefs.favoritesOrderArea.indexOf(it.id)
        }
    }

    override suspend fun getAreaByID(id: Int): SkiArea = areasDao.getAreaByID(id)
    override suspend fun search(text: String) = areasDao.search(text)
    override suspend fun updateFavorite(area: SkiArea, favorite: Boolean) {
        area.favorite = favorite
        areasDao.updateArea(area)
        if (favorite) {
            Prefs.favoritesOrderArea = Prefs.favoritesOrderArea.plus(area.id)
        } else {
            Prefs.favoritesOrderArea = Prefs.favoritesOrderArea.minus(area.id)
        }
    }

    override suspend fun updateFavorite(area: SkiArea, favoriteIndex: Int) {
        val mutable = Prefs.favoritesOrderArea.toMutableList()
        mutable.remove(area.id)
        mutable.add(favoriteIndex, area.id)
        Prefs.favoritesOrderArea = mutable
    }
}