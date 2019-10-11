package com.andb.apps.trails.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.database.regionsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.InitialLiveData
import com.andb.apps.trails.utils.RegionService
import com.andb.apps.trails.utils.newIoThread
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object RegionsRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")//TODO: change to backend
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val regionService = retrofit.create(RegionService::class.java)

    val loading = InitialLiveData(true)

    fun getRegionsFromParent(parent: SkiRegion): LiveData<List<SkiRegion>> {
        return regionsDao().getAllFromParent(parent.id)
    }

    suspend fun getRegionByID(id: Int): SkiRegion? {
        return regionsDao().getRegionByID(id)
    }

    suspend fun findAreaParents(area: SkiArea): List<SkiRegion> {
        return area.parentIDs.mapNotNull { getRegionByID(it) }
    }

    /**Refreshes regions from backend. Returns boolean indicating whether loading was successful**/
    fun updateRegions(): Boolean{
        try{
            newIoThread {
                if(regionsDao().getAllStatic().size < regionService.getRegionCount()){
                    val regions = regionService.getAllRegions()
                    regionsDao().insertMultipleRegions(regions)
                }else{
                    val newRegions = regionService.getRegionUpdates(Prefs.lastRegionsUpdate)
                    regionsDao().insertMultipleRegions(newRegions)
                }
                Prefs.lastRegionsUpdate = System.currentTimeMillis()
            }
            return true
        }catch (e: Exception){
            e.printStackTrace()
            return false
        }

    }

}
