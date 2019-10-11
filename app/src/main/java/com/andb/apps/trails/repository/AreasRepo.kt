package com.andb.apps.trails.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.AreaService
import com.andb.apps.trails.utils.InitialLiveData
import com.andb.apps.trails.utils.newIoThread
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AreasRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val areaService = retrofit.create(AreaService::class.java)

    val loading = InitialLiveData(true)

    fun getAreasFromRegion(parent: SkiRegion): LiveData<List<SkiArea>> {
        return Transformations.map(areasDao().getAll()) { areas ->
            return@map areas.filter { it.parentIDs.contains(parent.id) }
        }
    }

    suspend fun getAreaByID(id: Int): SkiArea? {
        return areasDao().getAreaByID(id)
    }

    /**Refreshes areas from backend. Returns boolean indicating whether loading was successful**/
    fun updateAreas(): Boolean {
        try {
            newIoThread {
                if (areasDao().getAllStatic().size < areaService.getAreaCount()) {
                    val areas = areaService.getAllAreas()
                    areasDao().insertMultipleAreas(areas)
                } else {
                    val newAreas = areaService.getAreaUpdates(Prefs.lastAreasUpdate)
                    areasDao().insertMultipleAreas(newAreas)
                }
                Prefs.lastAreasUpdate = System.currentTimeMillis()
            }
            return true
        } catch (e: Exception) {
            return false
        }

    }

}