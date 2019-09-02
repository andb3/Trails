package com.andb.apps.trails.repository

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.RegionService
import com.andb.apps.trails.utils.addNotNull
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.RegionXMLConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.Exception
import java.net.UnknownHostException
import java.security.spec.ECField

object RegionsRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(RegionXMLConverterFactory())
        .build()

    private val regionService = retrofit.create(RegionService::class.java)

    private val regions = ArrayList<SkiRegion>()
    private val currentlyDownloading = mutableMapOf<Int, Deferred<Response<SkiRegion?>>>()

    var initLoad = false
    fun init(lifecycleOwner: LifecycleOwner, onLoad: (() -> Unit)? = null) {
        regionsDao().getAll().observe(lifecycleOwner, Observer { regions ->
            this.regions.clear()
            this.regions.addAll(regions)
            if (!initLoad) {
                onLoad?.invoke()
                initLoad = true
            }
        })
    }

    fun getRegionsFromParent(parent: SkiRegion): ListLiveData<SkiRegion?> {
        val childRegions: MutableList<SkiRegion?> = regions.filter { parent.childIds.contains(it.id) }
            .toMutableList()
        Log.d("getRegionsFromParent", "local ${childRegions.size}/${parent.childIds.size}")
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
        val childRegions: MutableList<SkiRegion?> = regions.filter { parent.childIds.contains(it.id) }
            .toMutableList()

        parent.childIds.minus(childRegions.mapNotNull { it?.id }).forEach { id ->
            childRegions.add(getRegion(id))
        }

        return childRegions
    }

    suspend fun getRegionById(id: Int): SkiRegion? {
        val local = regions.toMutableList().firstOrNull { it?.id == id }
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

        //TODO: remove jobs that were null due to offline (so refresh works)
        if (!currentlyDownloading.containsKey(id)) {
            try {
                currentlyDownloading[id] = regionService.getRegion(id)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val region = try {
            val job = currentlyDownloading[id]
            val response = job?.await()
            response?.body()
        } catch (e: Exception) {
            currentlyDownloading.remove(id)
            e.printStackTrace()
            null
        }
        if (region != null) {
            regionsDao().insertRegion(region)
        }
        return region
    }

}
