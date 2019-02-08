package com.andb.apps.trails.lists

import android.content.Context
import android.os.AsyncTask
import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.BaseSkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.xml.AreaXMLParser
import com.nostra13.universalimageloader.core.ImageLoader
import kotlinx.coroutines.*

object FavoritesList{
    val favoriteMaps = ArrayList<SkiMap>()
    val favoriteAreas = ArrayList<BaseSkiArea>()

    fun init(){
        favoriteMaps.apply {
            clear()
            addAll(mapsDao().getAll())
        }
        favoriteAreas.apply {
            clear()
            addAll(areasDao().getFavorites())
        }
    }

    fun contains(map: SkiMap): Boolean{
        favoriteMaps.forEach {
            if(it.id==map.id){
                return true
            }
        }
        return false
    }

    fun contains(mapID: Int): Boolean{
        favoriteMaps.forEach {
            if(it.id==mapID){
                return true
            }
        }
        return false
    }

    fun contains(area: BaseSkiArea): Boolean{
        favoriteAreas.forEach {
            if(it.id==area.id){
                return true
            }
        }
        return false
    }


    fun add(map: SkiMap){
        favoriteMaps.add(map)
        AsyncTask.execute {
            mapsDao().insertMap(map)
        }
    }

    fun add(area: BaseSkiArea){
        //AreaXMLParser.parseBase(area.id)
        area.favorite = 1
        favoriteAreas.add(area)
        AsyncTask.execute {
            areasDao().updateArea(area)
        }
    }

    fun remove(map: SkiMap){
        favoriteMaps.remove(map)
        AsyncTask.execute {
            mapsDao().deleteMap(map)
            ImageLoader.getInstance().diskCache.remove(map.imageUrl)
        }
    }

    fun remove(area: BaseSkiArea){
        area.favorite = 0
        favoriteAreas.remove(area)
        AsyncTask.execute {
            areasDao().updateArea(area)
        }
    }

    fun positionInList(pos: Int): Int{
        return when(pos){
            0, favoriteMaps.size+1 -> -1 //dividers
            in 1..favoriteMaps.size -> pos-1
            else->pos- favoriteMaps.size-2
        }
    }

    fun count(): Int = favoriteMaps.size + favoriteAreas.size + 2/*for dividers*/


}