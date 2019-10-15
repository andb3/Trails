package com.andb.apps.trails.lists

import android.util.Log
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.objects.SkiRegionTree
import com.andb.apps.trails.objects.toTree
import com.andb.apps.trails.repository.RegionsRepo
import com.andb.apps.trails.utils.time

object RegionList {
    val backStack = ArrayList<SkiRegionTree>()
    fun currentRegion() = backStack.last()

    val parentRegions = ArrayList<SkiRegionTree>()

    suspend fun setup() {
        Log.d("RegionList.setup", "saved parents: ${RegionsRepo.getParents()}")
        parentRegions.addAll(RegionsRepo.getParents().map { parentSkiRegion ->
            Log.d("RegionList.setup", "adding region - ${parentSkiRegion.name}")
            parentSkiRegion.toTree()
        })
    }

    fun drop() {
        backStack.removeAt(backStack.size - 1)
    }
}