package com.andb.apps.trails.ui.area

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.model.SkiRegion
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.data.repository.RegionsRepository
import com.andb.apps.trails.util.InitialLiveData
import com.andb.apps.trails.util.ListLiveData
import com.andb.apps.trails.util.equalsUnordered
import com.andb.apps.trails.util.newIoThread
import com.snakydesign.livedataextensions.map
import com.snakydesign.livedataextensions.switchMap

class AreaViewModel(
    private val regionsRepo: RegionsRepository,
    private val mapsRepo: MapsRepository,
    private val areasRepo: AreasRepository
) : ViewModel() {

    val skiArea = MediatorLiveData<SkiArea?>().also { it.value = null }
    val loading = Updater.loadingMaps
    val offline: LiveData<Boolean> = loading.switchMap bool@{ loading ->
        Log.d("areaVMOffline", "loading = $loading")
        if (loading) {
            InitialLiveData<Boolean>(false)
        } else {
            unfilteredMaps.switchMap mapsMatch@{ maps ->
                val mapIds = maps.map { it.id }
                Log.d("areaVMOffline", "mapIds = $mapIds")
                return@mapsMatch skiArea.map { area ->
                    Log.d("areaVMOffline", "areaMapIds = $mapIds")
                    val matches = area?.maps?.equalsUnordered(mapIds) ?: false
                    Log.d("areaVMOffline", "matches = $matches")
                    return@map !matches
                }
            }
        }
    }
    val regions = ListLiveData<SkiRegion>().also { ld ->
        ld.addSource(skiArea) { area: SkiArea? ->
            Log.d("areaViewModel", "regions refreshed")
            if (area != null) {
                newIoThread {
                    ld.clear()
                    ld.addAll(area.parentIDs.mapNotNull { regionsRepo.getRegionByID(it) })
                }
            }
        }
    }

    private val unfilteredMaps = Transformations.switchMap(skiArea) { area ->
        if (area == null) {
            return@switchMap ListLiveData<SkiMap>()
        }
        Log.d("areaViewModel", "maps refreshed not null")
        return@switchMap mapsRepo.getMapsFromArea(area)
    }
    val maps: LiveData<List<SkiMap>> = unfilteredMaps.map { list ->
        list.filter {
            listOf("pdf", "jpg", "png", "gif", "jpeg").contains(it.suffix())
        }
    }


    fun loadArea(id: Int) {
        Log.d("areaViewModel", "loading area - id: $id")
        newIoThread {
            val newArea = areasRepo.getAreaByID(id)
            Log.d("areaViewModel", "loading area - newArea: $newArea")
            skiArea.postValue(newArea)
        }
    }

    fun favoriteMap(map: SkiMap, liked: Boolean) {
        newIoThread {
            mapsRepo.updateFavorite(map, liked)
        }
    }

}