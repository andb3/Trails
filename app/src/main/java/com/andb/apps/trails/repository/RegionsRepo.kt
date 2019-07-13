package com.andb.apps.trails.repository

import android.util.Log
import android.util.Log.d
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.addNotNull
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.RegionXMLParser
import kotlinx.coroutines.*

object RegionsRepo {

    private val regions by lazy { ArrayList(regionsDao().getAllStatic()) }
    private val currentlyDownloading = mutableMapOf<Int, Deferred<SkiRegion?>>()

    fun init(lifecycleOwner: LifecycleOwner) {
        regionsDao().getAll().observe(lifecycleOwner, Observer { regions ->
            this.regions.clear()
            this.regions.addAll(regions)
            d("regionsRepo", "init")
        })
    }

    fun getRegionsFromParent(parent: SkiRegion): ListLiveData<SkiRegion?> {
        val childRegions: MutableList<SkiRegion?> = regions.filter { it.parentId == parent.id }
            .toMutableList()
        //Log.d("getRegionsFromParent", "local values: ${localValues.map { it.name }}")
        //Log.d("getRegionsFromParent", "downloaded ${localValues.size}/${parent.childIds.size}")
        val liveData = ListLiveData(childRegions)

        parent.childIds.minus(childRegions.mapNotNull { it?.id }).forEach { id ->
            newIoThread {
                val region = getRegion(id)
                mainThread {
                    liveData.add(region)
                }
            }
        }

        Log.d("getRegionsFromParent", "as livedata ${liveData.size()}/${parent.childIds.size}")
        return liveData
    }

    suspend fun getRegionsFromParentNonLive(parent: SkiRegion): List<SkiRegion?> {
        //get downloaded regions
        val childRegions: MutableList<SkiRegion?> = regions.filter { it.parentId == parent.id }
            .toMutableList()

        parent.childIds.minus(childRegions.mapNotNull { it?.id }).forEach { id ->
            childRegions.add(getRegion(id))
        }

        return childRegions
    }

    suspend fun getRegionById(id: Int): SkiRegion? {
        val local = regions.toMutableList().firstOrNull { it.id == id }
        return local ?: getRegion(id)
    }


    suspend fun findAreaParents(area: SkiArea): List<SkiRegion> {
        val areaParents = regions.filter { it.areaIds.contains(area.id) }.toMutableList()
        area.parentIds.minus(areaParents.map { it.id }).forEach {
            areaParents.addNotNull(getRegion(it))
        }
        return areaParents
    }

    private suspend fun getRegion(id: Int): SkiRegion? {


        if (!currentlyDownloading.containsKey(id)) {
            val job = CoroutineScope(Dispatchers.IO).async {
                return@async RegionXMLParser.downloadRegion(id)
            }
            currentlyDownloading[id] = job
        }

        return currentlyDownloading[id]?.await()
    }

}
