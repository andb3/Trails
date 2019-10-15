package com.andb.apps.trails.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Transformations
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.database.AreasDao
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.AreaService
import com.andb.apps.trails.utils.InitialLiveData
import com.andb.apps.trails.utils.newIoThread
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object AreasRepo : KoinComponent {

    private val retrofit: Retrofit by inject()

    private val areaService = retrofit.create(AreaService::class.java)

    val loading = InitialLiveData(false)
    val areasDao: AreasDao by inject()

    val areas by lazy { areasDao.getAllStatic() }

    fun getAreasFromRegion(parent: SkiRegion): List<SkiArea> {
        return areas.filter { it.parentIDs.contains(parent.id) }
    }

    suspend fun getAreaByID(id: Int): SkiArea? {
        return areasDao.getAreaByID(id)
    }

    /**Refreshes areas from backend. Returns boolean indicating whether loading was successful**/
    suspend fun updateAreas(): Boolean {
        try {
            if (areasDao.getAllStatic().size < areaService.getAreaCount()) {
                val areas = areaService.getAllAreas().filter { !it.name.isNullOrEmpty() }
                Log.d("updateAreas", "null names - ${areas.filter { it.name.isNullOrEmpty() }}")
                areasDao.insertMultipleAreas(areas)
            } else {
                val newAreas = areaService.getAreaUpdates(Prefs.lastAreasUpdate)
                areasDao.insertMultipleAreas(newAreas)
            }
            Prefs.lastAreasUpdate = System.currentTimeMillis()

            return true
        } catch (e: Exception) {
            return false
        }

    }

}