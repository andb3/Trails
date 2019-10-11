package com.andb.apps.trails.repository

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import com.andb.apps.trails.Prefs
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.utils.InitialLiveData
import com.andb.apps.trails.utils.MapService
import com.andb.apps.trails.utils.newIoThread
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object MapsRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")
        .addConverterFactory(MoshiConverterFactory.create())
        .build()

    private val mapService = retrofit.create(MapService::class.java)

    val loading = InitialLiveData(true)

    fun getMapsFromArea(parent: SkiArea): LiveData<List<SkiMap>> {
        return mapsDao().getMapsFromParent(parent.id)
    }


    suspend fun getMapByID(id: Int): SkiMap? {
        return mapsDao().getMapByID(id)
    }

    /**Refreshes maps from backend. Returns boolean indicating whether loading was successful**/
    fun updateMaps(): Boolean {
        try {
            newIoThread {
                if (mapsDao().getAllStatic().size < mapService.getMapCount()) {
                    val maps = mapService.getAllMaps()
                    mapsDao().insertMultipleMaps(maps)
                } else {
                    val newMaps = mapService.getMapUpdates(Prefs.lastMapsUpdate)
                    mapsDao().insertMultipleMaps(newMaps)
                }
                Prefs.lastMapsUpdate = System.currentTimeMillis()
            }
            return true
        } catch (e: Exception) {
            return false
        }

    }

}