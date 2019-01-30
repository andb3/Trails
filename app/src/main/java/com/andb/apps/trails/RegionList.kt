package com.andb.apps.trails

import com.andb.apps.trails.objects.SkiRegion

object RegionList{
    val backStack = ArrayList<SkiRegion>()

    fun currentRegion() = backStack.last()

    fun drop(){
        backStack.removeAt(backStack.size-1)
    }
}