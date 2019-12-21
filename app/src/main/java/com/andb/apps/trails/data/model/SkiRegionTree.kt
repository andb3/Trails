package com.andb.apps.trails.data.model

data class SkiRegionTree(val id: Int,
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

fun SkiRegion.toTree(regionSource: List<SkiRegion>, areaSource: List<SkiArea>): SkiRegionTree {
    val regionTree = regionSource.filter { it.parentID == this.id }
        .map { it.toTree(regionSource, areaSource) }.filter { this.isChild() || it.mapCount > 0 }
        .sortedWith(Comparator { o1, o2 ->
            if (this.isBase())
                o2.mapCount.compareTo(o1.mapCount)
            else
                o1.name.compareTo(o2.name)
        })
    val areaTree = areaSource.filter { it.parentIDs.contains(this.id) }.sortedBy { it.name }
    val offline = (regionTree.isEmpty() && this.childRegionIDs.isNotEmpty()) || (areaTree.isEmpty() && this.childAreaIDs.isNotEmpty())
    return SkiRegionTree(this.id, this.name, this.mapCount, regionTree, areaTree, offline)
}