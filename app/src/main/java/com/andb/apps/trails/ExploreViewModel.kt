package com.andb.apps.trails

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

class ExploreViewModel : ViewModel() {
    private val regionStack = ListLiveData<SkiRegion>()

    private var baseRegionOffline: Int = -1


    fun getParentRegionName(): LiveData<String> {
        return Transformations.map(regionStack) {
            if (baseRegionOffline.isPositive()) {
                return@map ""
            }
            return@map it.lastOrNull()?.name ?: ""
        }
    }

    fun getChildRegions(): LiveData<List<SkiRegion?>> {
        return Transformations.switchMap(regionStack) { regionStack ->
            Log.d("liveDataChanged", "regions changed on stack update")
            if (regionStack.isEmpty()) {//don't run it.last() / load if parent still loading
                return@switchMap ListLiveData<SkiRegion?>()
            }
            return@switchMap RegionsRepo.getRegionsFromParent(regionStack.last()).also {
                it.refresh()
            }
        }
    }

    fun getChildAreas(): LiveData<List<SkiArea?>> {
        return Transformations.switchMap(regionStack) { regionStack ->
            Log.d("liveDataChanged", "areas changed on stack update")
            if (regionStack.isEmpty()) {//don't run it.last() / load if parent still loading
                return@switchMap ListLiveData<SkiArea?>()
            }
            return@switchMap AreasRepo.getAreasFromRegion(regionStack.last()).also {
                it.refresh()
            }
        }
    }

    /**Returns LiveData constantly updating with chips for the current child regions. Chips can have a child region or area, or be null for both to show offline**/
    fun getChips(): LiveData<List<ChipItem>> {
        return ChipLiveData(getChildRegions())
    }

    class ChipLiveData(source: LiveData<List<SkiRegion?>>) : ListLiveData<ChipItem>() {

        private val regionCache = ArrayList<SkiRegion>()

        init {
            addSource(source) { regions ->
                val notAlreadyLoaded = regions.filterNotNull().minus(regionCache)
                notAlreadyLoaded.forEach { childRegion ->
                    regionCache.add(childRegion)
                    newIoThread {
                        val chips = if (childRegion.childIds.isEmpty()) {
                            val chipChildren = AreasRepo.getAreasFromRegionNonLive(childRegion)
                            chipChildren.sortedByDescending { it?.maps?.size }.take(2)
                                .map { ChipItem(childRegion.id, area = it) }
                        } else {
                            val chipChildren = RegionsRepo.getRegionsFromParentNonLive(childRegion)
                            chipChildren.sortedByDescending { it?.mapCount }.take(2)
                                .map { ChipItem(childRegion.id, region = it) }
                        }
                        mainThread {
                            this@ChipLiveData.addAll(chips)
                        }
                    }
                }
            }
        }
    }

    fun getLoadingState(): LiveData<Boolean> {

/*        return MediatorLiveData<Boolean>().also {

            it.addSource(getChildRegions()) { regions ->
                if (regions.isNotEmpty()) {
                    it.postValue(regionStack.last().childIds.size != regions.size)
                }
            }

            it.addSource(getChildAreas()) { areas ->
                if (areas.isNotEmpty()) {
                    it.postValue(regionStack.last().areaIds.size != areas.size)
                }
            }
        }*/

        return Transformations.switchMap(regionStack) parentSwitchMap@{ parents ->
            return@parentSwitchMap Transformations.switchMap(getChildRegions()) regionSwitchMap@{ childRegions ->
                return@regionSwitchMap Transformations.switchMap(getChildAreas()) areaSwitchMap@{ childAreas ->
                    return@areaSwitchMap Transformations.map(getOffline()) offlineMap@{ offline ->
                        val parentRegion = parents.lastOrNull() ?: return@offlineMap true
                        return@offlineMap offline == false
                                && (parentRegion.childIds.size != childRegions.size //true if not all child regions and areas are loaded
                                || parentRegion.areaIds.size != childAreas.size)
                    }
                }
            }
        }

    }

    fun getOffline(): LiveData<Boolean> {
        return Transformations.switchMap(getChildRegions()) regionSwitchMap@{ childRegions ->
            return@regionSwitchMap Transformations.switchMap(getChildAreas()) areaSwitchMap@{ childAreas ->
                return@areaSwitchMap Transformations.map(regionStack) {
                    //region needed for baseRegionOffline refresh
                    return@map baseRegionOffline.isPositive()
                            || childRegions.contains(null)
                            || childAreas.contains(null)
                }
            }

        }
    }

    fun setBaseRegion(id: Int) {
        regionStack.value = emptyList()
        newIoThread {
            val region = RegionsRepo.getRegionById(id)
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


    fun nextRegion(region: SkiRegion, addToStack: Boolean = true) {
        if (addToStack) {
            baseRegionOffline = -1
            addRegionToStack(region)
        }
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
        return regionStack.value.size == 1 || isFirstLoad()
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


class ChipItem(val parentId: Int, val region: SkiRegion? = null, val area: SkiArea? = null)

