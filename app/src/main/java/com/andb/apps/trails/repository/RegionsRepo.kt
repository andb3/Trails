package com.andb.apps.trails.repository

import android.util.Log
import android.util.Log.d
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.RegionXMLParser

object RegionsRepo {

    private val regions by lazy { ArrayList<SkiRegion>(regionsDao().getAllStatic()) }

    fun init(lifecycleOwner: LifecycleOwner) {
        regionsDao().getAll().observe(lifecycleOwner, Observer { regions ->
            this.regions.clear()
            this.regions.addAll(regions)
            d("regionsRepo", "init")
        })
    }

    fun getRegionsFromParent(parent: SkiRegion): ListLiveData<SkiRegion?> {
        val localValues = ArrayList(regions).filter { it.parentId == parent.id } //get downloaded regions
        Log.d("getRegionsFromParent", "local values: ${localValues.map { it.name }}")
        Log.d("getRegionsFromParent", "downloaded ${localValues.size}/${parent.childIds.size}")
        val liveData = ListLiveData(localValues)

        val localIds = localValues.map { it.id }
        parent.childIds.minus(localIds).forEach {
            newIoThread {
                val downloadedRegion = RegionXMLParser.downloadRegion(it)
                mainThread {
                    liveData.add(downloadedRegion)
                }
            }
        }

        Log.d("getRegionsFromParent", "as livedata ${liveData.size()}/${parent.childIds.size}")
        return liveData
    }

    fun getRegionsFromParentNonLive(parent: SkiRegion): List<SkiRegion?> {
        val localValues = ArrayList(regions).filter { it.parentId == parent.id }
            .toMutableList() //get downloaded regions

        val localIds = localValues.map { it.id }
        parent.childIds.minus(localIds).forEach {
            val downloadedRegion = RegionXMLParser.downloadRegion(it)
            localValues.add(downloadedRegion)
        }

        return localValues
    }

    fun getRegionById(id: Int): SkiRegion? {
        val possible = ArrayList(regions).firstOrNull { it.id == id }
        return possible ?: RegionXMLParser.downloadRegion(id)
    }


    fun findAreaParents(area: SkiArea): List<SkiRegion> {
        return area.parentIds.mapNotNull {
            RegionXMLParser.downloadRegion(it)
        }
    }


}
