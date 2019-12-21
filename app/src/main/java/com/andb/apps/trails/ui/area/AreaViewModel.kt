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
import com.andb.apps.trails.util.ListLiveData
import com.andb.apps.trails.util.mainThread
import com.andb.apps.trails.util.newIoThread

class AreaViewModel(private val regionsRepo: RegionsRepository,
                    private val mapsRepo: MapsRepository,
                    private val areasRepo: AreasRepository) : ViewModel() {

    val skiArea = MediatorLiveData<SkiArea?>().also { it.value = null }
    val offline = MediatorLiveData<Boolean>().also { it.value = false }
    val loading = Updater.loadingMaps
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

    val maps: LiveData<List<SkiMap>> = Transformations.switchMap(skiArea) { area ->
        if(area==null){
            return@switchMap ListLiveData<SkiMap>()
        }
        Log.d("areaViewModel", "maps refreshed not null")
        return@switchMap mapsRepo.getMapsFromArea(area)
    }


    fun loadArea(id: Int) {
        Log.d("areaViewModel", "loading area - id: $id")
        offline.value = false
        newIoThread {
            val newArea = areasRepo.getAreaByID(id)
            Log.d("areaViewModel", "loading area - newArea: $newArea")
            mainThread {
                if (newArea != null) {
                    skiArea.value = newArea
                    offline.value = false
                } else {
                    offline.value = true
                }
            }
        }
    }

    fun favoriteMap(map: SkiMap, liked: Boolean) {
        newIoThread {
            mapsRepo.updateFavorite(map, liked)
        }
    }

}