package com.andb.apps.trails.ui.area

import android.util.Log
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.MapTag
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.data.repository.RegionsRepository
import com.andb.apps.trails.util.asyncIO
import com.andb.apps.trails.util.newIoThread
import com.andb.apps.trails.util.zip
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.*

@OptIn(ExperimentalCoroutinesApi::class)
class AreaViewModel(
    private val regionsRepo: RegionsRepository,
    private val mapsRepo: MapsRepository,
    private val areasRepo: AreasRepository,
    private val areaID: Int
) : ViewModel() {

    private val currentID: MutableStateFlow<Int> = MutableStateFlow(areaID)
    val area: Flow<SkiArea> = currentID.map {
        val area = asyncIO { areasRepo.getAreaByID(it) }.await()
        println("got area = $area")
        return@map area
    }
    private val unfilteredMaps = area.flatMapLatest {
        println("unfilteredMaps - flatMapLatest | area = $it")
        mapsRepo.getMapsFromArea(it)
    }
    val maps = unfilteredMaps.map { unfiltered ->
        unfiltered
            .filter { it.suffix() in listOf("pdf", "jpg", "png", "gif", "jpeg") }
            .filter { MapTag.PLAN !in it.tags }
    }
    val regions = area.map { area ->
        area.parentIDs.mapNotNull { regionsRepo.getRegionByID(it) }
    }

    val loading = Updater.loadingMaps
    val offline = area
        .zip(unfilteredMaps) { area, maps -> area.maps == maps.map { it.id } }
        .zip(loading) { mapsMatch, loading -> !mapsMatch && loading }

    val emptyState = maps.zip(offline, loading) { maps, offline, loading ->
        maps.isEmpty() && !offline && !loading
    }


    fun checkUpdates() {
        Log.d("areaViewModel", "updating area with maps- id: $areaID")
        if (Updater.areaUpdateNeeded()) {
            newIoThread {
                Updater.updateAreas()
            }
        }
        if (Updater.mapUpdateNeeded()) {
            newIoThread {
                Updater.updateMaps()
            }
        }
    }

    fun favoriteMap(map: SkiMap, liked: Boolean) {
        newIoThread { mapsRepo.updateFavorite(map, liked) }
    }

    fun favoriteArea(liked: Boolean) {
        newIoThread { areasRepo.updateFavorite(area.first(), liked) }
    }
}