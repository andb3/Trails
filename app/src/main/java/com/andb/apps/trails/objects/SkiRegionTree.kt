package com.andb.apps.trails.objects

import com.andb.apps.trails.repository.AreasRepo
import com.andb.apps.trails.repository.RegionsRepo

class SkiRegionTree(val id: Int,
                    val name: String,
                    val mapCount: Int,
                    val childRegions: List<SkiRegionTree>,
                    val childAreas: List<SkiArea>,
                    var offline: Boolean) {
    /**Returns whether region is one of the 4 base regions (Americas, Europe, Asia, Oceania)**/
    fun isBase() = listOf(1, 2, 3, 4).contains(id)

    /**Returns whether region isn't a base region**/
    fun isChild() = !isBase()

    fun childrenSize() = childRegions.size + childAreas.size

}

suspend fun SkiRegion.toTree(): SkiRegionTree {
    val regionTree = RegionsRepo.getRegionsFromParent(this).map { it.toTree() }.filter { this.isChild() || it.mapCount>0 }
        .sortedWith(Comparator { o1, o2 ->
            if (this.isBase())
                o2.mapCount.compareTo(o1.mapCount)
            else
                o1.name.compareTo(o2.name)
        })
    val areaTree = AreasRepo.getAreasFromRegion(this).sortedBy { it.name }
    val offline = (regionTree.isEmpty() && this.childRegionIDs.isNotEmpty()) || (areaTree.isEmpty() && this.childAreaIDs.isNotEmpty())
    return SkiRegionTree(this.id, this.name, this.mapCount, regionTree, areaTree, offline)
}