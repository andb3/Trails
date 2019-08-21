package com.andb.apps.trails.repository

import android.util.Log
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import com.andb.apps.trails.ListLiveData
import com.andb.apps.trails.database.areasDao
import com.andb.apps.trails.objects.SkiArea
import com.andb.apps.trails.objects.SkiRegion
import com.andb.apps.trails.utils.*
import com.andb.apps.trails.xml.AreaXMLConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.*
import retrofit2.Response
import retrofit2.Retrofit
import java.lang.Exception
import java.net.UnknownHostException
import kotlin.collections.ArrayList

object AreasRepo {

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://skimap.org/")
        .addCallAdapterFactory(CoroutineCallAdapterFactory())
        .addConverterFactory(AreaXMLConverterFactory())
        .build()

    private val areaService = retrofit.create(AreaService::class.java)

    private val areas = ArrayList<SkiArea>()
    private val currentlyDownloading = mutableMapOf<Int, Deferred<Response<SkiArea?>>>()


    var initLoad = false
    fun init(lifecycleOwner: LifecycleOwner, onLoad: (() -> Unit)? = null) {
        areasDao().getAll().observe(lifecycleOwner, Observer { areas ->
            this.areas.clear()
            this.areas.addAll(areas)
            if (!initLoad) {
                onLoad?.invoke()
                initLoad = true
            }
        })
    }

    fun getAreasFromRegion(parent: SkiRegion): ListLiveData<SkiArea?> {
        val localValues: MutableList<SkiArea?> = areas.toList().filter { parent.areaIds.contains(it.id) }
            .toMutableList()
        val liveData = ListLiveData(localValues)

        parent.areaIds.minus(localValues.mapNotNull { it?.id }).forEach { id ->
            newIoThread {
                val area = getArea(id)
                mainThread {
                    liveData.add(area)
                }
            }
        }

        return liveData
    }

    suspend fun getAreasFromRegionNonLive(parent: SkiRegion): List<SkiArea?> {
        val localValues: MutableList<SkiArea?> = areas.toList().filter { parent.areaIds.contains(it.id) }
            .toMutableList()

        parent.areaIds.minus(localValues.mapNotNull { it?.id }).forEach { id ->
            localValues.add(getArea(id))
        }

        return localValues
    }


    suspend fun getAreaById(id: Int): SkiArea? {
        val possible = areas.toList().firstOrNull { it.id == id }
        Log.d("getAreaById", "possible id: ${possible?.id}")
        return possible ?: getArea(id)
    }

    private suspend fun getArea(id: Int): SkiArea? {

        if (!currentlyDownloading.containsKey(id)) {
            currentlyDownloading[id] = areaService.getArea(id)
        }

        val area = try { currentlyDownloading[id]?.await()?.body() } catch (e: Exception){
            currentlyDownloading.remove(id)
            null
        }
        if (area != null) {
            areasDao().insertArea(area)
        }
        return area
    }

}