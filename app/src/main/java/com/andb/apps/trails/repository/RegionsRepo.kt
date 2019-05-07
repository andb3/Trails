package com.andb.apps.trails.repository

import android.util.Log.d
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.xml.MapXMLParser
import com.andb.apps.trails.xml.RegionXMLParser

object RegionsRepo {
/*
    private val regions by lazy { ArrayList<SkiRegion>(regionsDao().getAllStatic()) }

    fun init(lifecycleOwner: LifecycleOwner){
        regionsDao().getAll().observe(lifecycleOwner, Observer { regions->
            this.regions.clear()
            this.regions.addAll(regions)
            d("regionsRepo", "init")
        })
    }*/

    fun getRegions(parent: SkiRegion): List<SkiRegion>{
        /*val localValues = ArrayList(regions).filter { it.parentId==parent.id } //get downloaded regions
        val localIds = localValues.map { it.id }
        if(!localIds.containsAll(parent.childIds)){ //check if all regions are downloaded
            val newRegions = ArrayList<SkiRegion>(localValues)//make list to hold returned downloads
            parent.childIds.filter { !localIds.contains(it) }.forEach {//if not, download
                newRegions.add(RegionXMLParser.downloadRegion(it)?:return@forEach)
                //TODO: async downloading
            }
            return newRegions
            //TODO: handle end of downloading and internet errors
        }else{//if so, return them
            return localValues
        }*/
        return parent.childIds.mapNotNull {
            RegionXMLParser.getRegion(it)
        }
    }

    fun getRegionById(id: Int): SkiRegion?{
        /*val copy = ArrayList(regions)
        if(!copy.any { it.id==id }){
            return RegionXMLParser.downloadRegion(id)
        }else{
            return copy.first { it.id==id}
        }*/
        return RegionXMLParser.getRegion(id)

    }

    fun getParentRegions(): List<SkiRegion>{
/*        val localValues = ArrayList(regions).filter { it.isParent() }
        val localIds = localValues.map { it.id }*/
        val parentIds = listOf(1, 2, 3, 4)
/*        d("getParentRegions", "localIds: $localIds")
        d("getParentRegions", "totalSize: ${regions.size}")
        if(!localIds.containsAll(parentIds)){
            parentIds.filter { !localIds.contains(it) }.forEach {
                RegionXMLParser.downloadRegion(it)
            }
            return regions.filter { it.isParent() }
        }else{
            return localValues
        }*/
        return parentIds.mapNotNull {
            RegionXMLParser.getRegion(it)
        }
    }

    fun findAreaParents(area: SkiArea): List<SkiRegion>{
        return area.parentIds.mapNotNull {
            RegionXMLParser.getRegion(it)
        }
    }


}
