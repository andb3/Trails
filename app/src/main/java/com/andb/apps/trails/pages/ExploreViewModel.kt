package com.andb.apps.trails.pages

import android.util.Log
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegionTree
import com.andb.apps.trails.objects.toTree
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.*

class ExploreViewModel : ViewModel() {

    var parents = listOf<SkiRegionTree>()
    var regionStack = mutableListOf<SkiRegionTree>()
    var baseRegionOffline = -1


    fun setup(regionID: Int, onSetup: ()->Unit) {
        newIoThread {
            parents = RegionsRepo.getParents().map { it.toTree() }
            Log.d("exploreViewModel setup", "parents: $parents")
            setBaseRegion(regionID)
        }
    }

    val parent = object : MediatorLiveData<SkiRegionTree>() {
        init {
            value = regionStack.lastOrNull()
            /*addSource(regionStack){
                value = it.lastOrNull()
            }*/
        }
    }

/*    fun getParentRegionName(): LiveData<String> {
        return Transformations.map(regionStack) {
            return@map it.lastOrNull()?.name ?: "Offline"
        }
    }*/
/*

    val childRegions = Transformations.switchMap(regionStack) { regionStack ->
        Log.d("liveDataChanged", "regions changed on stack update")
        if (regionStack.isEmpty()) {//don't run it.last() / load if parent still loading
            return@switchMap ListLiveData<SkiRegion>()
        }
        return@switchMap RegionsRepo.getRegionsFromParent(regionStack.last())
    }


    val childAreas = Transformations.switchMap(regionStack) { regionStack ->
        Log.d("liveDataChanged", "areas changed on stack update")
        if (regionStack.isEmpty()) {//don't run it.last() / load if parent still loading
            return@switchMap ListLiveData<SkiArea>() as LiveData<List<SkiArea>>
        }
        return@switchMap AreasRepo.getAreasFromRegion(regionStack.last())
    }

    */
    /**LiveData constantly updating with chips for the current child regions. Chips can have a child region or area, or be null for both to show offline**//*

    val chips = ChipLiveData(childRegions)

    class ChipLiveData(source: LiveData<List<SkiRegion>>) : ListLiveData<ChipItem>() {
        init {
            */
/*addSource(source) { regions ->
                regions.forEach { region ->
                    newIoThread {
                        if (region.childIDs.isNotEmpty()) {
                            addSource(RegionsRepo.getRegionsFromParent(region)) { childChildren ->
                                addAll(childChildren.sortedByDescending { it.mapCount }.take(2).map { ChipItem(region.id, region = it) })
                            }
                        } else {
                            addSource(AreasRepo.getAreasFromRegion(region)) { childAreas ->
                                addAll(childAreas.sortedByDescending { it.maps.size }.take(2).map { ChipItem(region.id, area = it) })
                            }
                        }
                    }

                }
            }*//*

        }
    }
*/

    val loading = InitialLiveData(false).apply {
        addSource(RegionsRepo.loading) { this.value = it }
        addSource(AreasRepo.loading) { this.value = it }
    }


/*    val offline: LiveData<Boolean> = Transformations.map(parent) { parent ->
        val parentRegion = parent ?: return@map true
        return@map parentRegion.offline
    }*/

    fun setBaseRegion(id: Int) {
        Log.d("setBaseRegion", "loading: ${loading.value}, parents: $parents")
        if (!loading.value) {
            resetStackWith(parents.first { it.id == id })
            Log.d("setBaseRegion", "new regionStack: ${regionStack}")
        }else{
            baseRegionOffline = id
        }
    }

    private fun resetStackWith(region: SkiRegionTree) {
        regionStack.clear()
        regionStack.add(region)
        //parent.value = regionStack.lastOrNull()
    }

    fun nextRegion(region: SkiRegionTree) {
        regionStack.add(region)
        //parent.value = regionStack.lastOrNull()
    }

    fun backRegion() {
        regionStack.drop(1)
        //parent.value = regionStack.lastOrNull()
    }

/*
    fun refresh() {
        if (!baseRegionOffline.isNegative()) {
            Log.d("refresh", "base")
            setBaseRegion(baseRegionOffline)
        }
        Log.d("refresh", "children")
        //parent.value = regionStack.lastOrNull() //by resending the value, the switchMap will rerun for the children
    }
*/

    fun isFirstLoad(): Boolean {
        return regionStack.isEmpty()
    }

    fun isBaseRegion(): Boolean {
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

}


class ChipItem(val parentID: Int, val region: SkiRegionTree? = null, val area: SkiArea? = null)

