package com.andb.apps.trails.ui.favorites

import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.util.newIoThread
import com.snakydesign.livedataextensions.map
import com.snakydesign.livedataextensions.switchMap

class FavoritesViewModel(val areasRepo: AreasRepository, val mapsRepo: MapsRepository) :
    ViewModel() {
    val maps = mapsRepo.getFavorites().map { mapList -> mapList.sortedBy { it.favorite } }
    val areas = areasRepo.getFavorites().map { areaList -> areaList.sortedBy { it.favorite } }
    val empty = maps.switchMap { maps -> areas.map { areas -> maps.isEmpty() && areas.isEmpty() } }

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

    fun updateMapFavorite(map: SkiMap, index: Int) {
        newIoThread {
            mapsRepo.updateFavorite(map, index)
        }
    }

    fun updateAreaFavorite(area: SkiArea, index: Int) {
        newIoThread {
            areasRepo.updateFavorite(area, index)
        }
    }
}