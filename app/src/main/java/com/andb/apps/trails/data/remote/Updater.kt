package com.andb.apps.trails.data.remote

import android.util.Log
import com.andb.apps.trails.data.local.AreasDao
import com.andb.apps.trails.data.local.MapsDao
import com.andb.apps.trails.data.local.Prefs
import com.andb.apps.trails.data.local.RegionsDao
import com.andb.apps.trails.util.InitialLiveData
import jonathanfinerty.once.Once
import org.koin.core.KoinComponent
import org.koin.core.inject
import retrofit2.Retrofit

object Updater : KoinComponent {

    val loadingRegions = InitialLiveData(false)
    val loadingAreas = InitialLiveData(false)
    val loadingMaps = InitialLiveData(false)

    private val regionsDao: RegionsDao by inject()
    private val areasDao: AreasDao by inject()
    private val mapsDao: MapsDao by inject()

    private val retrofit: Retrofit by inject()
    private val areaService = retrofit.create(AreaService::class.java)
    private val regionService = retrofit.create(RegionService::class.java)
    private val mapService = retrofit.create(MapService::class.java)


    /**Refreshes regions from backend. Returns boolean indicating whether loading was successful**/
    suspend fun updateRegions(): Boolean {
        loadingRegions.postValue(true)
        var loaded = false

        try {
            if (regionsDao.getAllStatic().size < regionService.getRegionCount()) {
                Log.d("updateRegions", "getting all")
                val regions = regionService.getAllRegions()
                regionsDao.insertMultipleRegions(regions)
            } else {
                Log.d("updateRegions", "getting updates only")
                val newRegions = regionService.getRegionUpdates(Prefs.lastRegionsUpdate)
                regionsDao.insertMultipleRegions(newRegions)
            }
            Prefs.lastRegionsUpdate = System.currentTimeMillis()
            Once.markDone("regionLoad")
            loaded = true
        } catch (e: Exception) {
            Log.d("updateRegions", "failed")
            e.printStackTrace()
        }

        loadingRegions.postValue(false)
        return loaded
    }

    /**Refreshes areas from backend. Returns boolean indicating whether loading was successful**/
    suspend fun updateAreas(): Boolean {
        loadingAreas.postValue(true)
        var loaded = false

        try {
            if (areasDao.getAllStatic().size < areaService.getAreaCount()) {
                Log.d("updateAreas", "getting all")
                val areas = areaService.getAllAreas().filter { !it.name.isNullOrEmpty() }
                Log.d("updateAreas", "null names - ${areas.filter { it.name.isNullOrEmpty() }}")
                areasDao.insertMultipleAreas(areas)
            } else {
                Log.d("updateAreas", "getting updates only")
                val newAreas = areaService.getAreaUpdates(Prefs.lastAreasUpdate)
                areasDao.insertMultipleAreas(newAreas)
            }
            Prefs.lastAreasUpdate = System.currentTimeMillis()
            Once.markDone("areaLoad")
            loaded = true
        } catch (e: Exception) {
            Log.d("updateAreas", "failed")
            e.printStackTrace()
        }

        loadingAreas.postValue(false)
        return loaded
    }

    /**Refreshes maps from backend. Returns boolean indicating whether loading was successful**/
    suspend fun updateMaps(): Boolean {
        loadingMaps.postValue(true)
        var loaded = false

        try {
            if (mapsDao.getAllStatic().size < mapService.getMapCount()) {
                val maps = mapService.getAllMaps()
                mapsDao.insertMultipleMaps(maps)
            } else {
                val newMaps = mapService.getMapUpdates(Prefs.lastMapsUpdate)
                mapsDao.insertMultipleMaps(newMaps)
            }
            Prefs.lastMapsUpdate = System.currentTimeMillis()
            Once.markDone("mapLoad")
            loaded = true
        } catch (e: Exception) {
            e.printStackTrace()
        }

        loadingMaps.postValue(false)
        return loaded

    }
}