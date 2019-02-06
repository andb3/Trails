package com.andb.apps.trails.lists

import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.BaseSkiArea

object AreaList {
    val areaList = ArrayList<BaseSkiArea>()

    fun setup(){
        areaList.apply {
            clear()
            addAll(areasDao().getAll())
        }
    }
}