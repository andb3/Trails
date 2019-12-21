package com.andb.apps.trails.data.repository

import android.util.Log
import androidx.lifecycle.LiveData
import com.andb.apps.trails.data.local.MapsDao
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.util.toUnordered

class MapsRepositoryImpl(private val mapsDao: MapsDao) : MapsRepository {

    override fun getMapsFromArea(area: SkiArea): LiveData<List<SkiMap>> =
        mapsDao.getMapsFromParent(area.id)

    override fun getFavorites(): LiveData<List<SkiMap>> = mapsDao.getFavorites()
    override suspend fun getMapByID(id: Int): SkiMap? = mapsDao.getMapByID(id)

    override suspend fun updateFavorite(map: SkiMap, favorite: Boolean) {
        if (!favorite) {
            val oldFavoriteIndex = map.favorite
            map.favorite = -1
            mapsDao.updateMap(map)
            mapsDao.getAllStatic().filter { it.favorite > oldFavoriteIndex }.forEach {
                it.favorite--
                mapsDao.updateMap(it)
            }
        } else {
            map.favorite = if (!favorite) -1 else (mapsDao.getAllStatic().maxBy { it.favorite }?.favorite
                ?: -1) + 1
            mapsDao.updateMap(map)
        }
    }

    override suspend fun updateFavorite(map: SkiMap, favoriteIndex: Int) {
        val oldIndex = map.favorite

        //returns -1 if oldIndex is smaller (every map needs to step down to fill the gap) or 1 if larger (step up to fill gap)
        val increment = oldIndex.compareTo(favoriteIndex)
        Log.d("updateFavorite", "increment = $increment")

        val range = oldIndex toUnordered favoriteIndex
        val maps = mapsDao.getAllStatic().filter { it.favorite in range }
        maps.forEach {
            if (it.id == map.id) {
                it.favorite = favoriteIndex
            } else {
                it.favorite += increment
            }
        }
        mapsDao.updateMaps(maps)
    }

}