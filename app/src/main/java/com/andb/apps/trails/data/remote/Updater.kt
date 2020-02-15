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
import java.util.concurrent.TimeUnit

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

    fun regionUpdateNeeded() = !Once.beenDone(TimeUnit.DAYS, 1, "regionLoad")
    fun areaUpdateNeeded() = !Once.beenDone(TimeUnit.DAYS, 1, "areaLoad")
    fun mapUpdateNeeded() = !Once.beenDone(TimeUnit.DAYS, 1, "mapLoad")

    /**
     * Refreshes regions from backend.
     * @return Boolean indicating whether loading was successful
     **/
    suspend fun updateRegions(): Boolean {
        loadingRegions.postValue(true)
        var loaded = false

        try {
            Log.d("updateRegions", "getting since ${Prefs.lastRegionsUpdate}")

            val newRegions = regionService.getRegions(Prefs.lastRegionsUpdate)
            regionsDao.insertMultipleRegions(newRegions)

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

    /**
     * Refreshes areas from backend.
     * @return Boolean indicating whether loading was successful
     **/
    suspend fun updateAreas(): Boolean {
        loadingAreas.postValue(true)
        var loaded = false
        try {
            Log.d("updateAreas", "getting since ${Prefs.lastAreasUpdate}")
            val areas =
                areaService.getAreas(Prefs.lastAreasUpdate).filter { !it.name.isNullOrEmpty() }

            //Preserve favorite status of updated areas
            val currentAreas = areasDao.getAllStatic()
            areas.forEach { new ->
                new.favorite = currentAreas.find { old -> old.id == new.id }?.favorite ?: false
            }
            areasDao.insertMultipleAreas(areas)
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

    /**
     * Refreshes maps from backend.
     * @return Boolean indicating whether loading was successful
     **/
    suspend fun updateMaps(): Boolean {
        loadingMaps.postValue(true)
        var loaded = false
        try {
            Log.d("updateMaps", "getting since ${Prefs.lastMapsUpdate}")
            val maps = mapService.getMaps(Prefs.lastMapsUpdate)

            //Preserve favorite status of updated maps
            val currentMaps = mapsDao.getAllStatic()
            maps.forEach { new ->
                new.favorite = currentMaps.find { old -> old.id == new.id }?.favorite ?: false
            }
            mapsDao.insertMultipleMaps(maps)
            Prefs.lastMapsUpdate = System.currentTimeMillis()
            Once.markDone("mapLoad")
            loaded = true
        } catch (e: Exception) {
            Log.d("updateMaps", "failed")
            e.printStackTrace()
        }

        loadingMaps.postValue(false)
        return loaded
    }
}