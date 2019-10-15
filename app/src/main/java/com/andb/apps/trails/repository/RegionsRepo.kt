package com.andb.apps.trails.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.database.RegionsDao
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.InitialLiveData
import com.andb.apps.trails.utils.RegionService
import com.andb.apps.trails.utils.newIoThread
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RegionsRepo : KoinComponent {

    private val retrofit: Retrofit by inject()

    private val regionService = retrofit.create(RegionService::class.java)

    val loading = InitialLiveData(false)
    val regionsDao: RegionsDao by inject()

    val regions by lazy { regionsDao.getAllStatic() }

    fun getRegionsFromParent(parent: SkiRegion): List<SkiRegion> {
        return regions.filter { it.parentID == parent.id }
    }

    fun getParents(): List<SkiRegion>{
        return regions.filter { (1..4).contains(it.id) }
    }

    fun getRegionByID(id: Int): SkiRegion? {
        return regions.find { it.id == id }
    }

    fun findAreaParents(area: SkiArea): List<SkiRegion> {
        return area.parentIDs.mapNotNull { getRegionByID(it) }
    }

    /**Refreshes regions from backend. Returns boolean indicating whether loading was successful**/
    suspend fun updateRegions(): Boolean {
        try {
            if (regionsDao.getAllStatic().size < regionService.getRegionCount()) {
                val regions = regionService.getAllRegions()
                regionsDao.insertMultipleRegions(regions)
            } else {
                val newRegions = regionService.getRegionUpdates(Prefs.lastRegionsUpdate)
                regionsDao.insertMultipleRegions(newRegions)
            }
            Prefs.lastRegionsUpdate = System.currentTimeMillis()

            return true
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }

    }

}
