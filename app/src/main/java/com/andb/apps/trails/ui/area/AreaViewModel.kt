package com.andb.apps.trails.ui.area

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.model.SkiRegion
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.data.repository.RegionsRepository
import com.andb.apps.trails.util.ListLiveData
import com.andb.apps.trails.util.equalsUnordered
import com.andb.apps.trails.util.newIoThread
import com.andb.apps.trails.util.notNull
import com.snakydesign.livedataextensions.map
import com.snakydesign.livedataextensions.switchMap

class AreaViewModel(
    private val regionsRepo: RegionsRepository,
    private val mapsRepo: MapsRepository,
    private val areasRepo: AreasRepository
) : ViewModel() {

    val skiArea = MediatorLiveData<SkiArea?>().also { it.value = null }
    val loading = Updater.loadingMaps

    val regions = ListLiveData<SkiRegion>().also { ld ->
        ld.addSource(skiArea.notNull()) { area: SkiArea ->
            Log.d("areaViewModel", "regions refreshed")
            newIoThread {
                ld.clear()
                ld.addAll(area.parentIDs.mapNotNull { regionsRepo.getRegionByID(it) })
            }
        }
    }

    private val unfilteredMaps = skiArea.notNull().switchMap { area ->
        Log.d("areaViewModel", "maps refreshed not null")
        return@switchMap mapsRepo.getMapsFromArea(area)
    }
    val maps: LiveData<List<SkiMap>> = unfilteredMaps.map { unfilteredMaps ->
        unfilteredMaps.filter { it.suffix() in listOf("pdf", "jpg", "png", "gif", "jpeg") }
    }

    val offline = object : MediatorLiveData<Boolean>() {
        var loadingState = true
        var loadedMapIDs = listOf<Int>()
        var areaState: SkiArea? = null

        init {
            addSource(loading) { loadingState = it; update() }
            addSource(skiArea) { areaState = it; update() }
            addSource(unfilteredMaps) { m -> loadedMapIDs = m.map { it.id }; update() }
        }

        fun update() {
            val areaCopy = areaState
            postValue(
                when {
                    loadingState -> false
                    areaCopy == null -> true
                    else -> !areaCopy.maps.equalsUnordered(loadedMapIDs)

                }
            )
        }
    }

    val emptyState = object : MediatorLiveData<Boolean>() {
        var loadingState = true
        var offlineState = true
        var mapsState = listOf<SkiMap>()

        init {
            addSource(loading) { loadingState = it; update() }
            addSource(offline) { offlineState = it; update() }
            addSource(unfilteredMaps) { mapsState = it; update() }
        }

        fun update() {
            postValue(mapsState.isEmpty() && !(loadingState || offlineState))
        }

    }

    fun loadArea(id: Int) {
        Log.d("areaViewModel", "loading area - id: $id")
        if (skiArea.value == null) {
            newIoThread {
                if (Updater.areaUpdateNeeded()) {
                    Updater.updateAreas()
                }
                val newArea = areasRepo.getAreaByID(id)
                Log.d("areaViewModel", "loading area - newArea: $newArea")
                skiArea.postValue(newArea)
            }
        }

        if (Updater.mapUpdateNeeded()) {
            newIoThread {
                Updater.updateMaps()
            }
        }
    }

    fun favoriteMap(map: SkiMap, liked: Boolean) {
        newIoThread {
            mapsRepo.updateFavorite(map, liked)
        }
    }

}