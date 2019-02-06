package com.andb.apps.trails.objects

import android.util.Log
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.database.regionAreaDao
import com.andb.apps.trails.database.regionsDao

class SkiRegion(id: Int,
                name: String,
                mapCount: Int,
                parentId: Int,
                val areas: ArrayList<BaseSkiArea>,
                val children: ArrayList<SkiRegion>) : BaseSkiRegion(id, name, mapCount, parentId) {

    constructor(baseRegion: BaseSkiRegion) : this(
        baseRegion.id,
        baseRegion.name,
        baseRegion.mapCount,
        baseRegion.parentId?: -1,
        ArrayList(areasFromParent(baseRegion.id)),
        ArrayList(childrenFromParent(baseRegion.id))
    )



}

private fun childrenFromParent(parentId: Int): List<SkiRegion>{
    return regionsDao().getAllFromParent(parentId)
        .filter { baseSkiRegion -> baseSkiRegion.mapCount!=0 }
        .map { baseSkiRegion -> SkiRegion(baseSkiRegion) }
        .sortedWith(Comparator { o1, o2 ->
            when(parentId){
                1,2,3,4->o2.mapCount.compareTo(o1.mapCount)
                else-> o1.name.compareTo(o2.name)
            }
        })
}

private fun areasFromParent(parentId: Int): List<BaseSkiArea>{
    val joins = regionAreaDao().getJoinsByRegionId(parentId)
    Log.d("areasFromParent", "Size: ${joins.size}")
    return joins
        .map { areasDao().getAreasById(it.areaId)[0] }
        .sortedWith(Comparator { o1, o2 ->
            o1.name.compareTo(o2.name)
        })
    //return regionAreaDao().getAreasForRegion(parentId)
}