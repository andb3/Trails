package com.andb.apps.trails.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.AreaXMLParser

object AreasRepo {

    private val areas = ArrayList<SkiArea>()

    fun init(lifecycleOwner: LifecycleOwner) {
        areasDao().getAll().observe(lifecycleOwner, Observer { areas ->
            this.areas.clear()
            this.areas.addAll(areas)
        })
    }

    fun getAreasFromRegion(parent: SkiRegion): ListLiveData<SkiArea?> {
        val localValues = ArrayList(areas).filter { parent.areaIds.contains(it.id) }
        val liveData = ListLiveData(localValues)
        val localIds = localValues.map { it.id }

        parent.areaIds.minus(localIds).forEach {
            newIoThread {
                val downloadedArea = AreaXMLParser.downloadArea(it)
                mainThread {
                    liveData.add(downloadedArea)
                }
            }
        }
        return liveData
    }

    fun getAreasFromRegionNonLive(parent: SkiRegion): List<SkiArea?> {
        val localValues = ArrayList(areas).filter { parent.areaIds.contains(it.id) }.toMutableList()
        val localIds = localValues.map { it.id }

        parent.areaIds.minus(localIds).forEach {

            val downloadedArea = AreaXMLParser.downloadArea(it)
            localValues.add(downloadedArea)

        }

        return localValues
    }


    fun getAreaById(id: Int): SkiArea? {
        val possible = ArrayList(areas).firstOrNull { it.id == id }
        return possible ?: AreaXMLParser.downloadArea(id)
    }


    fun getFavoriteAreas(): List<SkiArea> {
        return areas.filter { it.favorite } //Can only be favorited if already downloaded, so no check
    }

}