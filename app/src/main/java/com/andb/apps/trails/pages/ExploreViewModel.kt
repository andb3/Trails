package com.andb.apps.trails.pages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegionTree
import com.andb.apps.trails.objects.toTree
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.*

class ExploreViewModel : ViewModel() {

    var parents = listOf<SkiRegionTree>()
    var regionStack = ListLiveData<SkiRegionTree>()
    var baseRegionOffline = -1


    fun setup(regionID: Int) {
        newIoThread {
            parents = RegionsRepo.getParents().map { it.toTree() }
            setBaseRegion(regionID)
        }
    }

    val parent = object : MediatorLiveData<SkiRegionTree>() {
        init {
            value = regionStack.lastOrNull()
            addSource(regionStack) {
                value = it.lastOrNull()
            }
        }
    }

    val loading = InitialLiveData(false).apply {
        addSource(RegionsRepo.loading) { this.value = it }
        addSource(AreasRepo.loading) { this.value = it }
    }


    val offline: LiveData<Boolean> = Transformations.map(parent) { parent ->
        val parentRegion = parent ?: return@map true
        return@map parentRegion.offline
    }

    fun setBaseRegion(id: Int) {
        val possibleRegion = parents.firstOrNull { it.id == id }
        if (possibleRegion!=null) {
            resetStackWith(possibleRegion)
        } else {
            baseRegionOffline = id
            Log.d("baseRegionOffline", "setting to $baseRegionOffline")
        }
    }

    private fun resetStackWith(region: SkiRegionTree) {
        regionStack.clear()
        regionStack.add(region)
    }

    fun nextRegion(region: SkiRegionTree) {
        regionStack.add(region)
    }

    fun backRegion() {
        regionStack.drop(1)
    }


    fun isFirstLoad(): Boolean {
        return regionStack.size == 0
    }

    private fun isBaseRegion(): Boolean {
        return regionStack.size <= 1
    }

    fun isBackPossible(): Boolean {
        return !isBaseRegion()
    }

}

/**MediatorLiveData of List<T> with better sync to backing list and better modification methods**/
open class ListLiveData<T>(initialList: List<T> = emptyList()) : MediatorLiveData<List<T>>() {
    private val backingList: MutableList<T> = initialList.toMutableList()

    fun size() = backingList.size

    fun add(item: T) {
        backingList.add(item)
        postValue(backingList)
    }

    fun add(item: T, index: Int = backingList.size) {
        backingList.add(index, item)
        postValue(backingList)
    }

    fun addAll(items: Collection<T>) {
        backingList.addAll(items)
        postValue(backingList)
    }

    fun remove(item: T) {
        backingList.remove(item)
        postValue(backingList)
    }

    fun removeAt(index: Int) {
        backingList.removeAt(index)
        postValue(backingList)
    }

    fun drop(by: Int) {
        backingList.dropBy(by)
        postValue(backingList)
    }

    fun clear() {
        backingList.clear()
        postValue(backingList)
    }

    fun last(): T {
        return backingList.last()
    }


    fun lastOrNull(): T? {
        return backingList.lastOrNull()
    }

    override fun postValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.postValue(backingList)
    }

    override fun setValue(value: List<T>?) {
        if (value !== backingList) {
            backingList.clear()
            backingList.addAll(value.orEmpty())
        }
        super.setValue(backingList)
    }

    override fun getValue(): List<T> {
        return backingList
    }

    val size: Int
        get() = backingList.size

}


class ChipItem(val parentID: Int, val region: SkiRegionTree? = null, val area: SkiArea? = null)

