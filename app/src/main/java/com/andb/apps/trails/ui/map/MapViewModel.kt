package com.andb.apps.trails.ui.map

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.data.model.SkiMap
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.MapsRepository
import com.andb.apps.trails.util.*
import com.snakydesign.livedataextensions.map

class MapViewModel(val areasRepo: AreasRepository, val mapsRepo: MapsRepository, val fileDownloader: FileDownloader) :
    ViewModel() {

    private val mapID: MutableLiveData<Int> = InitialLiveData(-1)
    private val skiMap: LiveData<SkiMap?> = mapID.mapSuspend { mapsRepo.getMapByID(it) }

    val year: LiveData<Int> = skiMap.map { it?.year }.notNull()
    val areaName: LiveData<String> = skiMap.mapSuspend {
        if (it != null) areasRepo.getAreaByID(it.parentID)?.name ?: "" else ""
    }
    val imageURL: LiveData<String> = skiMap.map { it?.url }.notNull()
    val favorite: LiveData<Boolean> = skiMap.map { it?.isFavorite() }.notNull()

    fun setMap(id: Int) {
        mapID.value = id
    }

    fun downloadCurrent(onDownloaded: (SkiMap) -> Unit) {
        val currentMap = skiMap.value ?: return
        newIoThread {
            fileDownloader.downloadFileExternal(
                currentMap.url, areasRepo.getAreaByID(currentMap.parentID)?.name
                    ?: "no_area", currentMap.year
            )
        }
        onDownloaded.invoke(currentMap)
    }

    fun favoriteMap(liked: Boolean) {
        val currentMap = skiMap.value ?: return
        newIoThread {
            mapsRepo.updateFavorite(currentMap, liked)
        }
    }
}