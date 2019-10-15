package com.andb.apps.trails.objects

import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo

class SkiRegionTree(val id: Int,
                    val name: String,
                    val mapCount: Int,
                    val childRegions: List<SkiRegionTree>,
                    val childAreas: List<SkiArea>,
                    var offline: Boolean) {
}

suspend fun SkiRegion.toTree(): SkiRegionTree {
    val regionTree = RegionsRepo.getRegionsFromParent(this).map { it.toTree() }
    val areaTree = AreasRepo.getAreasFromRegion(this).sortedBy { it.name }
    val offline = regionTree.size != this.childIDs.size || areaTree.size != this.areaIDs.size
    return SkiRegionTree(this.id, this.name, this.mapCount, regionTree, areaTree, offline)
}