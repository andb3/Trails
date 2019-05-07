package com.andb.apps.trails

import androidx.lifecycle.ViewModel
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.pages.ExploreFragment

class ExploreViewModel : ViewModel(){
    val regionStack = ArrayList<SkiRegion>()
    var childRegions = listOf<SkiRegion>()
    var childAreas = listOf<SkiArea>()
    private val chips = ArrayList<ChipItem>()

    fun setParentRegion(id: Int){

    }

    fun isAllLoaded(): Boolean{
        return regionStack.last().childIds == childRegions.map { it.id } && regionStack.last().areaIds == childAreas.map { it.id }
    }
}

class ChipItem(val parentId: Int, val region: SkiRegion? = null, val area: SkiArea? = null)
