package com.andb.apps.trails.repository

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.xml.AreaXMLParser
import com.andb.apps.trails.xml.RegionXMLParser

object AreasRepo {

/*    private val areas = ArrayList<SkiArea>()

    fun init(lifecycleOwner: LifecycleOwner){
        areasDao().getAll().observe(lifecycleOwner, Observer { areas->
            this.areas.clear()
            this.areas.addAll(areas)
        })
    }*/

    fun getAreasByParent(parent: SkiRegion): List<SkiArea>{
/*        val localIds = areas.map { it.id }
        if(!localIds.containsAll(parent.areaIds)){ //check if all regions are downloaded
            parent.areaIds.filter { !localIds.contains(it) }.forEach {//if not, download
                AreaXMLParser.downloadArea(it)
                //TODO: async downloading
            }
            return areas
            //TODO: handle end of downloading and internet errors
        }else{//if so, return them
            return areas
        }*/
        return parent.areaIds.mapNotNull {
            AreaXMLParser.getArea(it)
        }
    }

    fun getAreaById(id: Int): SkiArea?{
        /*if(!areas.any { it.id==id }){
            return AreaXMLParser.downloadArea(id)
            //TODO: return directly from download
        }
        return areas.first { it.id == id }*/
        return AreaXMLParser.getArea(id)
    }

/*
    fun getAllDownloadedAreas() = areas
*/


    fun getFavoriteAreas(): List<SkiArea>{
        //return areas.filter { it.favorite } //Can only be favorited if already downloaded, so no check
        return emptyList()
    }

}