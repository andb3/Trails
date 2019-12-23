package com.andb.apps.trails.data.repository

import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.local.MapsDao
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.snakydesign.livedataextensions.map

class MapsRepositoryImpl(private val mapsDao: MapsDao) : MapsRepository {

    override fun getMapsFromArea(area: SkiArea): LiveData<List<SkiMap>> =
        mapsDao.getMapsFromParent(area.id)

    override fun getFavorites(): LiveData<List<SkiMap>> = mapsDao.getFavorites().map { list ->
        list.sortedBy { Prefs.favoritesOrderMap.indexOf(it.id) }
    }

    override suspend fun getMapByID(id: Int): SkiMap? = mapsDao.getMapByID(id)
    override suspend fun updateFavorite(map: SkiMap, favorite: Boolean) {
        map.favorite = favorite
        mapsDao.updateMap(map)
        if (favorite) {
            Prefs.favoritesOrderMap = Prefs.favoritesOrderMap.plus(map.id)
        } else {
            Prefs.favoritesOrderMap = Prefs.favoritesOrderMap.minus(map.id)
        }
    }

    override suspend fun updateFavorite(map: SkiMap, favoriteIndex: Int) {
        val mutable = Prefs.favoritesOrderMap.toMutableList()
        mutable.remove(map.id)
        mutable.add(favoriteIndex, map.id)
        Prefs.favoritesOrderMap = mutable
    }

}

