package com.andb.apps.trails.repository

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.mapsDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiMap
import com.andb.apps.trails.utils.MapService
import com.andb.apps.trails.utils.mainThread
import com.andb.apps.trails.utils.newIoThread
import com.andb.apps.trails.xml.MapXMLConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Response
import retrofit2.Retrofit
import java.net.UnknownHostException

object MapsRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(MapXMLConverterFactory())
        .build()

    private val mapService = retrofit.create(MapService::class.java)

    private val maps = ArrayList<SkiMap>()
    private val currentlyDownloading = mutableMapOf<Int, Deferred<Response<SkiMap?>>>()

    fun init(lifecycleOwner: LifecycleOwner) {
        mapsDao().getAll().observe(lifecycleOwner, Observer { maps ->
            this.maps.clear()
            this.maps.addAll(maps)
        })
    }

    fun getMaps(parent: SkiArea): LiveData<List<SkiMap>> {
        val liveData = mapsDao().getMapsFromParent(parent.id)

        val localIds: List<Int> = liveData.value?.map { it.id } ?: listOf()
        val toDownload = parent.maps.minus(localIds)
        Log.d("getMaps", "localIds: $localIds, toDownload: $toDownload")
        parent.maps.minus(localIds).forEach {
            newIoThread {
                downloadMap(it)
                //will propagate to liveData through inserting into the database
            }
        }

        return liveData
    }

    suspend fun getMapsNonLive(parent: SkiArea): List<SkiMap> {
        val localValues = maps.filter { it.parentId == parent.id }.toMutableList() //get downloaded regions

        val localIds = localValues.map { it.id }
        parent.maps.minus(localIds).forEach {
            val downloadedMap = downloadMap(it)

            if (downloadedMap != null) {
                localValues.add(downloadedMap)
            }

        }

        return localValues
    }

    suspend fun getMapById(id: Int): SkiMap? {
        val possible = ArrayList(maps).firstOrNull { it.id == id }
//        return possible?: MapXMLParser.downloadMap(id)
        return possible ?: downloadMap(id)
    }

    private suspend fun downloadMap(id: Int): SkiMap? {
        if (!currentlyDownloading.containsKey(id)) {
            currentlyDownloading[id] = mapService.getMap(id)
        }

        val map = try { currentlyDownloading[id]?.await()?.body() } catch (e: UnknownHostException){
            currentlyDownloading.remove(id)
            null
        }
        if (map != null) {
            mapsDao().insertMap(map)
            Log.d("downloadMap", "inserting - id: $id")
        }else{

            Log.d("downloadMap", "download failed - id: $id")
        }
        return map
    }

}