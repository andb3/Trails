package com.andb.apps.trails

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.pages.ListLiveData
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.MapsRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread

class AreaViewModel : ViewModel() {

    var skiArea = MediatorLiveData<SkiArea?>().also { it.value = null }
    var offline = MediatorLiveData<Boolean>().also { it.value = false }
    val regions = ListLiveData<SkiRegion>().also { ld ->
        ld.addSource(skiArea) { area: SkiArea? ->
            Log.d("areaViewModel", "regions refreshed")
            if (area != null) {
                newIoThread {
                    ld.clear()
                    ld.addAll(RegionsRepo.findAreaParents(area))
                }
            }
        }
    }

/*    val maps = ListLiveData<SkiMap>().also { ld ->
        ld.addSource(skiArea) { area: SkiArea? ->
            if (area != null) {
                newIoThread {
                    val newMaps = MapsRepo.getMapsNonLive(area).sortedByDescending { it.year }
                    if (newMaps.map { it.id }.toSet() != area.maps.toSet()) {
                        Log.d("areaFragmentOffline", "newMaps: ${newMaps.map {it.id}}, area maps: ${area.maps}")
                        offline.postValue(true)
                    }
                    ld.addAll(newMaps)
                }
            }
        }
    }*/

    val maps: LiveData<List<SkiMap>> = Transformations.switchMap(skiArea) { area ->
        if(area==null){
            return@switchMap ListLiveData<SkiMap>()
        }
        Log.d("areaViewModel", "maps refreshed not null")
        return@switchMap MapsRepo.getMapsFromArea(area)
    }


    fun loadArea(id: Int) {
        offline.value = false
        newIoThread {
            val newArea = AreasRepo.getAreaByID(id)
            mainThread {
                if(!(newArea == null && skiArea.value != null)) { //don't have null value overtake not-null one
                    skiArea.value = newArea
                    offline.value = skiArea.value == null
                }else{
                    skiArea.value = skiArea.value
                }
            }
        }
    }


}