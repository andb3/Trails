package com.andb.apps.trails.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.MapXMLParser

object MapsRepo {

    private val maps = ArrayList<SkiMap>()

    fun init(lifecycleOwner: LifecycleOwner){
        mapsDao().getAll().observe(lifecycleOwner, Observer { maps->
            this.maps.clear()
            this.maps.addAll(maps)
        })
    }

    fun getMaps(parent: SkiArea): ListLiveData<SkiMap>{
        val localValues = maps.filter { it.parentId==parent.id } //get downloaded regions
        val liveData = ListLiveData(localValues)

        val localIds = localValues.map { it.id }
        parent.maps.minus(localIds).forEach {
            newIoThread {
                val downloadedMap = MapXMLParser.downloadMap(it)
                if(downloadedMap!=null){
                    mainThread {
                        liveData.add(downloadedMap)
                    }
                }
            }
        }

        return liveData
    }

    fun getMapsNonLive(parent: SkiArea): List<SkiMap>{
        val localValues = maps.filter { it.parentId==parent.id }.toMutableList() //get downloaded regions

        val localIds = localValues.map { it.id }
        parent.maps.minus(localIds).forEach {
                val downloadedMap = MapXMLParser.downloadMap(it)
                if(downloadedMap!=null){
                    localValues.add(downloadedMap)
                }

        }

        return localValues
    }

    fun getMapById(id: Int): SkiMap?{
        val possible = ArrayList(maps).firstOrNull { it.id == id }
        return possible?: MapXMLParser.downloadMap(id)
    }

    fun getFavoriteMaps(): List<SkiMap>{
        return maps.filter { it.favorite } //Can only be favorited if already downloaded, so no check
    }
}