package com.andb.apps.trails.utils

import com.andb.apps.trails.objects.*
import com.andb.apps.trails.xml.RegionXMLConverter
import com.andb.apps.trails.xml.RegionXMLConverterFactory
import com.jakewharton.retrofit2.adapter.kotlin.coroutines.CoroutineCallAdapterFactory
import kotlinx.coroutines.Deferred
import retrofit2.Call
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.http.GET
import retrofit2.http.Path

interface MapService {
    @GET("/SkiMaps/view/{mapId}.xml")
    fun getMap(@Path("mapId") mapId: Int): Deferred<Response<SkiMap>>
}

interface AreaService{
    @GET("/SkiAreas/view/{areaId}.xml")
    fun getArea(@Path("areaId") areaId: Int): Deferred<Response<SkiArea>>
}

interface RegionService{
    @GET("/SkiRegions/view/{regionId}.xml")
    fun getRegion(@Path("regionId") regionId: Int): Call<SkiRegion?>
}

const val BASE_URL = "https://skimap.org"
fun makeMapService(): MapService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(MapService::class.java)

fun makeAreaService(): AreaService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addCallAdapterFactory(CoroutineCallAdapterFactory())
    .build()
    .create(AreaService::class.java)

fun makeRegionService(): RegionService = Retrofit.Builder()
    .baseUrl(BASE_URL)
    .addConverterFactory(RegionXMLConverterFactory())
    .build()
    .create(RegionService::class.java)