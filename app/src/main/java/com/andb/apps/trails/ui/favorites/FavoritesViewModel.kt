package com.andb.apps.trails.ui.favorites

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.util.newIoThread
import com.snakydesign.livedataextensions.map

class FavoritesViewModel(val areasRepo: AreasRepository, val mapsRepo: MapsRepository) :
    ViewModel() {
    fun getFavoriteMaps(): LiveData<List<SkiMap>> =
        mapsRepo.getFavorites().map { mapList -> mapList.sortedBy { it.favorite } }

    fun getFavoriteAreas(): LiveData<List<SkiArea>> =
        areasRepo.getFavorites().map { areaList -> areaList.sortedBy { it.favorite } }

    fun favoriteMap(map: SkiMap, liked: Boolean) {
        newIoThread {
            mapsRepo.updateFavorite(map, liked)
        }
    }

    fun favoriteArea(area: SkiArea, liked: Boolean) {
        newIoThread {
            areasRepo.updateFavorite(area, liked)
        }
    }
}