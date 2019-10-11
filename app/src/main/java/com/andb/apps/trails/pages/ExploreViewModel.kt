package com.andb.apps.trails.pages

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.*
import jonathanfinerty.once.Once
import java.util.concurrent.TimeUnit

class ExploreViewModel : ViewModel() {
    private val regionStack = ListLiveData<SkiRegion>()
    var baseRegionOffline = -1

    fun getParentRegionName(): LiveData<String> {
        return Transformations.map(regionStack) {
            return@map it.lastOrNull()?.name ?: "Offline"
        }
    }

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

    /**LiveData constantly updating with chips for the current child regions. Chips can have a child region or area, or be null for both to show offline**/
    val chips = ChipLiveData(childRegions)

    class ChipLiveData(source: LiveData<List<SkiRegion>>) : ListLiveData<ChipItem>() {
        init {
            addSource(source) { regions ->
                regions.forEach { region ->
                    if (region.childIDs.isNotEmpty()) {
                        addSource(RegionsRepo.getRegionsFromParent(region)) { childChildren ->
                            addAll(childChildren.sortedBy { it.mapCount }.take(2).map { ChipItem(region.id, region = it) })
                        }
                    } else {
                        addSource(AreasRepo.getAreasFromRegion(region)) { childAreas ->
                            addAll(childAreas.sortedBy { it.maps.size }.take(2).map { ChipItem(region.id, area = it) })
                        }
                    }
                }
            }
        }
    }

    //TODO: convert to objects updated w/ this method
    val loading = Transformations.map(regionStack) { _ ->
        return@map !Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")
    }


    val offline: LiveData<Boolean> = Transformations.switchMap(childRegions) regionSwitchMap@{ childRegions ->
        return@regionSwitchMap Transformations.switchMap(childAreas) areaSwitchMap@{ childAreas ->
            return@areaSwitchMap Transformations.map(regionStack) { regionStack ->
                val parentRegion = regionStack.lastOrNull() ?: return@map true
                return@map parentRegion.childIDs != childRegions.map { it.id } || parentRegion.areaIDs != childAreas.map { it.id }
            }
        }
    }

    fun setBaseRegion(id: Int) {
        regionStack.value = emptyList()
        newIoThread {
            val region = RegionsRepo.getRegionByID(id)
            mainThread {
                if (region != null) {
                    baseRegionOffline = -1
                    resetStackWith(region)
                    Log.d("baseRegionLoaded", "id: $id")
                } else {
                    baseRegionOffline = id
                    regionStack.refresh()
                    Log.d("baseRegionOffline", "id: $id")
                }
            }
        }
    }

    private fun resetStackWith(region: SkiRegion) {
        regionStack.value = listOf(region)
    }

    private fun addRegionToStack(region: SkiRegion) {
        regionStack.add(region)
    }


    fun nextRegion(region: SkiRegion) {
        addRegionToStack(region)
    }

    fun backRegion() {
        regionStack.drop(1)
    }

    fun refresh() {
        if (!baseRegionOffline.isNegative()) {
            Log.d("refresh", "base")
            setBaseRegion(baseRegionOffline)
        }
        Log.d("refresh", "children")
        regionStack.refresh() //by resending the value, the switchMap will rerun for the children
    }

    fun isFirstLoad(): Boolean {
        return regionStack.value.isEmpty()
    }

    fun isBaseRegion(): Boolean {
        return regionStack.value.size <= 1
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


class ChipItem(val parentID: Int, val region: SkiRegion? = null, val area: SkiArea? = null)

