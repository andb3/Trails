package com.andb.apps.trails.lists

import com.andb.apps.trails.database.Database
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiRegion

object RegionList{
    val backStack = ArrayList<SkiRegion>()
    fun currentRegion() = backStack.last()

    val parentRegions = ArrayList<SkiRegion>()

    fun setup(){
        parentRegions.addAll(regionsDao().getAllParents().map { parentSkiRegion ->
            SkiRegion(parentSkiRegion)
        })
    }

    fun drop(){
        backStack.removeAt(backStack.size-1)
    }
}