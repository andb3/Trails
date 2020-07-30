package com.andb.apps.trails.ui.explore

import androidx.lifecycle.*
import com.andb.apps.trails.data.model.SkiArea
import com.andb.apps.trails.data.model.SkiRegion
import com.andb.apps.trails.data.model.SkiRegionTree
import com.andb.apps.trails.data.model.toTree
import com.andb.apps.trails.data.remote.Updater
import com.andb.apps.trails.data.repository.AreasRepository
import com.andb.apps.trails.data.repository.RegionsRepository
import com.andb.apps.trails.util.ListLiveData
import com.andb.apps.trails.util.newIoThread
import com.andb.apps.trails.util.notNull

class ExploreViewModel(val regionsRepo: RegionsRepository, val areasRepo: AreasRepository) :
    ViewModel() {

    private val allRegions: LiveData<List<SkiRegion>> = regionsRepo.getAll()
    private val allAreas: LiveData<List<SkiArea>> = areasRepo.getAll()
    private var regionStack = ListLiveData<Int>()


    fun setup(regionID: Int) {
        setBaseRegion(regionID)
    }

    val tree: LiveData<SkiRegionTree> = regionStack.switchMap { stack ->
        allRegions.switchMap { regions ->
            allAreas.map { areas ->
                regions.find { it.id == stack.last() }?.toTree(regions, areas)
            }
        }
    }.notNull()

    val loading = object : MediatorLiveData<Boolean>() {
        var loadingRegions = false
        var loadingAreas = false

        init {
            this.value = false
            addSource(Updater.loadingRegions.asLiveData()) { loadingRegions = it; refresh() }
            addSource(Updater.loadingAreas.asLiveData()) { loadingAreas = it; refresh() }
        }

        fun refresh() {
            this.postValue(loadingRegions || loadingAreas)
        }
    }

    val offline: LiveData<Boolean> = Transformations.map(tree) { tree ->
        val parentRegion = tree ?: return@map true
        return@map parentRegion.offline
    }

    fun setBaseRegion(id: Int) {
        regionStack.clear()
        regionStack.add(id)
    }

    fun nextRegion(regionID: Int) {
        regionStack.add(regionID)
    }

    fun backRegion() {
        regionStack.drop(1)
    }

    fun refresh() {
        regionStack.postValue(regionStack.value)
    }

    fun isFirstLoad(): Boolean {
        return regionStack.size == 0
    }

    fun isBaseRegion(): Boolean {
        return regionStack.size <= 1
    }

    fun isBackPossible(): Boolean {
        return !isBaseRegion()
    }

    fun updateFavorite(area: SkiArea, favorite: Boolean) {
        area.also {
            newIoThread {
                areasRepo.updateFavorite(area, favorite)
            }
        }
    }

}
