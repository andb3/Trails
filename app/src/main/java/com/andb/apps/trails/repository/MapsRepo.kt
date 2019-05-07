package com.andb.apps.trails.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.MapXMLParser

object MapsRepo {

/*    private val maps = ArrayList<SkiMap>()

    fun init(lifecycleOwner: LifecycleOwner){
        mapsDao().getAll().observe(lifecycleOwner, Observer { maps->
            this.maps.clear()
            this.maps.addAll(maps)
        })
    }*/

    suspend fun getMaps(parent: SkiArea): List<SkiMap>{
        /*val localValues = maps.filter { it.parentId==parent.id } //get downloaded regions
        val localIds = localValues.map { it.id }
        if(!localIds.containsAll(parent.maps)){ //check if all regions are downloaded
            parent.maps.filter { !localIds.contains(it) }.forEach {//if not, download
                MapXMLParser.downloadMap(it)
                //TODO: async downloading
            }
            return maps.filter { it.parentId==parent.id }
            //TODO: handle end of downloading and internet errors
        }else{//if so, return them
            return localValues
        }*/
        return parent.maps.mapNotNull {
            MapXMLParser.getMap(it)
        }
    }

    fun getMapById(id: Int): SkiMap?{
        /*val localValues = getAllDownloadedMaps()
        if(!localValues.map { it.id }.contains(id)){
            return MapXMLParser.downloadMap(id)
        }else{
            return localValues.first { it.id == id }
        }*/
        return MapXMLParser.getMap(id)
    }

/*    fun getAllDownloadedMaps() = maps*/

    fun getFavoriteMaps(): List<SkiMap>{
        //return maps.filter { it.favorite } //Can only be favorited if already downloaded, so no check
        return emptyList()
    }
}